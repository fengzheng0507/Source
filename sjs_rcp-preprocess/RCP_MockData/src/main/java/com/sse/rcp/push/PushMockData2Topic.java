package com.sse.rcp.push;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.kafka.KafkaClientForMockByte;
import com.sse.rcp.mock.MockData;
import com.sse.rcp.parse.ParseMockDataFromCsv;
import com.sse.rcp.serialize.MtpOcSerialize;
import com.sse.rcp.serialize.MtpTcSerialize;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class PushMockData2Topic {

    private static AtomicLong seq = new AtomicLong(3590004000000001L);

    @Value("${kafka.mock.topic}")
    private String targetTopic;
    @Value("${BcastType.mock.OC}")
    private String OC;
    @Value("${BcastType.mock.TC}")
    private String TC;


    @Autowired
    private ParseMockDataFromCsv parseMockDataFromCsv;

    public void pushCSVData2Topic() {
        Producer<byte[], byte[]> producer = KafkaClientForMockByte.config();
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(KafkaConstant.TOPIC, targetTopic.getBytes());

        Iterator<Map.Entry<Long, Object>> iterator = ParseMockDataFromCsv.MockDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Object> entry = iterator.next();
            mockCsvData2Topic(entry.getValue(), producer, recordHeaders);
        }
        producer.close();
    }

    public void pushByteData2Topic() {
        Producer<byte[], byte[]> producer = KafkaClientForMockByte.config();
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(KafkaConstant.TOPIC, targetTopic.getBytes());

        while (true) {

            // 模拟 OC 数据
            for (int i = 0; i < Math.random() * 30 + 30; i++) {
                producer.send(new ProducerRecord<byte[], byte[]>(targetTopic,
                                null,
                                null,
                                MtpOcSerialize.serialize(MockData.generateOCData(seq)),
                                recordHeaders.add(KafkaConstant.BCAST_TYPE, OC.getBytes())
                        )
                );
                seq.incrementAndGet();
            }

            // 模拟 TC 数据
            producer.send(new ProducerRecord<byte[], byte[]>(targetTopic,
                            null,
                            null,
                            MtpTcSerialize.serialize(MockData.generateTCData(seq)),
                            recordHeaders.add(KafkaConstant.BCAST_TYPE, TC.getBytes())
                    )
            );
            seq.incrementAndGet();
        }
    }

    public void mtpOrdcnmf2Topic(MtpOrdcnmf MtpOrdcnmf) {
        Producer<byte[], byte[]> producer = KafkaClientForMockByte.config();
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(KafkaConstant.TOPIC, targetTopic.getBytes());
        // 模拟 OC 数据
        producer.send(new ProducerRecord<byte[], byte[]>(targetTopic,
                        null,
                        null,
                        MtpOcSerialize.serialize(MtpOrdcnmf),
                        recordHeaders.add(KafkaConstant.BCAST_TYPE, OC.getBytes())
                )
        );
        seq.incrementAndGet();
    }

    public void mtpTrdcnmf2Topic(MtpTrdcnmf mtpTrdcnmf) {
        Producer<byte[], byte[]> producer = KafkaClientForMockByte.config();
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(KafkaConstant.TOPIC, targetTopic.getBytes());
        // 模拟 TC 数据
        producer.send(new ProducerRecord<byte[], byte[]>(targetTopic,
                        null,
                        null,
                        MtpTcSerialize.serialize(mtpTrdcnmf),
                        recordHeaders.add(KafkaConstant.BCAST_TYPE, TC.getBytes())
                )
        );
        seq.incrementAndGet();
    }

    public void mockCsvData2Topic(Object object, Producer<byte[], byte[]> producer, RecordHeaders recordHeaders) {
        if (object instanceof MtpOrdcnmf) {
            producer.send(new ProducerRecord<byte[], byte[]>(targetTopic,
                            null,
                            null,
                            MtpOcSerialize.serialize((MtpOrdcnmf) object),
                            recordHeaders.add(KafkaConstant.BCAST_TYPE, OC.getBytes())
                    )
            );
        } else if (object instanceof MtpTrdcnmf) {
            producer.send(new ProducerRecord<byte[], byte[]>(targetTopic,
                            null,
                            null,
                            MtpTcSerialize.serialize((MtpTrdcnmf) object),
                            recordHeaders.add(KafkaConstant.BCAST_TYPE, TC.getBytes())
                    )
            );
        }
    }
}
