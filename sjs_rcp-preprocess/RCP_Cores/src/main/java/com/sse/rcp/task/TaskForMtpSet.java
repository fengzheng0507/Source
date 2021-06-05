package com.sse.rcp.task;

import com.sse.rcp.domains.ezei.EzEITopic;
import com.sse.rcp.lock.LockService;
import com.sse.rcp.lock.NeedDistributedLock;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class TaskForMtpSet {
//    @Value("${ezei.username}")
//    private String username;
//    @Value("${ezei.password}")
//    private String password;
//    @Value("${ezei.client.perpoll.max}")
//    private int maxPerPoll;
//    @Value("${ezei.host}")
//    private String host;
//    @Value("${ezei.port}")
//    private int port;
//    @Value("${ezei.datadate}")
//    private String date;
//    private LocalDate curDate;
    @Autowired
    private LockService lockService;
    @Async("taskExecutor")
    @NeedDistributedLock(key="taskformapAA")
    public void startAccess(EzEITopic topic) {
//            System.out.println(topic.getSetNo()+","+topic.getTopic()+","+Thread.currentThread().getName());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*int lowWatermark = 3 * maxPerPoll;
        int highWatermark = 10 * maxPerPoll;
        Duration timeout = Duration.ofSeconds(3);
        int quitAfter = maxPerPoll * 100;
        int logEvery = maxPerPoll * 10;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        curDate = LocalDate.parse(date, dateTimeFormatter);
        IEzEIClient client = new EzEIClient(ClientConf.builder()
                .username(username)
                .password(password)
                .highWatermark(highWatermark)
                .lowWatermark(lowWatermark)
                .dataDate(curDate)
                .host(host)
                .port(port)
                .topic(topic.getTopic()).build());
        try {
            BufferedWriter writer=new BufferedWriter(new FileWriter(new File("E:\\tmp\\"+topic.getTopic())));

        while (true) {
            PollResult pr = client.poll(maxPerPoll, timeout);
            for (DataRecord dr : pr.getRecords()) {
                MTPOrdcnmf ordcnmf = (MTPOrdcnmf) dr.getData();
                writer.write(dr.getData().toString());
                writer.newLine();
//                log.info("主题：{} 数据：{}", topic.getTopic(), ordcnmf);
            }
            writer.flush();
            if (pr.isEos()) {
                log.info("mvp: met EOS");
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }

        } catch (IOException e) {
            e.printStackTrace();
        }
        client.close(Duration.ofSeconds(15));*/
    }
}
