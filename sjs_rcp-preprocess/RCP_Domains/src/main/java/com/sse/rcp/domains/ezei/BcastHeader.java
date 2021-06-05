package com.sse.rcp.domains.ezei;

import lombok.Data;

/**
 * EzEI主题广播头部格式
 */
@Data
public class BcastHeader {
    /**
     * topic	char[32]	主题名称，左对齐右补空格，大小写敏感
     */
    private String topic;
    /**
     * recCount	uint32	数据记录个数
     */
    private long recCount;
    /**
     * recLen	uint16	数据记录长度(一条主题消息体长度)
     */
    private int recLen;
}
