package kafkaTest;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @ClassName KafkaProducerClient
 * @Description
 * @Date
 * @Create By huym
 */
public class KafkaProducerClient {
    private static final String PADDING = " EzEIData(topicHdr=TopicHeader(recSeq=1, exist=1), topicData=AtpTt(header=AtpHeader(eosFlag=1, filler=, sendTimeStamp=1603862811224), dataTag=, bizTyp=, tradeSeqNum=0, tradeDate=, tradeTime=, tradeTimestamp=0, instId=, tradePrice=0, tradeQuantity=0, tradeAmount=0, tradeStatus= , buyPBUOrderNo=, buyNGTSOrderNo=0, buyActionSeqNum=0, buyOrderTime=0, buyInvAcctId=, buyPbuId=, buyBranchId=, buyClrPbuId=, buyTrdLvsQty=0, sellPBUOrderNo=, sellNGTSOrderNo=0, sellActionSeqNum=0, sellOrderTime=0, sellInvAcctId=, sellPbuId=, sellBranchId=, sellClrPbuId=, sellTrdLvsQty=0, reserved=, sellYmtAccountId=null, buyYmtAccountId=null, sellYmtAccountName=null, buyYmtAccountName=null))";
    private static final String PADDING1 = "EzEIData(topicHdr=TopicHeader(recSeq=1, exist=1), topicData=AtpTt(header=AtpHeader(eosFlag=1, filler=, sendTimeStamp=1603862811224), dataTag=, bizTyp=, tradeSeqNum=0, tradeDate=, tradeTime=, tradeTimestamp=0, instId=, tradePrice=0, tradeQuantity=0, tradeAmount=0, tradeStatus= , buyPBUOrderNo=, buyNGTSOrderNo=0, buyActionSeqNum=0, buyOrderTime=0, buyInvAcctId=, buyPbuId=, buyBranchId=, buyClrPbuId=, buyTrdLvsQty=0, sellPBUOrderNo=, sellNGTSOrderNo=0, sellActionSeqNum=0, sellOrderTime=0, sellInvAcctId=, sellPbuId=, sellBranchId=, sellClrPbuId=, sellTrdLvsQty=0, reserved=, sellYmtAccountId=null, buyYmtAccountId=null, sellYmtAccountName=null, buyYmtAccountName=null))";

    public static void main(String[] args) {

        //构建PROP对象，管理生产者的配置
        Properties props = new Properties();
        //指定连接的Kafka的地址
        props.put("bootstrap.servers", "10.112.103.2:9092,10.112.103.3:9092,10.112.103.4:9092");

        props.put("acks", "all");
//        props.put("acks", "0");
//        props.put("acks", "1");
//        props.put("acks", -1);
        //如果发送失败，重试的次数
        props.put("retries", 0);
        //每次从缓存中发送的批次的大小
        props.put("batch.size", 131072);
        //间隔时间
        props.put("linger.ms", 100);
        //生产数据的 缓存
        props.put("buffer.memory", 67108864);
        //序列化机制：Kafka也是以KV形式进行数据存储，K可以没有，写入的数据是Value
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //创建一个Kafka的生产者对象，加载Kafka生产者的配置
        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        //构建循环，模拟不断产生新的数据
        producer.send(new ProducerRecord<String, String>("huyms", null, PADDING1 + "------>" + "huang" + -1));
        List vals = new LinkedList();
        for (int i = 0; i <500; i++) {
            vals.add(PADDING);
        }
//        long start = System.nanoTime() ;
//        int j = 0;
        long tt = 0;
        for (int i = 0; i <100; i++) {
            long start = System.nanoTime();
            int j = 0;
            while (true) {

                producer.send(new ProducerRecord<String, String>("huyms", String.valueOf(j), vals.toString() + "------>" + "huang" + j));
                j++;
                if ((System.nanoTime() - start) >= 1000000000) {
                    System.out.println(j);
                    tt += j;
                    break;
                }
            }
        }
        System.out.println("平均为：" + tt / 100);
        //关闭生产者
        producer.close();
    }

}