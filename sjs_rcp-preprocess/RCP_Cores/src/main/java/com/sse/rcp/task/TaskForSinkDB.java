package com.sse.rcp.task;

import com.sse.rcp.dbsink.ConsumerBookToPostgre;
import com.sse.rcp.dbsink.ConsumerSortedToPostgre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskForSinkDB {
    @Value("${kafka.topic.prefix}")
    private String topicPrefix;
    @Value("${kafka.topic.book}")
    private String ordBook;
    @Value("${kafka.topic.order}")
    private String cnmf;
    @Value("#{'${kafka.topic.sinkset}'.split(',')}")
    private List<String> needSortSet;
    @Value("${kafka.topic.suffix}")
    private String topicSuffix;
    @Value("${postgre.maxThread}")
    private int postgreThread;

    @Autowired
    ConsumerBookToPostgre clientForOrderBook;

    @Autowired
    ConsumerSortedToPostgre clientForSorted;

    /**
     * 一个线程负责多个set数据入库
     */
    public void realtimeInputDB() {
        int size = (int) Math.ceil(needSortSet.size() * 1.0 / postgreThread);

        for (int i = 0; i < postgreThread; i++) {
            List<String> ordTopics = new ArrayList<>();
            List<String> cnmfTopics = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                int idx = i * size + j;
                if (idx >= needSortSet.size()) {
                    break;
                }
                ordTopics.add(topicPrefix + ordBook + needSortSet.get(idx) + topicSuffix);
                cnmfTopics.add(topicPrefix + cnmf + needSortSet.get(idx) + topicSuffix);
            }
            if(cnmfTopics.size()>0){
                clientForOrderBook.consumeDataToDB(ordTopics);
                clientForSorted.consumeDataToDB(cnmfTopics);
            }

        }
    }
}
