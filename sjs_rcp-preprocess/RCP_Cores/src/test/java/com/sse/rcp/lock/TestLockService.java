package com.sse.rcp.lock;

import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

public class TestLockService {
    public static void main(String[]args){
        for(int i=0;i<6;i++){
            LockThread thread=new LockThread(i+"");
            new Thread(thread).start();
        }
    }
}
class LockThread implements Runnable{
    LockService lockService;
    InterProcessMutex lock;
    String name;
    public LockThread(String name){
        lockService=new LockService();
        lockService.init();
        lock=new InterProcessMutex(lockService.client, "/taskForMap");
        this.name=name;
    }

    @Override
    public void run() {
        try {
            lock.acquire();
            System.out.println(this.name+"抢占成功");
            Thread.sleep(1000);
            lock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}