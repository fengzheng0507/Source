package com.sse.rcp.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Scope
@Component
@Aspect
@Slf4j
public class DistributedLockAspect {
    @Autowired
    LockService lockService;
    @Pointcut("@annotation(com.sse.rcp.lock.NeedDistributedLock)")
    public void distributeLock(){}
    @Around("distributeLock()")
    public void process(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature=(MethodSignature)joinPoint.getSignature();
        Method method=methodSignature.getMethod();
        if(null==method){
            throw new Exception("DisLock method is null");
        }

        //避免锁的名称一样，加上获取锁的类名和方法名
        String lockKeyPrefix = joinPoint.getTarget().getClass().getSimpleName()+"_"+method.getName();

        //分布式锁取第一个参数作为zk目录名
        Object[]args=joinPoint.getArgs();
        String lockKey= "";
        if(args==null||args.length==0){
            lockKey =lockKeyPrefix;
        }else if(args[0] instanceof String){
            lockKey = lockKeyPrefix+"_"+(String)args[0];
        }else if(args[0] instanceof List){
            StringBuffer tempStr = new StringBuffer();
            for(String per: (List<String>)args[0]){
                tempStr.append(per);
            }
            lockKey = lockKeyPrefix+"_"+tempStr.toString();
        }

        if(StringUtils.isEmpty(lockKey)){
            //为空，则使用方法名创建锁
            lockKey= lockKeyPrefix;
        }

        InterProcessMutex disLock=lockService.acquireInterProcessMutex(lockKey);
        disLock.acquire();
        log.info("获取分布式锁{}，执行", lockKey);
        joinPoint.proceed();
        disLock.release();
    }
}
