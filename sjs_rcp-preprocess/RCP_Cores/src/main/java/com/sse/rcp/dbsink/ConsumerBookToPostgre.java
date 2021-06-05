package com.sse.rcp.dbsink;

import com.sse.rcp.config.KafkaConsumerConfig;
import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.order.OrderBookRecord;
import com.sse.rcp.kafka.IKafkaClient;
import com.sse.rcp.kafka.KafkaListenerFactory;
import com.sse.rcp.kafka.KafkaProducerTool;
import com.sse.rcp.lock.NeedDistributedLock;
import com.sse.rcp.monitor.MonitorPerformance;
import com.sse.rcp.utils.JacksonSerializerUtil;
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
import java.util.LinkedList;
import java.util.List;

@Slf4j
@DependsOn("storageDao")
@Component("kafkaClientForBook")
public class ConsumerBookToPostgre implements IKafkaClient {
    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;
    @Autowired
    private StorageService storageService;
    private static final String THREAD_PREFIX = "DB4OrdBook-Thread";

    /**
     * 启动一个consumer去监听消费目标topic的数据，并在消费完成后关闭
     *
     * @param topicNames 指定的topic
     */
    @Async("taskExecutor")
    @NeedDistributedLock
    public void consumeDataToDB(List<String> topicNames) {
        Thread.currentThread().setName(THREAD_PREFIX);
        Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(kafkaConsumerConfig.getKafkaPropertiesForDB(Thread.currentThread().getName()));
        consumer.subscribe(topicNames, KafkaListenerFactory.getConsumerRebalanceListener(consumer));
        log.info("入库线程[{}]订阅以下Topic[{}]成功！", Thread.currentThread().getName(), Arrays.toString(topicNames.toArray()));
        try {
            boolean eos = false;
            while (!eos) { // 流结束标记在最后一条记录
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(kafkaConsumerConfig.getPollDuration()));
                if (null == records || records.count() < 1) continue;
                eos = consumePerBatch(records, records.count(), records.iterator().next().topic());
            }
        } finally {
            KafkaProducerTool.closeLocalProducer();
//            consumer.close(); TODO 断开客户端链接的位置待定
        }
    }

    /**
     * @return true 闭市  false 正常交易
     */
    @MonitorPerformance
    public boolean consumePerBatch(ConsumerRecords<byte[], byte[]> records, int size, String topicName) {
        long startTime = System.currentTimeMillis();

        List<OrderBookRecord> ordBooks = new LinkedList();
        for (ConsumerRecord<byte[], byte[]> consumerRecord : records) {
            try {
                String eosFlag = KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.HEADER_EOS);
                if (KafkaProducerTool.isTodayEos(eosFlag)) {
//                    return true;
                    continue;  // TODO 现在的eos很不准确，所以暂时不退出执行
                }
                OrderBookRecord orderBookRecord = JacksonSerializerUtil.deserializeWithType(consumerRecord.value(), OrderBookRecord.class);
                ordBooks.add(orderBookRecord);
//            } catch (BadSqlGrammarException e) {
            } catch (Exception e) {
                log.error("线程[{}]消费异常：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }

        try {
            if (!ordBooks.isEmpty()) {
                storageService.storeOrderBooKRecord(ordBooks);
            }
        } catch (Exception e) {
            log.error("线程[{}]写数据库异常：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));

        }

//        log.info("monitor-订单簿信息写入数据库->主题{}本次处理{}条，耗时{}秒", topicName, ordBooks.size(), (System.currentTimeMillis() - startTime) / 1000.0);

        return false;
    }


}
