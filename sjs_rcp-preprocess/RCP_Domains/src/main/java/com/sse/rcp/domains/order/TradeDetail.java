package com.sse.rcp.domains.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeDetail {
    // 买卖标识(0/买;1/卖;2/无;-1其它)
    private int driverFlg;
    // 产品每日交易的唯一标识，成交确认号
    private long tranIdNo;
    // 国际证券代码
    private String isinCod;
    // 最新成交价
    private BigDecimal tradePriceNew;
    // 交易执行价格（撮合价格），即交易产品的单位价格。对于净值交易，发送最后一笔交易的撮合价格
    private BigDecimal tradMtchPrc;
    // 成交数量
    private BigDecimal trdQty;
    // 该字段为订单执行数量乘以成交价格，成交金额
    private BigDecimal mktVal;
    // 全部竞价数量
    private BigDecimal totaUcQty;
    /**
     * 交易类型：
     * C-跨交易所交易；
     * E-ETF 申购；
     * F-ETF 赎回；
     * N-协商交易；
     * O-OTC 场外交易；
     * X-普通交易；
     * W-权证创设；
     * L-权证注销；
     * B-质押债券入库；
     * D-质押债券出库；
     * 1-股票配股；
     * 2-股票转配股配股；
     * 3-职工转配股配股；
     * 4-股票配转债
     */
    private String trdTyp;
    // 描述交易限制类型的代码/标识符,AU - 仅开市集合竞价;FP - 固定价格;OA - 公开竞价;SU - 接受剩余;''  '' - 无约束条件
    private String trdResTypCod;
    // 交易类型(A/增加;X/取消;D/删除;C/修改;T/触发 (当STP订单被从停止订单簿移除时);S/交易阶段改变)
    private String trnTpyId;
    // 交易所分配给订单的唯一编号，通过订单申报确认广播给订单呈交者
    private long ordrNoB;
    // 交易所分配给订单的唯一编号，通过订单申报确认广播给订单呈交者
    private long ordrnoS;
    // 订单是否全部完成的标识(P/部分;F/全部)
    private String ordrComplCodB;
    // 订单是否全部完成的标识(P/部分;F/全部)
    private String ordrComplCodS;
    // 订单类型(I/冰山订单;M/市价订单;L/限价订单;T/市价转限价订单;Q/报价订单)
    private String ordrTypCodB;
    // 订单类型(I/冰山订单;M/市价订单;L/限价订单;T/市价转限价订单;Q/报价订单)
    private String ordrTypCodS;
    // 订单价格（买方订单或卖方订单）
    private BigDecimal ordrExePrcB;
    // 订单价格（买方订单或卖方订单）
    private BigDecimal ordrExePrcS;
    // 尚未被执行的订单数量或买卖报价请求数量
    private BigDecimal ordQtyB;
    // 尚未被执行的订单数量或买卖报价请求数量
    private BigDecimal ordQtyS;
    // 已撮合/执行、成交的订单累计数量
    private BigDecimal ordrExeQtyB;
    // 已撮合/执行、成交的订单累计数量
    private BigDecimal ordrExeQtyS;
    // 账户类型(A/个人账户;B/机构账户;C/B股投资者账户;D/券商自营账户;F/基金账户;M/做市商)
    private String invAcctTypCodB;
    // 账户类型(A/个人账户;B/机构账户;C/B股投资者账户;D/券商自营账户;F/基金账户;M/做市商)
    private String invAcctTypCodS;
    // 买方投资者账户代码
    private String invAcctNoB;
    // 卖方投资者账户代码
    private String invAcctNoS;
    // 买方席位代码
    private String pbuIdB;
    // 卖方席位代码
    private String pbuIdS;
    // 订单提交者的席位代码
    private String osPbuIdB;
    // 订单提交者的席位代码
    private String osPbuIdS;
    // 买方营业部代码
    private String brnIdB;
    // 卖方营业部代码
    private String brnIdS;
    // 会员代码/基金管理公司代码
    private String memberIdB;
    // 会员代码/基金管理公司代码
    private String memberIdS;
    // 买方信用标签(C/担保品;M/融资;S/融券;P/平仓)
    private String creditTagB;
    // 卖方信用标签(C/担保品;M/融资;S/融券;P/平仓)
    private String creditTagS;
    // 事务日期，例如，订单中记录的交易所订单接收日期，撮合/执行买卖方订单生成成交的日期
    private long tranDat;
    // 事务时间，例如，订单中记录的交易所订单接收时间，撮合/执行买卖订单生成成交的时间
    private long tranTim;
    // 批次号，标识一批由EAI发送的数据
    private long batchNum;
    // INVACCTNO_S是信用账户（E账户）时，此列为信用账户对应的普通账户；INVACCTNO_S不是信用账户（E账户）时，此列与INVACCTNO_S一致
    private String mapInvAcctNoB;

    private String mapInvAcctNoS;
    // 一码通号(买)
    private String ymtAccountIdB;
    // 一码通号(卖)
    private String ymtAccountIdS;
    // 买订单执行限制：F - 全额即时;D - 即时或取消;Z - 触发的止损订单;G - 无约束条件;P - 市价本方最优;O - 市价对手方最优
    private String ordrResCodB;
    // 卖订单执行限制：F - 全额即时;D - 即时或取消;Z - 触发的止损订单;G - 无约束条件;P - 市价本方最优;O - 市价对手方最优
    private String ordrResCodS;


}
