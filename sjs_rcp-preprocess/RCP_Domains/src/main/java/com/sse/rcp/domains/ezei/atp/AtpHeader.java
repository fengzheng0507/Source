package com.sse.rcp.domains.ezei.atp;

import lombok.Data;

@Data
public class AtpHeader {

    /**
     * HHBMBcEOST	　	　	消息体	主题属性区
     * eosFlag	char	1	Eos标志	‘1’表示为该消息流已结束
     */
    private char eosFlag;

    /**
     * filler	char	15	填充字符	填0
     */
    private String filler;

    /**
     * sendTimeStamp	uint64	8	发送时间戳	unix时间戳精确到毫秒（距UTC+0 1970-01-01 00:00:00 的毫秒数）
     */
    private long sendTimeStamp;
}
