package com.sse.rcp.order;

import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;

public interface OrderOperator {

    public void processTrdcnmf(MtpTrdcnmf topicData);

    public void processOrdcnmf(MtpOrdcnmf topicData);
}
