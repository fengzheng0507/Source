package com.sse.rcp.domains.ezei;

import com.sse.rcp.domains.constant.TopicConstant;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public enum EzEITopics {
    SINGLETON;

    private Map<String, EzEITopic> byTopic = new LinkedHashMap<>();
    private Map<BcastType, Map<String, EzEITopic>> byBcastSet = new LinkedHashMap<>();
    private LinkedList<String> topics = null ;


    EzEITopics() {
        add(new EzEITopic(TradePlatform.MTP, "0001", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0001));
        add(new EzEITopic(TradePlatform.MTP, "0001", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0001));
        add(new EzEITopic(TradePlatform.MTP, "0002", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0002));
        add(new EzEITopic(TradePlatform.MTP, "0002", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0002));
        add(new EzEITopic(TradePlatform.MTP, "0003", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0003));
        add(new EzEITopic(TradePlatform.MTP, "0003", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0003));
        add(new EzEITopic(TradePlatform.MTP, "0004", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0004));
        add(new EzEITopic(TradePlatform.MTP, "0004", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0004));
        add(new EzEITopic(TradePlatform.MTP, "0005", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0005));
        add(new EzEITopic(TradePlatform.MTP, "0005", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0005));
        add(new EzEITopic(TradePlatform.MTP, "0006", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0006));
        add(new EzEITopic(TradePlatform.MTP, "0006", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0006));
        add(new EzEITopic(TradePlatform.MTP, "0020", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0020));
        add(new EzEITopic(TradePlatform.MTP, "0020", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0020));
        add(new EzEITopic(TradePlatform.MTP, "0991", BcastType.OC, TopicConstant.EZEI_SSE_MTP_ORDCNMF_0991));
        add(new EzEITopic(TradePlatform.MTP, "0991", BcastType.TC, TopicConstant.EZEI_SSE_MTP_TRDCNMF_0991));
        add(new EzEITopic(TradePlatform.MTP, "0991", BcastType.NC, TopicConstant.EZEI_SSE_MTP_NONTRAD_0991));
        add(new EzEITopic(TradePlatform.MTP, "0991", BcastType.PN, TopicConstant.EZEI_SSE_MTP_PRVNEWS_0991));
        add(new EzEITopic(TradePlatform.ATP, "0103", BcastType.TO, TopicConstant.EZEI_SSE_ATP_TO_0103));
        add(new EzEITopic(TradePlatform.ATP, "0103", BcastType.TT, TopicConstant.EZEI_SSE_ATP_TT_0103));
    }

    private void add(EzEITopic e) {
        byTopic.put(e.getTopic(), e);
        Map<String, EzEITopic> m = byBcastSet.get(e.getBcastType());
        if (m == null) {
            m = new LinkedHashMap<>();
            byBcastSet.put(e.getBcastType(), m);
        }
//        String topic = e.getTopic();
//        topics.add( e.getTopic());

    }

    public EzEITopic get(BcastType bcastType, String setNo) {
        Map<String, EzEITopic> m = byBcastSet.get(bcastType);
        return m == null ? null : m.get(setNo);
    }

    public EzEITopic get(String topic) {
        return byTopic.get(topic);
    }

}
