package com.sse.rcp.domains.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
/**
 * 竞价实时申报撤单
 */
public class OrderWithdraw {
    // 国际证券代码
    private String isinCod;
    // 撤单类型(X/Cancel,D/Delete)
    private String trnTpyId;
    // 尚未被执行的订单数量或买卖报价请求数量，此处特指撤单数量
    private BigDecimal ordrQty;
    // 被撤订单编号
    private BigDecimal ordrNo;
    // 订单申报日期
    private long tranDat;
    // 撤单时间
    private long tranTim;

}
