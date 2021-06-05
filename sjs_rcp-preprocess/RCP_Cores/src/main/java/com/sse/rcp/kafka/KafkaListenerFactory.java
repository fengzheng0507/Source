package com.sse.rcp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;

@Slf4j
public class KafkaListenerFactory {
    private static ThreadLocal<ConsumerRebalanceListener> localListener = new ThreadLocal<>();

    public  static ConsumerRebalanceListener getConsumerRebalanceListener(Consumer<byte[], byte[]> consumer){
        if(localListener.get() == null){
            ConsumerRebalanceListener consumerRebalanceListener = new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    log.info("onPartitionsRevoked: {}", partitions);
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    log.info("onPartitionsAssigned: {}", partitions);
                    // 在没有提交offset的情况下，从分区的最早记录开始读取  TODO
                    consumer.seekToBeginning(partitions);
                }
            };
            localListener.set(consumerRebalanceListener);
        }
        return localListener.get();
    }
}
