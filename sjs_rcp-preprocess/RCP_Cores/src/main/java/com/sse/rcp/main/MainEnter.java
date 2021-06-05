package com.sse.rcp.main;

import com.sse.rcp.task.TaskForEnter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = {"com.sse"})
public class MainEnter implements CommandLineRunner {
    //scanBasePackages 如果bean与main入口不在同一个package下，需要scan
    @Autowired
    private TaskForEnter taskForEnter;

    public static void main(String[]args){
        SpringApplication application=new SpringApplication(MainEnter.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        taskForEnter.start(args);
//        Thread.currentThread().join();
    }
}
