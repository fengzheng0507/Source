package com.sse.rcp.domains.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
/**
 * 竞价持仓详情表
 */
public class PositionDetail {
    // 投资者账户代码
    private String investorAccountId;
    // 国际证券代码
    private String isinCod;
    // 上一交易日持仓量
    private BigDecimal positionAmountOld;
    // 持有数量
    private BigDecimal positionAmount;
    // 席位代码
    private String pbuId;
    // 营业部代码
    private String brnId;

    // 交易日
    private String tradeDate;
    /**
     * investor_account_id是信用账户（e账户）时，此列为信用账户对应的普通账户；
     * investor_account_id不是信用账户（e账户）时，此列与investor_account_id一致
     */
    private String mapInvacctno;

    // 一码通号
    private String ymtAccountId;


}
