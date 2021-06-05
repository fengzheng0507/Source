package com.sse.rcp.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Scope
@Slf4j
@Aspect
@Component
public class MonitorExecTimeAspect {
    private Logger monitorLog = LoggerFactory.getLogger("monitor");

    @Pointcut("@annotation(com.sse.rcp.monitor.MonitorPerformance)")
    public void monitorPerformance() {
    }

    @Around("monitorPerformance()")
    public Object execTimeAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        int perBatchCount = 0;
        String topicName = "";
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        MonitorPerformance monitorPerformance = signature.getMethod().getAnnotation(MonitorPerformance.class);
        try {
            Object[] args = joinPoint.getArgs();
            perBatchCount = (int) args[1];
            topicName = (String) args[2];
            return joinPoint.proceed(args);
        } finally {
            long endTime = System.currentTimeMillis();
            monitorLog.info("主题{}本次处理{}条，耗时{}秒", topicName, perBatchCount, (endTime - startTime) / 1000.0);
        }
    }
}
