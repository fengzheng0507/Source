package com.sse.rcp.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 预处理入口任务类，由该类启动所有预处理任务
 */
@Slf4j
@DependsOn({"redisUtil", "initUtil", "kafkaProducerTool"})
@Component
public class TaskForEnter {
    @Autowired
    private TaskForSort taskForSort;
    @Autowired
    private TaskForOrderBook taskForOrderBook;
    @Autowired
    private TaskForAggregation taskForAggregation;
    @Autowired
    private TaskForSinkDB taskForDB;

    @Autowired
    private TaskForATPSort taskForATPSort;
    @Autowired
    private TaskForAtpDB taskForAtpDB;


    @Value("${application.taskType}")
    private String appType;
    private static final String APP_TYPE_SORT_INJECT = "sort";
    private static final String APP_TYPE_ORDER_BOOK = "orderBook";
    private static final String APP_TYPE_AGGREGATE = "aggregate";
    private static final String APP_TYPE_INPUT_DB = "inputDb";
    private static final String APP_TYPE_INPUT_ATP = "atp";

    public void start(String... args) {
        // sort,orderBook,inputDb
        if (args == null || args.length < 1 || Arrays.stream(args).allMatch(p ->
                !p.contains(APP_TYPE_SORT_INJECT) && !p.contains(APP_TYPE_ORDER_BOOK) &&
                        !p.contains(APP_TYPE_AGGREGATE) && !p.contains(APP_TYPE_INPUT_DB))) {
            log.info("-----无指定task，默认启动所有task-----");
            taskForDB.realtimeInputDB();
//        taskForAggregation.aggregate();  TODO
            taskForOrderBook.orderBook();
            taskForSort.sortAndInject();
        } else {
            String tasks = Arrays.toString(args);
            log.info("-----启动应用，指定[{}]task-----", tasks);
            if (tasks.contains(APP_TYPE_INPUT_DB)) {
                taskForDB.realtimeInputDB();
            }
            if (tasks.contains(APP_TYPE_AGGREGATE)) {
                taskForAggregation.aggregate();
            }
            if (tasks.contains(APP_TYPE_ORDER_BOOK)) {
                taskForOrderBook.orderBook();
            }
            if (tasks.contains(APP_TYPE_SORT_INJECT)) {
                taskForSort.sortAndInject();
                if (tasks.contains(APP_TYPE_INPUT_ATP))
                    taskForATPSort.sortAndInject();
            }
            if (tasks.contains(APP_TYPE_INPUT_ATP)) {
                taskForAtpDB.realtimeInputDB();
                taskForATPSort.sortAndInject();
            }
        }
    }
}
