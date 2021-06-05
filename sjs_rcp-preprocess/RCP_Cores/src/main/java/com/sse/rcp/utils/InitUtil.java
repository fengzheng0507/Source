package com.sse.rcp.utils;

import com.alibaba.fastjson.JSON;
import com.sse.rcp.config.KafkaConsumerConfig;
import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.constant.TopicConstant;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.kafka.KafkaProducerTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.LongConsumer;

@Slf4j
@Component
public class InitUtil {
    private static final int INIT_PULL_SIZE = 3;

    private static KafkaConsumerConfig kafkaConsumerConfig;

    @Autowired(required = true)
    @Qualifier("kafkaConsumerConfig")
    private void setConfig(KafkaConsumerConfig config) {
        kafkaConsumerConfig = config;
    }

    // 消费线程启动时，从下游kafka拉取最新的Seq作为初始值，防止重复消费
    public static boolean initRecords(String targetTopic, LongConsumer doInitSeq, LongConsumer doInitOrder) {
        // 初始化actSeq
        Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(kafkaConsumerConfig.getKafkaPropertiesForInit(targetTopic));
        try {
            // 订阅主题
            consumer.subscribe(Arrays.asList(targetTopic), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    long lastOffset = -1;
                    for (TopicPartition topicPartition : partitions) {
                        lastOffset = consumer.position(topicPartition);
                        // 如果有最新offset，则拉取最后一条数据
                        if (lastOffset > 0) {
                            consumer.seek(topicPartition, lastOffset - INIT_PULL_SIZE);
                        } else {
                            consumer.seekToEnd(partitions);
                        }
                        break;
                    }
                }
            });

            ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(kafkaConsumerConfig.getPollDuration()));

            long initSeq = 0L, initNum = 0L;

            if (records == null || records.count() < 1) {
                log.warn("[{}]线程初始化时未从sorted拉取到数据！", Thread.currentThread().getName());
                doInitSeq.accept(initSeq);
                if (doInitOrder != null)
                    doInitSeq.accept(initNum);
                return true;
            }

            for (ConsumerRecord<byte[], byte[]> consumerRecord : records) {
                if (consumerRecord.headers().lastHeader(KafkaConstant.HEADER_EOS) != null
                        || consumerRecord.headers().lastHeader(KafkaConstant.HEADER_DATA_TYPE) == null) {

                    // 消费线程启动时，拉取到下游EOS,应该判断是否是当天的EOS
                    String headerEos = KafkaProducerTool.getHeaderValue(consumerRecord.headers(), KafkaConstant.HEADER_EOS);
                    if (KafkaProducerTool.isTodayEos(headerEos)) {
//                        LOCAL_EOS.set(true);
//                        return false;
                        continue;  //TODO 为了调试，先不return
                    }
                    continue;
                }
                String dataType = new String(consumerRecord.headers().lastHeader(KafkaConstant.HEADER_DATA_TYPE).value()
                        , StandardCharsets.UTF_8);


                switch (dataType) {
                    case KafkaConstant.DATA_TYPE_OC:
                        MtpOrdcnmf ocData = JSON.parseObject(JSON.parse(consumerRecord.value()).toString(), MtpOrdcnmf.class);
                        // 如果有当天的数据，则应当严格按照下游接收的seq来确定排序的初始值
                        if (InitUtil.dataNotExpired(ocData.getTranDatTim())) {
                            if (ocData.getActnSeqNum() > initSeq) {
                                initSeq = ocData.getActnSeqNum();
                                initNum = ocData.getOrdrNum();
                            }
                        }
                        break;
                    case KafkaConstant.DATA_TYPE_TC:
                        MtpTrdcnmf tcData = JSON.parseObject(JSON.parse(consumerRecord.value()).toString(), MtpTrdcnmf.class);
                        // 如果有当天的数据，则应当严格按照下游接收的seq来确定排序的初始值
                        if (InitUtil.dataNotExpired(tcData.getTrdCfmBuy().getTranDatTim())) {
                            long seq = Math.min(tcData.getTrdCfmSell().getActnSeqNum(), tcData.getTrdCfmBuy().getActnSeqNum());
                            if (seq > initSeq) {
                                initSeq = seq;
                                initNum = tcData.getTrdCfmBuy().getOrdrNo();
                            }
                        }
                        break;
                    case KafkaConstant.DATA_TYPE_TO:
                        AtpTo ocAtpData = JSON.parseObject(JSON.parse(consumerRecord.value()).toString(), AtpTo.class);
                        if (InitUtil.dataExpiredStr(ocAtpData.getOrdDate())) {
                            if (ocAtpData.getActionSeqNum() > initSeq) {
                                initSeq = ocAtpData.getActionSeqNum();
                                initNum = ocAtpData.getOrdrNo();
                            }
                        }
                        break;
                    case KafkaConstant.DATA_TYPE_TT:
                        AtpTt tcAtpData = JSON.parseObject(JSON.parse(consumerRecord.value()).toString(), AtpTt.class);
                        if (InitUtil.dataExpiredStr(tcAtpData.getTradeDate())) {
                            long num = Math.min(tcAtpData.getBuyNGTSOrderNo(), tcAtpData.getSellNGTSOrderNo());
                            if (tcAtpData.getTradeSeqNum() > initSeq) {
                                initSeq = tcAtpData.getTradeSeqNum();
                                initNum = num;
                            }
                        }
                        break;
                    default:
                        log.error("[{}]初始化异常！->{}", Thread.currentThread().getName(), JSON.parse(consumerRecord.value()));
                }
            }
            doInitSeq.accept(initSeq);
            if (doInitOrder != null)
                doInitSeq.accept(initNum);
            return true;
        } catch (Exception e) {
            log.warn("[{}]初始化异常:{}->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.asList(e.getStackTrace()));
            doInitSeq.accept(0L);
            if (doInitOrder != null)
                doInitSeq.accept(0L);
        } finally {
            consumer.close();
        }
        return true;
    }

    // 判断当前时间是否是今天的
    private static boolean dataNotExpired(long tranDatTim) {
        long currentData = Long.parseLong(LocalDate.now().toString().replace("-", ""));
        return tranDatTim / 100_000_000 == currentData;
    }

    // 判断当前时间是否是今天的
    private static boolean dataExpiredStr(String tranDat) {
        long currentData = Long.parseLong(LocalDate.now().toString().replace("-", ""));
        return Long.valueOf(tranDat) < currentData;
    }

    // 根据消费线程对应的下游topic，得到当前消费线程的初始化本地seq
    public static long getSetLocalInitSeq(String orderTopic) {
        // example : rcps-sse.mtp.cnmf.0991.sorted
        String[] strs = orderTopic.split("\\.");
        switch (strs[3]) {
            case (TopicConstant.SET_NO_0001):
                return TopicConstant.SET_INIT_0001;

            case (TopicConstant.SET_NO_0002):
                return TopicConstant.SET_INIT_0002;

            case (TopicConstant.SET_NO_0003):
                return TopicConstant.SET_INIT_0003;

            case (TopicConstant.SET_NO_0004):
                return TopicConstant.SET_INIT_0004;

            case (TopicConstant.SET_NO_0005):
                return TopicConstant.SET_INIT_0005;

            case (TopicConstant.SET_NO_0006):
                return TopicConstant.SET_INIT_0006;

            case (TopicConstant.SET_NO_0020):
                return TopicConstant.SET_INIT_0020;

            case (TopicConstant.SET_NO_0991):
                return TopicConstant.SET_INIT_0991;
            case (TopicConstant.SET_NO_0103):
                return TopicConstant.SET_INIT_0103;
            default:
                log.error("[{}]没有拿到对应的initSeq!", Thread.currentThread().getName());
                return -1L;
        }
    }
}
