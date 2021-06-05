package com.sse.rcp.domains.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetail {

    // 国际证券代码
    private String isinCod;
    // 交易类型(A/Add;X/Cancel;D/Delete;C/Modify;T/Trigger)
    private String trnTpyId;
    // 买卖标识(B/买;S/卖)
    private String ordrBuyCod;
    // 订单价格（买方订单或卖方订单）
    private BigDecimal ordrExePrc;
    // 尚未被执行的订单数量或买卖报价请求数量
    private BigDecimal ordrQty;
    // 成交数量
    private BigDecimal trdQty;
    // 成交金额
    private BigDecimal mktVal;
    // 账户类型(A/个人账户;B/机构账户;C/B股投资者账户;D/券商自营账户;F/基金账户;M/做市商)
    private String invAcctTypCod;
    // 投资者账户代码
    private String invAcctNo;
    // 实际提交订单的席位代码
    private String pbuOsCod;
    // 交易员申报订单时输入的自由文本（普通订单和止损订单）
    private String brnId;
    // 会员代码/基金管理公司代码
    private String memberId;
    // 订单类型(I/冰山订单;M/市价订单;L/限价订单;T/市价转限价订单;Q/报价订单;Y/意向订单;N/协商订单;C/协商对手方订单)-注:不能与订单执行限制重复
    private String ordrTypCod;
    // 交易所分配给订单的唯一编号，通过订单申报确认广播给订单呈交者
    private long ordrNo;
    // 事务日期，例如，订单中记录的交易所订单接收日期，撮合/执行买卖方订单生成成交的日期
    private long tranDat;
    // 事务时间，例如，订单中记录的交易所订单接收时间，撮合/执行买卖订单生成成交的时间
    private long tranTim;
    // 信用标签(C/担保品;M/融资;S/融券;P/平仓)
    private String creditTag;
    // invacctno是信用账户（E账户）时，此列为信用账户对应的普通账户；invacctno不是信用账户（E账户）时，此列与invacctno一致
    private String mapInvacctNo;
    // 一码通号
    private String ymtAccountId;
    // 订单执行限制：F - 全额即时;D - 即时或取消;Z - 触发的止损订单;G - 无约束条件;P - 市价本方最优;O - 市价对手方最优
    private String ordrResCod;

}
