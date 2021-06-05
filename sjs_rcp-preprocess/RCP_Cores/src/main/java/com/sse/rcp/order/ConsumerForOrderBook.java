package com.sse.rcp.order;

import com.sse.rcp.config.KafkaConsumerConfig;
import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.kafka.IKafkaClient;
import com.sse.rcp.kafka.KafkaListenerFactory;
import com.sse.rcp.kafka.KafkaProducerTool;
import com.sse.rcp.lock.NeedDistributedLock;
import com.sse.rcp.monitor.MonitorPerformance;
import com.sse.rcp.utils.JacksonSerializerUtil;
import com.sse.rcp.utils.MonitorPrintUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;


@Slf4j
@DependsOn("orderOperator")
@Component()
public class ConsumerForOrderBook implements IKafkaClient {
    @Autowired
    private OrderOperator orderOperator;

    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;

    private static final String THREAD_PREFIX = "OrderThread-";

    /**
     * 启动一个consumer去监听消费目标topic的数据，并在消费完成后关闭
     *
     * @param topicName 指定的topic
     */
    @Async("taskExecutor")
    @NeedDistributedLock
    public void consumeData(String topicName) {
        Thread.currentThread().setName(THREAD_PREFIX + topicName);
        OrderBookCache.InitLocalCache();
        Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(kafkaConsumerConfig.getKafkaProperties(topicName));
        // 订阅主题
        consumer.subscribe(Arrays.asList(topicName), KafkaListenerFactory.getConsumerRebalanceListener(consumer));
        log.info("------线程[{}]订阅[{}]成功！------", Thread.currentThread().getName(), topicName);
        // 开始消费
        try {
            boolean eos = false;
            while (!eos) { // 流结束标记在最后一条记录
                // 每次消费poll出若干消息
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(kafkaConsumerConfig.getPollDuration()));
                if (null == records || records.count() < 1) continue;
                eos = consumePerBatch(records, records.count(), topicName);
            }
        } finally {
            KafkaProducerTool.closeLocalProducer();
            log.info("------线程[{}]已处理完当前数据，进入对[{}]的监听状态", Thread.currentThread().getName(), topicName);
//            consumer.close(); TODO 断开客户端链接的位置待定
        }
    }


    /**
     * @return true 闭市  false 正常交易
     */
    @MonitorPerformance
    public boolean consumePerBatch(ConsumerRecords<byte[], byte[]> records, int size, String topicName) {
        long startTime = System.currentTimeMillis();
        int perBatchCount = 0;
        for (ConsumerRecord<byte[], byte[]> consumerRecord : records) {
            try {
                String headerEos = KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.HEADER_EOS);
                if (KafkaProducerTool.isTodayEos(headerEos)) {
//                    return true;
                    continue;  // TODO 现在的eos很不准确，所以暂时不退出执行
                }
                TopicData data = JacksonSerializerUtil.deserializeWithType(consumerRecord.value(), TopicData.class);
                switch (data.dataType()) {
                    case KafkaConstant.DATA_TYPE_OC:
                        orderOperator.processOrdcnmf((MtpOrdcnmf) data);
                        perBatchCount++;
                        break;
                    case KafkaConstant.DATA_TYPE_TC:
                        orderOperator.processTrdcnmf((MtpTrdcnmf) data);
                        perBatchCount++;
                        break;
                    default:
                        log.error("Kafka排序主题出现不明数据错误{}", consumerRecord.headers().toString());
                }
                MonitorPrintUtil.printDelayTimeStamp(data); //TODO
            } catch (Exception e) {
                log.error("线程[{}]消费异常：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }
        log.info("monitor-订单簿重演模块->主题{}本次处理{}条，耗时{}秒", topicName, perBatchCount, (System.currentTimeMillis() - startTime) / 1000.0);
        return false;
    }

}

