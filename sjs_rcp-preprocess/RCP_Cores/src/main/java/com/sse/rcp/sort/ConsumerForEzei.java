package com.sse.rcp.sort;

import com.sse.rcp.config.KafkaConsumerConfig;
import com.sse.rcp.domains.constant.AppConst;
import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.*;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.utils.ByteUtil;
import com.sse.rcp.domains.utils.DomainsDecodeUtil;
import com.sse.rcp.kafka.IKafkaClient;
import com.sse.rcp.kafka.KafkaListenerFactory;
import com.sse.rcp.kafka.KafkaProducerTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@DependsOn("redisTemplate")
@Component("consumerForEzei")
public class ConsumerForEzei implements IKafkaClient {
    private static final String THREAD_PREFIX = "SortThread-";

    @Autowired
    private SortOperator sortOperator;
    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;

    private static ThreadLocal<Long> localSeq = new ThreadLocal<>();

    private static ThreadLocal<Map<Long, TopicData>> cacheDatas = new ThreadLocal<>();

    private static ConcurrentHashMap<String, String> eosCount = new ConcurrentHashMap();

    /**
     * 启动一个consumer去拉取目标topic的数据到缓存中，并在消费完成后关闭
     *
     * @param topicName 指定监听的topic
     */
    @Async("taskExecutor")
//    @NeedDistributedLock
    public void consumeData(String topicName, String targetTopic) {
        Thread.currentThread().setName(THREAD_PREFIX + topicName);
        Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(kafkaConsumerConfig.getKafkaPropertiesForEzei(topicName));
        consumer.subscribe(Arrays.asList(topicName), KafkaListenerFactory.getConsumerRebalanceListener(consumer));
        log.info("------线程[{}]订阅[{}]成功！------", Thread.currentThread().getName(), topicName);
        try {
            boolean eos = false;
            localSeq.set(0L);
            cacheDatas.set(new HashMap<>(2048));
            while (!eos) {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(kafkaConsumerConfig.getPollDuration()));
                if (null == records || records.count() < 1) continue;
                eos = consumePerBatch(records, records.count(), topicName);
            }
            // 处理eos
            makeEzeiEos(targetTopic);

        } finally {
            localSeq.remove();
            cacheDatas.remove();
            KafkaProducerTool.closeLocalProducer();
//            consumer.close(); TODO 断开客户端链接的位置待定
        }
    }

    /**
     * 消费每个拉取批次的记录数
     */
    public boolean consumePerBatch(ConsumerRecords<byte[], byte[]> records, int size, String topicName) {
        long startTime = System.currentTimeMillis();

        String ezeiTopic;
        int perBatchCount = 0;
        for (ConsumerRecord<byte[], byte[]> consumerRecord : records) {
            try {
                BcastType bcastType = BcastType.valueOf(KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.BCAST_TYPE));
                ByteBuffer recordBuffer = ByteBuffer.wrap(consumerRecord.value());
                while (recordBuffer.hasRemaining()) {
                    // ezei msg主题
                    ezeiTopic = ByteUtil.readCharSequence(recordBuffer, 32).trim();
                    // 消息条数
                    int recCount = recordBuffer.getInt();
                    perBatchCount += recCount;
                    // 单条消息长度
                    int recLen = recordBuffer.getShort();
                    EzEITopic topic = EzEITopics.SINGLETON.get(ezeiTopic);

                    // 根据解析得到的报文头，开始消费明细数据
                    for (int i = 0; i < recCount; i++) {
                        // 解析得到Ezei数据结构
                        EzEIData ezeiData = new EzEIData();
                        TopicHeader topicHdr = ezeiData.getTopicHdr();
                        topicHdr.setRecSeq(recordBuffer.getInt());
                        topicHdr.setExist(recordBuffer.get());
                        byte[] topicData = new byte[recLen];
                        recordBuffer.get(topicData);
                        ezeiData.setTopicData(DomainsDecodeUtil.decode(topicData, topic.getBcastType()));
                        // 流结束标记在最后一条记录
                        if (ezeiData.getTopicData().eos()) {
                            return true;
                        }

                        // 获取 OC 或 TC 的seq
                        long msgSeq = -1;
                        if (bcastType == BcastType.OC) {
                            //订单确认消息
                            msgSeq = ((MtpOrdcnmf) ezeiData.getTopicData()).getActnSeqNum() % AppConst.SEQ_SCALE;
                        } else if (bcastType == BcastType.TC) {
                            //成交确认消息
                            long actSeqBuy = ((MtpTrdcnmf) ezeiData.getTopicData()).getTrdCfmBuy().getActnSeqNum();
                            long actSeqSell = ((MtpTrdcnmf) ezeiData.getTopicData()).getTrdCfmSell().getActnSeqNum();
                            SortCacheTool.discardSeq(Math.max(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE);
                            msgSeq = Math.min(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE;
                        } else if (bcastType == BcastType.TO) {
                            //盘后固定价格交易申报
                            msgSeq = ((AtpTo) ezeiData.getTopicData()).getActionSeqNum() % AppConst.SEQ_SCALE;
                        } else if (bcastType == BcastType.TT) {
                            //盘后固定价格交易成交确认
                            long actSeqBuy = ((AtpTt) ezeiData.getTopicData()).getBuyActionSeqNum();
                            long actSeqSell = ((AtpTt) ezeiData.getTopicData()).getSellActionSeqNum();
                            SortCacheTool.discardSeq(Math.max(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE);
                            msgSeq = Math.min(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE;
                        }

                        if (msgSeq > 0) {
                            cacheDatas.get().put(msgSeq, ezeiData.getTopicData());
                            if (msgSeq > localSeq.get())
                                localSeq.set(msgSeq);
                        } else {
                            log.warn("线程{}消费数据类型异常！->{}", Thread.currentThread().getName(), ezeiData.toString());
                        }

                        // 消费完一条数据后，手动提交offset到zookeeper记录
                        //  consumer.commitAsync();  TODO 暂时不提交，便于开发过程中使用数据调试

//                        MonitorPrintUtil.printlnActSeq(ezeiData.getTopicData()); //TODO 调试时打印ezei数据

                    }
                }
            } catch (Exception e) {
                log.error("线程[{}]消费异常：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }
        SortCacheTool.saveRandomDatas(cacheDatas.get());
        cacheDatas.get().clear();

        log.info("monitor-排序和维度注入模块->主题{}本次向Redis写入{}条，耗时{}秒", topicName, perBatchCount, (System.currentTimeMillis() - startTime) / 1000.0);
        return false;
    }

    // 通知对应的线程开始执行eos流程
    public static void makeEzeiEos(String targetTopic) {
        if (eosCount.contains(targetTopic)) {
            // 将eos数据写入redis,通知对应的线程开始执行eos流程
            MtpOrdcnmf eosData = new MtpOrdcnmf();
            eosData.setIsix(0);
            SortCacheTool.putRandomData(localSeq.get() + 1, eosData);
        } else {
            eosCount.put(targetTopic, targetTopic);
        }
    }
}
