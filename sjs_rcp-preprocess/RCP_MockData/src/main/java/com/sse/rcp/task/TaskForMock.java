package com.sse.rcp.task;

import com.sse.rcp.push.PersistMockData2Csv;
import com.sse.rcp.push.PushMockData2Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class TaskForMock {

    @Value("${mock.dataType}")
    private String dataType;

    @Autowired
    private PushMockData2Topic pushMockData2Topic;

    @Autowired
    private PersistMockData2Csv persistMockData2Csv;


    public void start(String[] args) {
        // 模拟数据调试：两种方式
        switch (dataType) {
            case "byte":
                pushMockData2Topic.pushByteData2Topic();
                break;
            case "persist_csv":
                persistMockData2Csv.pushMockData2CSV();
                break;
            case "parse_csv":
                pushMockData2Topic.pushCSVData2Topic();
                break;
        }
    }
}