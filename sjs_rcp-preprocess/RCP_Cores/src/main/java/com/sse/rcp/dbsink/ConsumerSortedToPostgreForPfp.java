package com.sse.rcp.dbsink;

import com.alibaba.fastjson.JSON;
import com.sse.rcp.config.KafkaConsumerConfig;
import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
@DependsOn("storageDao")
@Component
public class ConsumerSortedToPostgreForPfp implements IKafkaClient {
    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;
    @Autowired
    private StorageService storageService;
    private static final String THREAD_PREFIX = "DB4Sorted-atp-Thread";

    @Value("${postgresql.batchsize}")
    private int batchSize;
    @Value("${postgresql.batchmillisecon}")
    private int batchmillisecon;

    /**
     * 启动一个consumer去监听消费目标topic的数据，并在消费完成后关闭
     * 综业排序数据入库
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
                eos = consumePerBatch(consumer, records, records.count(), records.iterator().next().topic());
            }
        } finally {
            consumer.close();
        }
    }

    /**
     * @return true 闭市  false 正常交易
     */
    @MonitorPerformance
    public boolean consumePerBatch(Consumer<byte[], byte[]> consumer, ConsumerRecords<byte[], byte[]> records, int size, String topicName) {
        long startTime = System.currentTimeMillis();
        //综业数据
        List<AtpTo> ocDataTrads_pfp = new LinkedList();
        List<AtpTo> ocDataCancels_pfp = new LinkedList();
        List<AtpTt> tcDatas_pfp = new LinkedList();

        int recordIndex = 0;
        for (ConsumerRecord<byte[], byte[]> consumerRecord : records) {
            try {
                String eosFlag = KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.HEADER_EOS);
                if (KafkaProducerTool.isTodayEos(eosFlag)) {
//                    return true;
                    continue;  // TODO 现在的eos很不准确，所以暂时不退出执行
                }
                String dataType = KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.HEADER_DATA_TYPE);

                switch (dataType) {
                    case KafkaConstant.DATA_TYPE_TO:
                        AtpTo pfpocData = JacksonSerializerUtil.deserializeWithType(consumerRecord.value(), AtpTo.class);
//                        TODO  测试数据不整合，暂时取消异常校验
//                        if (pfpocData.getOrdrNum() <= 0) {
//                            log.info("[{}]的主题数据异常[{}]", Thread.currentThread().getName(), pfpocData.toString());
//                            continue;
//                        }
                        // 消息类型 F=申报撤单
                        if (Objects.equals((pfpocData.getMsgTyp()), 'F')) {
                            ocDataCancels_pfp.add(pfpocData);
                        }
                        //消息类型 D=申报
                        else if (Objects.equals((pfpocData.getMsgTyp()), 'D')) {
                            ocDataTrads_pfp.add(pfpocData);
                        }
                        recordIndex++;
                        break;
                    case KafkaConstant.DATA_TYPE_TT:
                        AtpTt pfptcData = JacksonSerializerUtil.deserializeWithType(consumerRecord.value(), AtpTt.class);
//                        TODO  测试数据不整合，暂时取消异常校验
//                        if (pfptcData.getTrdCfmBuy().getTranIdNo() <= 0 || pfptcData.getTrdCfmSell().getTranIdNo() <= 0
//                                || pfptcData.getTrdCfmSell().getOrdrNo() <= 0 || pfptcData.getTrdCfmBuy().getOrdrNo() <= 0) {
//                            log.info("[{}]的主题数据异常[{}]", Thread.currentThread().getName(), pfptcData.toString());
//                            continue;
//                        }
                        tcDatas_pfp.add(pfptcData);
                        recordIndex++;
                        break;

                }

                //判断数据是否可以批次提交
                if (recordIndex % batchSize == 0 || (System.currentTimeMillis() - startTime) > batchmillisecon) {

                    storeAtpPostgre(ocDataTrads_pfp, ocDataCancels_pfp, tcDatas_pfp);
                    ocDataTrads_pfp = new LinkedList();
                    ocDataCancels_pfp = new LinkedList();
                    tcDatas_pfp = new LinkedList();
                    consumer.commitSync();
                }

            } catch (Exception e) {
                log.error("线程[{}]消费异常：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }

        storeAtpPostgre(ocDataTrads_pfp, ocDataCancels_pfp, tcDatas_pfp);
        consumer.commitSync();

        log.info("monitor-排序信息写入数据库->主题{}本次处理{}条，耗时{}秒", topicName, ocDataTrads_pfp.size() + ocDataCancels_pfp.size() + tcDatas_pfp.size()
                , (System.currentTimeMillis() - startTime) / 1000.0);

        return false;
    }

    /**
     * 存入综业盘后数据
     *
     * @param ocDataTrads_pfp
     * @param ocDataCancels_pfp
     * @param tcDatas_pfp
     */
    private void storeAtpPostgre(List<AtpTo> ocDataTrads_pfp, List<AtpTo> ocDataCancels_pfp, List<AtpTt> tcDatas_pfp) {
        try {
            if (!ocDataTrads_pfp.isEmpty()) {
                storageService.storePfpOrderDetail(ocDataTrads_pfp);
            }
            if (!ocDataCancels_pfp.isEmpty()) {
                storageService.storePfpOrderWithCancel(ocDataCancels_pfp);
            }
            if (!tcDatas_pfp.isEmpty()) {
                storageService.storePfpTradeDetail(tcDatas_pfp);
            }

        } catch (Exception e) {
            log.error("线程[{}]写数据库异常：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));

        }
    }


}
