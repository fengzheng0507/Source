package com.sse.rcp.domains.ezei;

import lombok.Value;

@Value
public class EzEITopic {
    private TradePlatform tradePlatform;
    private String setNo;
    private BcastType bcastType;
    private String topic;
}


