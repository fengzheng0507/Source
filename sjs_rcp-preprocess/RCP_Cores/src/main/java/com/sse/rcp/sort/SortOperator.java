package com.sse.rcp.sort;

import com.sse.rcp.config.KafkaConsumerConfig;
import com.sse.rcp.domains.constant.AppConst;
import com.sse.rcp.domains.constant.TopicConstant;
import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.kafka.KafkaProducerTool;
import com.sse.rcp.utils.InitUtil;
import com.sse.rcp.utils.MonitorPrintUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SortOperator {
    @Value("${lua.poll.maxSize}")
    private String maxSize;

    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;

    @Autowired
    private ComplementOcByTc complementMtpOc;

    private static final String THREAD_PREFIX = "SortOpThread-";

    private static final ThreadLocal<Long> LOCAL_SEQ = new ThreadLocal<>();

    private static final ThreadLocal<Long> LOCAL_ORDER_NO = new ThreadLocal<>();

    private static ThreadLocal<Boolean> LOCAL_EOS = new ThreadLocal<>();


    @Async("taskExecutor")
//    @NeedDistributedLock
    public void sortOperate(String targetTopic) {
        Thread.currentThread().setName(THREAD_PREFIX + targetTopic);
        // 初始化seq和orderNum的起始值
        boolean notInitEos = InitUtil.initRecords(targetTopic,
                seq -> {
                    if (LOCAL_SEQ.get() == null) {
                        if (seq > 0) {
                            LOCAL_SEQ.set(seq);
                        }
                        // 如果下游topic里的数据不是当天的，则当天还没有数据，用缺省seq初始
                        else {
                            LOCAL_SEQ.set(getSetLocalInitSeq(targetTopic));
                        }
                    }

                },
                LOCAL_ORDER_NO::set);

        LOCAL_EOS.set(false);
        while (notInitEos && !LOCAL_EOS.get()) {
            long startTime = System.currentTimeMillis();
            List<? extends TopicData> datas = new ArrayList<>();
            try {
                datas = SortCacheTool.getRandomList(LOCAL_SEQ.get() + 1L, maxSize);
            } catch (Exception e) {
                datas = new ArrayList<>();
                log.error("线程[{}]调用lua脚本异常：[{}]->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            }

            long getDatas = System.currentTimeMillis();
            if (!datas.isEmpty())
                log.info("monitor-排序和维度注入模块->{}线程本次从redis读取{}条数据，耗时{}秒", Thread.currentThread().getName(), datas.size(), (getDatas - startTime) / 1000.0);

            datas.stream().forEach(orderedData -> {
                if (orderedData.eos()) {
                    LOCAL_EOS.set(true);
                } else {
                    // 先推送补订单补的oc
                    KafkaProducerTool.pushDataToKafka(targetTopic, complementMtpOc.addOrder(orderedData));
                    // 推送当前ezei数据
                    KafkaProducerTool.pushDataToKafka(targetTopic, orderedData);

                    MonitorPrintUtil.printDelayTimeStamp(orderedData); //TODO
                }
            });
            if (!datas.isEmpty()) {
                LOCAL_SEQ.set(Math.max(LOCAL_SEQ.get(), getActSeq(datas.get(datas.size() - 1))) % AppConst.SEQ_SCALE);
                log.info("monitor-排序和维度注入模块->本次处理向{}主题写入{}条数据，共耗时{}秒", targetTopic, datas.size(), (System.currentTimeMillis() - startTime) / 1000.0);
            }
        }
        // 推送eos信
        KafkaProducerTool.pushSetEocData(targetTopic);
    }


    // 获取当前消费线程记录的订单编号getLocalOrderNo
    public static long getLocalOrderNo() {
        return SortOperator.LOCAL_ORDER_NO.get();
    }

    // 更新当前消费线程记录的订单编号
    public static void updateLocalOrderNo(long orderNo) {
        SortOperator.LOCAL_ORDER_NO.set(orderNo);
    }

    public static long getActSeq(TopicData data) {
        long msgSeq = -1;
        if (data instanceof MtpOrdcnmf) {
            //订单确认消息
            msgSeq = ((MtpOrdcnmf) data).getActnSeqNum() % AppConst.SEQ_SCALE;
        } else if (data instanceof MtpTrdcnmf) {
            //成交确认消息
            long actSeqBuy = ((MtpTrdcnmf) data).getTrdCfmBuy().getActnSeqNum();
            long actSeqSell = ((MtpTrdcnmf) data).getTrdCfmSell().getActnSeqNum();
            SortCacheTool.discardSeq(Math.max(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE);
            msgSeq = Math.min(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE;
        } else if (data instanceof AtpTo) {
            //盘后固定价格交易申报
            msgSeq = ((AtpTo) data).getActionSeqNum() % AppConst.SEQ_SCALE;
        } else if (data instanceof AtpTt) {
            //盘后固定价格交易成交确认
            long actSeqBuy = ((AtpTt) data).getBuyActionSeqNum();
            long actSeqSell = ((AtpTt) data).getSellActionSeqNum();
            SortCacheTool.discardSeq(Math.max(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE);
            msgSeq = Math.min(actSeqBuy, actSeqSell) % AppConst.SEQ_SCALE;
        }
        return msgSeq;
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
