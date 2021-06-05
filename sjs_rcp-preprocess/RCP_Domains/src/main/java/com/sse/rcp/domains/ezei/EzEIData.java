package com.sse.rcp.domains.ezei;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EzEIData {
    private TopicHeader topicHdr = new TopicHeader();
    private TopicData topicData;
}


