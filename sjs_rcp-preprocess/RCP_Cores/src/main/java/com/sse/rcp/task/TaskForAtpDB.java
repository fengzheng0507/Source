package com.sse.rcp.task;

import com.sse.rcp.dbsink.ConsumerBookToPostgre;
import com.sse.rcp.dbsink.ConsumerSortedToPostgre;
import com.sse.rcp.dbsink.ConsumerSortedToPostgreForPfp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 综业盘后数据入库
 */
@Component
public class TaskForAtpDB {
    @Value("${kafka.topic.prefix}")
    private String topicPrefix;
    @Value("${kafka.topic.atp.order}")
    private String cnmf;
    @Value("${kafka.topic.atp.book}")
    private String ordBook;
    @Value("#{'${kafka.topic.atp.set}'.split(',')}")
    private List<String> storeSet;
    @Value("${kafka.topic.suffix}")
    private String topicSuffix;
    @Value("${postgre.maxThread}")
    private int postgreThread;

    @Autowired
    ConsumerBookToPostgre clientForOrderBook;

    @Autowired
    ConsumerSortedToPostgreForPfp clientForSorted;

    public void realtimeInputDB() {
        int size = (int) Math.ceil(storeSet.size() * 1.0 / postgreThread);
        for (int i = 0; i < postgreThread; i++) {
            List<String> ordTopics = new ArrayList<>();
            List<String> cnmfTopics = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                int idx = i * size + j;
                if (idx >= storeSet.size()) {
                    break;
                }
                ordTopics.add(topicPrefix + ordBook + storeSet.get(idx) + topicSuffix);
                cnmfTopics.add(topicPrefix + cnmf + storeSet.get(idx) + topicSuffix);
            }
            if(ordTopics.size()>0){
                clientForOrderBook.consumeDataToDB(ordTopics);
                clientForSorted.consumeDataToDB(cnmfTopics);
            }
        }
    }
}
