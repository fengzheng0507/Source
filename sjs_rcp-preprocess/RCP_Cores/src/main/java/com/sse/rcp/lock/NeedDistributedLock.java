package com.sse.rcp.lock;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NeedDistributedLock {
    String description() default "操作中，请稍后再试";
    String key() default "dislock";
    /**
     * 是否重试获取锁，默认重试
     * */
    //boolean isRetry() default true;
    /**
     * 重试次数，默认1，isRetry=true时生效
     * */
    //int retryCount() default 1;
    /**
     * 重试等待时间，默认5秒，isRetry=true时生效
     * */
    //int retryWaitSeconds() default 5;
}
