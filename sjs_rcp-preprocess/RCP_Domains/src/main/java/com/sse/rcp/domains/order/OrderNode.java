package com.sse.rcp.domains.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderNode {


    /**
     * instrumentId String 证券6位国内代码
     */
    private String instrumentId;

    /**
     * ordrNum	unsigned __int64	订单号码	原始订单号 来自MtpOrdcnmf ordrNum
     */
    private long ordrNum;

    /**
     * invAcctId  String  投资者账户  来自MtpOrdcnmf invAcctId
     */
    private String invAcctId;

    /**
     * ordrExePrc	__int64	订单价格（申报价格） 来自MtpOrdcnmf ordrExePrc
     */
    private BigDecimal ordrExePrc;

    /**
     * ordrQty	__int64	剩余订单有效数量	未执行的订单数量
     */
    private BigDecimal ordrQty;
}
