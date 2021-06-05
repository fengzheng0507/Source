package com.sse.rcp.domains.ezei;

public interface TopicData {
    boolean eos();

    String dataType();

    long getDelayTimeStamp();
}
