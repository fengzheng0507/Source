package com.sse.rcp.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ZkClientUtil {
    static int BASE_SLEEP_TIME_MS=5000;
    static int MAX_RETRIES=3;
    static int SESSION_TIME_OUT=10000;
    static String ZK_URI="bdprt05:2181,bdprt06:2181,bdprt07:2181";
    static String NAMESPACE="rcp_process";
    public static CuratorFramework build(){
        RetryPolicy retryPolicy=new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);
        CuratorFramework client= CuratorFrameworkFactory.builder()
                .connectString(ZK_URI).retryPolicy(retryPolicy).namespace(NAMESPACE)
                .sessionTimeoutMs(SESSION_TIME_OUT).build();
        return client;
    }

    public List<String> multiPath(List<String>names){
        List<String> paths=new ArrayList<>();
        for(String name:names){
            paths.add(buildPath(name));
        }
        return paths;
    }
    public String buildPath(String name){
        String path="";
        String[]roots=new String[]{"mg","lock"};
        return path;
    }
}
