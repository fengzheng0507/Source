package com.sse.rcp.domains.ezei;

import lombok.Getter;

@Getter
public enum BcastType {
    OC(1, "订单确认消息", 109),//MTPOrdcnmf
    TC(2, "成交确认消息", 330),//MTPTrdcnmf
    NC(3, "非交易确认", 186),//MTPNc
    PN(4, "私有消息", 522),                 // MTPPn
    TO(26, "盘后固定价格交易申报确认", 190),   // ATPTO
    TT(27, "盘后固定价格交易成交确认", 277);   // ATPTT

    BcastType(int value, String title, int recLen) {
        this.value = value;
        this.title = title;
        this.recLen = recLen;
    }

    private int value;
    private String title;
    private int recLen;
}
