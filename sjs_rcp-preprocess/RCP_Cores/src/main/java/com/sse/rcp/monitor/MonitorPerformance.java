package com.sse.rcp.monitor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MonitorPerformance {
    String desc() default "";
}
