package com.sse.rcp.domains.ezei;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * EzEI广播报文格式
 */
@Data
public class BcastMsg {
    private BcastHeader bcastHeader = new BcastHeader();
    /**
     * EzEI报文数据
     */
    private List<EzEIData> data = new LinkedList<>();
}
