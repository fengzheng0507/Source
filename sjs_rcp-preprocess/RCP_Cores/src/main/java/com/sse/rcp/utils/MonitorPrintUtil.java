package com.sse.rcp.utils;


import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorPrintUtil {
    public static void printlnActSeq(TopicData topicData) {
        if (topicData instanceof MtpTrdcnmf) {
            log.info("TC数据： Buy-actSeq={} | Sell-actSeq={}", ((MtpTrdcnmf) ((MtpTrdcnmf) topicData)).getTrdCfmBuy().getActnSeqNum()
                    , ((MtpTrdcnmf) ((MtpTrdcnmf) topicData)).getTrdCfmSell().getActnSeqNum());
        } else if (topicData instanceof MtpOrdcnmf) {
            log.info("OC数据： actSeq={}", ((MtpOrdcnmf) topicData).getActnSeqNum());
        }
    }

    private static ThreadLocal<Long> localCount = new ThreadLocal<>();

    public static void printDelayTimeStamp(TopicData data) {
        if (localCount.get() == null)
            localCount.set(0L);
        long temp = localCount.get() + 1;
        localCount.set(temp);
        if (temp > 2999 && temp % 3000 == 0) {
            log.info("线程{}当前数据延时为:[{}]ns", Thread.currentThread().getName(), System.nanoTime() - data.getDelayTimeStamp());
        }
    }
}
