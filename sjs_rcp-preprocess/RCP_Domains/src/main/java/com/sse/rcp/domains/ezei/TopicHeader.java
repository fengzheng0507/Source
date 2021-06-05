package com.sse.rcp.domains.ezei;

import lombok.Data;

@Data
public class TopicHeader {
    // recSeq	uint32	从1开始顺序递增的记录序号
    private long recSeq;

    // exist	uint8	记录存在标记，0表示后续的data字段不存在；1表示存在。本字段是为了适应未来的按行过滤功能
    private int exist;
}
