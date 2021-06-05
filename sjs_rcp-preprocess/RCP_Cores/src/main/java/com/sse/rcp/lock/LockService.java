package com.sse.rcp.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class LockService {
    @Value("${zookeeper.base_sleep_time_ms}")
    int BASE_SLEEP_TIME_MS=5000;
    @Value("${zookeeper.max_retries}")
    int MAX_RETRIES=3;
    @Value("${zookeeper.session_time_out}")
    int SESSION_TIME_OUT=10000;
    @Value("${zookeeper.uri}")
    String ZK_URI="bdprt05:2181,bdprt06:2181,bdprt07:2181";
    @Value("${zookeeper.namespace}")
    String NAMESPACE="rcp_process";
    CuratorFramework client;
    @PostConstruct
    public void init(){
        RetryPolicy retryPolicy=new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);
        client= CuratorFrameworkFactory.builder()
                .connectString(ZK_URI).retryPolicy(retryPolicy).namespace(NAMESPACE)
                .sessionTimeoutMs(SESSION_TIME_OUT).build();
        client.start();
    }
    /**
     * 获取共享锁
     * @param taskName 任务名，用以在zk的NAMESPACE下创建目录，区分多个任务锁
     * */
    public InterProcessMutex acquireInterProcessMutex(String taskName){

        InterProcessMutex lock=new InterProcessMutex(client, taskName.startsWith("/")?taskName:"/"+taskName);
        return lock;
    }
    public void acquireLock(String taskName) throws Exception {
        InterProcessMutex lock=new InterProcessMutex(client, taskName);
        lock.acquire();
    }

}
