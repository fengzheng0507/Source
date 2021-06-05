package com.sse.rcp.task;

import com.sse.rcp.sort.ConsumerForEzei;
import com.sse.rcp.sort.SortOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 综业数据排序
 */
@Component
public class TaskForATPSort {
    @Value("#{'${kafka.topic.atp.set}'.split(',')}")
    private List<String> needSortSet;
    @Value("#{'${kafka.topic.atp.needsort}'.split(',')}")
    private List<String> atpNeedSortTopics;
    @Value("${kafka.topic.atp.order}")
    private String atpSortedTopic;
    @Value("${kafka.topic.suffix}")
    private String suffix;


    @Value("${kafka.topic.prefix.raw}")
    private String kafkaPrefixEzei;

    @Value("${kafka.topic.prefix}")
    private String kafkaPrefix;

    @Autowired
    ConsumerForEzei consumer;

    @Autowired
    SortOperator sortOperator;

    public void sortAndInject() {
        for (String set : needSortSet) {
            // 开启排序
            String targetTopic = kafkaPrefix + atpSortedTopic + set + suffix;
            sortOperator.sortOperate(targetTopic);

            // 开启kafka consumer
            for (String needSortTopic : atpNeedSortTopics) {
                String topic = kafkaPrefixEzei + needSortTopic + set;
                consumer.consumeData(topic, targetTopic);
            }

        }
    }
}
