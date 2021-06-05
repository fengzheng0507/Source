package com.sse.rcp.kafka;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.ezei.mtp.MtpNc;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpPn;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.order.OrderBookRecord;
import com.sse.rcp.utils.JacksonSerializerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
@Component
public class KafkaProducerTool implements EnvironmentAware {
    private KafkaProducerTool() {
    }

    private static volatile Properties properties;

    private static ThreadLocal<KafkaProducer> threadLocalProducer = new ThreadLocal<>();

    // 数据写入kafka
    public static void pushDataToKafka(String targetTopic, Object data) {
        if (data == null)
            return;
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(KafkaConstant.HEADER_DATA_TYPE, getDataType(data).getBytes());
        recordHeaders.add(KafkaConstant.HEADER_EOS, null);

        try {
            getLocalProducer().send(new ProducerRecord<>(targetTopic, null, System.currentTimeMillis(), null,
                            JacksonSerializerUtil.serializerWithType(data), recordHeaders),
                    new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            KafkaProducerTool.producerPostProcess(targetTopic, data, metadata, exception);
                        }
                    });
        } catch (Exception e) {
            log.error("{}线程推送数据{}到kafka异常：{}->{}", Thread.currentThread().getName(), data.toString(), e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }

    // 发送收盘消息
    public static void pushSetEocData(String targetTopic) {
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(KafkaConstant.HEADER_EOS, LocalDate.now(ZoneId.systemDefault()).toString().getBytes());
        KafkaProducer producer;
        producer = getLocalProducer();
        try {
            producer.send(new ProducerRecord<>(targetTopic, null, System.currentTimeMillis(), null,
                            JacksonSerializerUtil.serializerWithType(new TopicData() {
                                @Override
                                public boolean eos() {
                                    return true;
                                }

                                @Override
                                public String dataType() {
                                    return "eos";
                                }

                                @Override
                                public long getDelayTimeStamp() {
                                    return 0;
                                }
                            }), recordHeaders),
                    new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            KafkaProducerTool.producerPostProcess(targetTopic, KafkaConstant.HEADER_EOS, metadata, exception);
                        }
                    });
        } catch (Exception e) {
            log.error("{}线程推送eos信息到kafka异常：{}->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));

        }
    }

    // 关闭线程producer
    public static void closeLocalProducer() {
        if (threadLocalProducer.get() != null) {
            threadLocalProducer.get().close();
            log.info("本次push完成，关闭[{}]线程的kafka Producer", Thread.currentThread().getName());
        }
    }

    /**
     * producer push 的回调
     *
     * @param targetTopic push的目标topic
     * @param data        push 的数据
     * @param metadata    metadata
     * @param exception   push的exception
     */
    public static void producerPostProcess(String targetTopic, Object data, RecordMetadata metadata, Exception exception) {
        // 推送失败，数据怎么处理？  TODO
        if (exception != null) {
            Exception trace = new Exception();
            log.error("kafka push error:{}!--->targetTopic:[{}]--->targetData:[{}]--->errorStackTrace:[{}]", exception.getMessage(), targetTopic, data.toString(), Arrays.toString(trace.getStackTrace()));
        }
    }

    /**
     * 获取kafka header中的参数
     *
     * @param headers kafka header
     * @param key     参数的key
     * @return 参数的value
     */
    public static String getHeaderValue(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        return header != null && header.value() != null ? new String(header.value(), StandardCharsets.UTF_8) : "";
    }

    /* 创建 Kafka 生产者对象 - 线程本地*/
    public static KafkaProducer getLocalProducer() {
        if (threadLocalProducer.get() == null) {
            threadLocalProducer.set(new KafkaProducer(properties));
        }
        return threadLocalProducer.get();
    }

    public static String getDataType(Object data) {
        if (data instanceof MtpOrdcnmf) {
            return KafkaConstant.DATA_TYPE_OC;
        } else if (data instanceof MtpTrdcnmf) {
            return KafkaConstant.DATA_TYPE_TC;
        } else if (data instanceof MtpPn) {
            return KafkaConstant.DATA_TYPE_PN;
        } else if (data instanceof MtpNc) {
            return KafkaConstant.DATA_TYPE_NC;
        } else if (data instanceof AtpTt) {
            return KafkaConstant.DATA_TYPE_TT;
        } else if (data instanceof AtpTo) {
            return KafkaConstant.DATA_TYPE_TO;
        } else if (data instanceof OrderBookRecord) {
            return KafkaConstant.DATA_TYPE_BOOK;
        }
        return null;
    }

    public static boolean isTodayEos(String eosTag) {
        return LocalDate.now(ZoneId.systemDefault()).toString().equalsIgnoreCase(eosTag);
    }

    /**
     * Set the {@code Environment} that this component runs in.
     *
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        if (properties == null) {
            synchronized (KafkaProducerTool.class) {
                if (properties == null) {
                    properties = new Properties();
                    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("kafka.producer.brokers"));
                    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, environment.getProperty("kafka.producer.keySerializerClass"));
                    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, environment.getProperty("kafka.producer.valueSerializerClass"));
                    properties.put(ProducerConfig.ACKS_CONFIG, environment.getProperty("kafka.producer.acks"));
                    properties.put(ProducerConfig.RETRIES_CONFIG, environment.getProperty("kafka.producer.retries"));

                    properties.put(ProducerConfig.LINGER_MS_CONFIG, environment.getProperty("kafka.producer.lingerMs"));
                    properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, environment.getProperty("kafka.producer.bufferMemory"));
                    properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, environment.getProperty("kafka.producer.maxInFlightsRequestsPerSession"));
                    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, environment.getProperty("kafka.producer.batchSize"));
                    properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, environment.getProperty("kafka.producer.requestTimeout"));
                    properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, environment.getProperty("kafka.producer.maxBlockMs"));
                    properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, environment.getProperty("kafka.producer.maxRequestSize"));
//                    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, environment.getProperty("kafka.producer.compressionType"));
                }
            }
        }

    }
}
