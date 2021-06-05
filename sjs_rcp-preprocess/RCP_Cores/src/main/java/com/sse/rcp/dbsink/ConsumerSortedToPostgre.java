package com.sse.rcp.dbsink;

import com.sse.rcp.config.KafkaConsumerConfig;
import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.order.OrderDetail;
import com.sse.rcp.domains.order.OrderWithdraw;
import com.sse.rcp.domains.order.PositionDetail;
import com.sse.rcp.domains.order.TradeDetail;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
@DependsOn("storageDao")
@Component("kafkaClientForSorted")
public class ConsumerSortedToPostgre implements IKafkaClient {
    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;
    @Autowired
    private StorageService storageService;
    private static final String THREAD_PREFIX = "DB4Sorted-Thread";

    @Value("${postgresql.batchsize}")
    private int batchSize;
    @Value("${postgresql.batchmillisecon}")
    private int batchmillisecon;

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
        List<OrderDetail> ocDataTrads = new LinkedList();
        List<OrderWithdraw> ocDataCancels = new LinkedList();
        List<TradeDetail> tcDatas = new LinkedList();
        List<PositionDetail> positionDatas = new LinkedList();

        int recordIndex = 0, positionIndex = 0;
        for (ConsumerRecord<byte[], byte[]> consumerRecord : records) {
            try {
                String eosFlag = KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.HEADER_EOS);
                if (KafkaProducerTool.isTodayEos(eosFlag)) {
//                    return true;
                    continue;  // TODO 现在的eos很不准确，所以暂时不退出执行
                }
                String dataType = KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.HEADER_DATA_TYPE);
                switch (dataType) {
                    case KafkaConstant.DATA_TYPE_OC:
                        MtpOrdcnmf ocData = JacksonSerializerUtil.deserializeWithType(consumerRecord.value(), MtpOrdcnmf.class);
//                        TODO  测试数据不整合，暂时取消异常校验
//                        if (ocData.getOrdrNum() <= 0) {
//                            log.info("[{}]的主题数据异常[{}]", Thread.currentThread().getName(), ocData.toString());
//                            continue;
//                        }
                        // OC类型的消息，事务类型 X - 撤销 | D - 删除
                        if (Objects.equals((ocData.getTrnTypId()), 'X')
                                || Objects.equals((ocData.getTrnTypId()), 'D')) {
                            ocDataCancels.add(StorageService.transToOrderWithCancel(ocData));
                        }
                        // OC类型的消息，事务类型	A - 增加
                        else if (Objects.equals((ocData.getTrnTypId()), 'A')) {
                            ocDataTrads.add(StorageService.transToOrderDetail(ocData));
                        }
                        recordIndex++;
                        break;
                    case KafkaConstant.DATA_TYPE_TC:
                        MtpTrdcnmf tcData = JacksonSerializerUtil.deserializeWithType(consumerRecord.value(), MtpTrdcnmf.class);
//                        TODO  测试数据不整合，暂时取消异常校验
//                        if (tcData.getTrdCfmBuy().getTranIdNo() <= 0 || tcData.getTrdCfmSell().getTranIdNo() <= 0
//                                || tcData.getTrdCfmSell().getOrdrNo() <= 0 || tcData.getTrdCfmBuy().getOrdrNo() <= 0) {
//                            log.info("[{}]的主题数据异常[{}]", Thread.currentThread().getName(), tcData.toString());
//                            continue;
//                        }
                        TradeDetail td = StorageService.transToTradeDetail(tcData);
                        tcDatas.add(td);
                        //收集持仓数据
                        StorageService.putPositionDetail(positionDatas, td);
                        recordIndex++;
                        positionIndex++;
                        break;

                }

                //判断数据是否可以批次提交
                if (recordIndex % batchSize == 0 || (System.currentTimeMillis() - startTime) > batchmillisecon) {
                    storeMtpPostgre(ocDataTrads, ocDataCancels, tcDatas, positionDatas);
                    ocDataTrads = new LinkedList();
                    ocDataCancels = new LinkedList();
                    tcDatas = new LinkedList();
                    positionDatas = new LinkedList();
                    consumer.commitSync();
                }

            } catch (Exception e) {
                log.error("线程[{}] ：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }

        storeMtpPostgre(ocDataTrads, ocDataCancels, tcDatas, positionDatas);
        consumer.commitSync();

        if (positionIndex > 0) {
            log.info("{}更新持仓表数据开始", topicName);
            storageService.updatePositionData();
            log.info("{}更新持仓表数据结束", topicName);
        }
//        log.info("monitor-排序信息写入数据库->主题{}本次处理{}条，耗时{}秒", topicName, ocDataTrads.size() + ocDataCancels.size() + tcDatas.size()
//                , (System.currentTimeMillis() - startTime) / 1000.0);

        return false;
    }


    /**
     * 存入竞价数据
     *
     * @param ocDataTrads
     * @param ocDataCancels
     * @param tcDatas
     * @param positionDatas
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void storeMtpPostgre(List<OrderDetail> ocDataTrads, List<OrderWithdraw> ocDataCancels, List<TradeDetail> tcDatas, List<PositionDetail> positionDatas) {
        if (!ocDataTrads.isEmpty()) {
            storageService.storeOrderDetail(ocDataTrads);
        }
        if (!ocDataCancels.isEmpty()) {
            storageService.storeOrderWithCancel(ocDataCancels);
        }
        if (!tcDatas.isEmpty()) {
            storageService.storeTradeDetail(tcDatas);
            //竞价成交持仓数据存入
            storageService.storePositionData(positionDatas);
        }
    }

}
