package com.sse.rcp.task;

import com.sse.rcp.sort.ConsumerForEzei;
import com.sse.rcp.sort.SortOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskForSort {
    @Value("#{'${kafka.topic.mtp.needSortSet}'.split(',')}")
    private List<String> needSortSet;
    @Value("#{'${kafka.topic.mtp.needsort}'.split(',')}")
    private List<String> mtpNeedSortTopics;
    @Value("${kafka.topic.order}")
    private String mtpSortedTopic;
    @Value("${kafka.topic.suffix}")
    private String suffix;


//    @Value("#{'${kafka.topic.mtp.noneSortSet}'.split(',')}")
//    private List<String> noneSortSet;
//    @Value("#{'${kafka.topic.mtp.nonesort}'.split(',')}")
//    private List<String> mtpNoneSortTopics;

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
            // 开启业务消费线程
            String targetTopic = kafkaPrefix + mtpSortedTopic + set + suffix;
            sortOperator.sortOperate(targetTopic);

            // 开启kafka consumer
            for (String needSortTopic : mtpNeedSortTopics) {
                String topic = kafkaPrefixEzei + needSortTopic + set;
                consumer.consumeData(topic, targetTopic);
            }

        }
    }
}
