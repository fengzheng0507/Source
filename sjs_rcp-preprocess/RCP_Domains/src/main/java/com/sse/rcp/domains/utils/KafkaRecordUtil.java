package com.sse.rcp.domains.utils;

import com.sse.rcp.domains.ezei.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class KafkaRecordUtil {
    /**
     * 将Kafka读取出的byte数组转换为对象结构
     */
    public static List<BcastMsg> decode(byte[] bytes) {
        List<BcastMsg> rs = new LinkedList<>();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        while (bb.hasRemaining()) {
            BcastMsg r = new BcastMsg();
            BcastHeader bcstHdr = r.getBcastHeader();
            bcstHdr.setTopic(ByteUtil.readCharSequence(bb, 32).trim());
            bcstHdr.setRecCount(bb.getInt());
            bcstHdr.setRecLen(bb.getShort());
            EzEITopic topic = EzEITopics.SINGLETON.get(bcstHdr.getTopic());
            for (int i = 0; i < bcstHdr.getRecCount(); i++) {
                EzEIData td = new EzEIData();
                TopicHeader topicHdr = td.getTopicHdr();
                topicHdr.setRecSeq(bb.getInt());
                topicHdr.setExist(bb.get());
                byte[] topicData = new byte[bcstHdr.getRecLen()];
                bb.get(topicData);
                td.setTopicData(DomainsDecodeUtil.decode(topicData, topic.getBcastType()));
                r.getData().add(td);
            }
            rs.add(r);
        }
        return rs;
    }
}
