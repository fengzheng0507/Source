package com.sse.rcp;


import com.sse.rcp.task.TaskForMock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.sse"})
public class MockApplication implements CommandLineRunner {


    @Autowired
    private TaskForMock taskForMock;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MockApplication.class);
        application.run(args);
    }

    public void run(String... args) throws Exception {
        taskForMock.start(args);
    }
}
