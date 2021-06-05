package com.sse.rcp.domains.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class YmtDeclareNode {
    //一码通账户
    private String ymtActId;

    /**
     * instrumentId String 证券6位国内代码
     */
    private String instrumentId;

    /**
     * ordrExePrc	__int64	订单价格（限价）	   来自 MTPOrdcnmf ordrExePrc订单申报价格
     */
    private BigDecimal ordrExePrc;

    /**
     * orderAmount  Long   当前价格节点的订单总笔数 , 该产品当前价剩余订单数量的总个数== MTPOrdcnmf ordrQty 的个数
     */
    private long orderCounts;

    /**
     * orderAmount  Long   当前价格节点中的成交次数
     */
    private long tradeCounts;

    /**
     * orderAmount  Long  当前价格节点中的撤单次数
     */
    private long cancelCounts;

    /**
     * ordrQtyTotal Long 该产品当前价的有效剩余申报量 == MTPOrdcnmf 表中的产品当前价格节点 剩余申报量
     */
    private BigDecimal ordrQtyTotal;
}
