package com.sse.rcp.task;

import com.sse.rcp.order.ConsumerForOrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskForOrderBook {
    @Value("#{'${kafka.topic.order}'.split(',')}")
    private List<String> orderBookSet;
    @Value("#{'${kafka.topic.mtp.needSortSet}'.split(',')}")
    private List<String> needSortSet;
    @Value("${kafka.topic.prefix}")
    private String kafkaPrefix;
    @Value("${kafka.topic.suffix}")
    private String kafkaSuffix;

    @Autowired
    ConsumerForOrderBook client;

    public void orderBook() {
        for (String set : needSortSet) {
            for (String orderBookTopic : orderBookSet) {
                String topic = kafkaPrefix + orderBookTopic + set + kafkaSuffix;
                client.consumeData(topic);
            }
        }
    }
}
