package com.sse.rcp.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

public class KafkaClientForMockJson {
    public static Producer<String, String> config() {
//    public static  Producer<String, String> configOC() {
        //构建PROP对象，管理生产者的配置
        Properties props = new Properties();
        //指定连接的Kafka的地址
        props.put("bootstrap.servers", "10.112.103.2:9092,10.112.103.3:9092,10.112.103.4:9092");
        /**
         * 指定生产者给Kafka发送消息的模式
         * 0-生产者不管kafka是否收到，都直接发送下一条，快，但是数据易丢失
         * 1-生产者发送一条数据到Topic的分区中，只要写入了leader分区，就返回一个ack给生产者，继续发送下一条
         * all-生产者发送一条数据到Topic的分区中，Topic必须保证所有分区副本都同步成功了 ，继续发送下一条，最安全，最慢
         */
        props.put("acks", "all");
        //如果发送失败，重试的次数
        props.put("retries", 0);
        //每次从缓存中发送的批次的大小
        props.put("batch.size", 16384);  //   16384  32768  65536
        //获取元数据的阻塞时间
//        props.put("max.block.ms",1000);
        //间隔时间
        props.put("linger.ms", 200);
        // producer 收到服务器响应前可以发送消息的个数
//        props.put("max.in.flight.requests.per.connection",10) ; // 可以是1
        // 设定两次重试之间时间间隔
//        props.put("retry.backoff.ms",50);
        //生产数据的 缓存，默认32MB  33554432
        props.put("buffer.memory", 33554432);  // 2911548
        //序列化机制：Kafka也是以KV形式进行数据存储，K可以没有，写入的数据是Value
//        props.put("value.serializer", "KafkaProducer.domains.MtpOrdcnmfSerializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String,String> producer = new KafkaProducer<>(props);
        return producer ;
    }
}