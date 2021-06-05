package com.sse.rcp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.Properties;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "kafka.consume")
public class KafkaConsumerConfig {
    private static final String INIT_TAG = "-Init";

    private String brokers;
    private String clientGroupId;
    private String keySerializerClass;
    private String valueSerializerClass;
    private boolean autoCommit;
    private int maxPartitionFetchBytes;
    private int fetchMaxBytes;
    private int pollDuration;
    private int maxPollRecords;
    private int maxPollEzeiRecords;
    private int maxPollIntervalMs;
    private int heartbeatIntervalMs;
    private int sessionTimeoutMs;

    private static volatile Properties rawProps;

    public Properties getKafkaProperties(String topicName) {
        Properties currentProps = new Properties();
        currentProps.putAll(getRawProp());
        currentProps.put(ConsumerConfig.GROUP_ID_CONFIG, clientGroupId + "-" + topicName);
        currentProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        devConfigTag(currentProps); // TODO 开发时防止groupId 冲突, 正式发布前删除

        return currentProps;
    }

    public Properties getKafkaPropertiesForInit(String topicName) {
        Properties currentProps = new Properties();
        currentProps.putAll(getRawProp());
        currentProps.put(ConsumerConfig.GROUP_ID_CONFIG, clientGroupId + "-" + topicName + INIT_TAG);
        currentProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 3);

        devConfigTag(currentProps); // TODO 开发时防止groupId 冲突, 正式发布前删除

        return currentProps;
    }

    public Properties getKafkaPropertiesForEzei(String topicName) {
        Properties currentProps = new Properties();
        currentProps.putAll(getRawProp());
        currentProps.put(ConsumerConfig.GROUP_ID_CONFIG, clientGroupId + "-" + topicName);
        currentProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollEzeiRecords);

        devConfigTag(currentProps); // TODO 开发时防止groupId 冲突, 正式发布前删除
        return currentProps;
    }


    public Properties getKafkaPropertiesForDB(String threadName) {
        Properties currentProps = new Properties();
        currentProps.putAll(getRawProp());
        currentProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        currentProps.put(ConsumerConfig.GROUP_ID_CONFIG, clientGroupId + "-" + threadName);

        devConfigTag(currentProps); // TODO 开发时防止groupId 冲突, 正式发布前删除
        return currentProps;
    }

    private Properties getRawProp() {
        if (null == rawProps) {
            synchronized (KafkaConsumerConfig.class) {
                if (null == rawProps) {
                    rawProps = new Properties();
                    rawProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
                    rawProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keySerializerClass);
                    rawProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueSerializerClass);
                    rawProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
                    rawProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
                    rawProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetchMaxBytes);
                    rawProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
                    rawProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, fetchMaxBytes);
                    rawProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);
                }
            }
        }
        return rawProps;
    }

    // TODO 开发时防止groupId 冲突, 正式发布前删除
    private static void devConfigTag(Properties currentProps) {
        try {
            currentProps.put(ConsumerConfig.GROUP_ID_CONFIG, currentProps.getProperty(ConsumerConfig.GROUP_ID_CONFIG)
                    + "-" + InetAddress.getLocalHost().getHostName()
                    + "-" + LocalTime.now());
        } catch (UnknownHostException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
