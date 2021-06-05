package com.sse.rcp.task;

import com.sse.rcp.domains.ezei.EzEITopic;
import com.sse.rcp.domains.ezei.EzEITopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskForMtp {
    private String mtpOcPrefix="sse.mtp.ordcnmf";
    private String mtpTcPrefix="sse.mtp.trdcnmf";
    private String mtpNcPrefix="sse.mtp.nontrad";
    private String mtpPnPrefix="sse.mtp.prvnews";
    //目前仅0004、0020、0991有数据
    private String[]mtpTcOcSets=new String[]{"0001","0002","0003","0004","0005","0006","0020","0991"};
    private String[]mtpNcPnSets=new String[]{"0991","0020"};
    @Autowired
    private TaskForMtpSet taskForMtpSet;
    private EzEITopics topics= EzEITopics.SINGLETON;
    public void startMtpThread(){
        startMtpOC();
        startMtpTC();
        startMtpNC();
        startMtpPN();
    }
    @Async("taskExecutor")
    public void startMtpOC(){
        startMtp(mtpOcPrefix, mtpTcOcSets);
    }
    public void startMtpTC(){
        startMtp(mtpTcPrefix, mtpTcOcSets);
    }
    public void startMtpNC(){
        startMtp(mtpNcPrefix, mtpNcPnSets);
    }
    public void startMtpPN(){
        startMtp(mtpPnPrefix, mtpNcPnSets);
    }
    private void startMtp(String mtpPrefix, String[]sets){
        for(String set: sets){
            String topicStr=mtpPrefix+"."+set;
            EzEITopic topic=topics.get(topicStr);
            taskForMtpSet.startAccess(topic);
        }
    }
}
