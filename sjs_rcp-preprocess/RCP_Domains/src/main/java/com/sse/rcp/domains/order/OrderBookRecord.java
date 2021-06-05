package com.sse.rcp.domains.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderBookRecord {
    // SET号
    private int setNo;
    // 执行动作序列号
    private long actSeqNo;
    // 订单号
    private long ordrNo;
    // 上一笔订单号
    private long lastOrdrNo;
    // 国际证券代码
    private String isinCod;
    // 国内证券代码
    private String instrumentId;
    // 数据类型(1:订单,2:补订单,3:撤单)
    private int ordType;
    // 上一笔数据类型(1:订单,2:补订单,3:撤单)
    private int lastOrdType;
    // 买卖方向
    private String ordrBuyCod;
    // 档位
    private int levelPrice;
    // 档位价格
    private BigDecimal price;
    // 申报量
    private long ordrQty;
    // 申买价一
    private BigDecimal buyPrice1;
    // 申买量一
    private long buyVolume1;
    // 申卖价一
    private BigDecimal sellPrice1;
    // 申卖量一
    private long sellVolume1;
    // 申买价二
    private BigDecimal buyPrice2;
    // 申买量二
    private long buyVolume2;
    // 申卖价二
    private BigDecimal sellPrice2;
    // 申卖量二
    private long sellVolume2;
    // 申买价三
    private BigDecimal buyPrice3;
    // 申买量三
    private long buyVolume3;
    // 申卖价三
    private BigDecimal sellPrice3;
    // 申卖量三
    private long sellVolume3;
    // 申买价四
    private BigDecimal buyPrice4;
    // 申买量四
    private long buyVolume4;
    // 申卖价四
    private BigDecimal sellPrice4;
    // 申卖量四
    private long sellVolume4;
    // 申买价五
    private BigDecimal buyPrice5;
    // 申买量五
    private long buyVolume5;
    // 申卖价五
    private BigDecimal sellPrice5;
    // 申卖量五
    private long sellVolume5;
    // 申买价六
    private BigDecimal buyPrice6;
    // 申买量六
    private long buyVolume6;
    // 申卖价六
    private BigDecimal sellPrice6;
    // 申卖量六
    private long sellVolume6;
    // 申买价七
    private BigDecimal buyPrice7;
    // 申买量七
    private long buyVolume7;
    // 申卖价七
    private BigDecimal sellPrice7;
    // 申卖量七
    private long sellVolume7;
    // 申买价八
    private BigDecimal buyPrice8;
    // 申买量八
    private long buyVolume8;
    // 申卖价八
    private BigDecimal sellPrice8;
    // 申卖量八
    private long sellVolume8;
    // 申买价九
    private BigDecimal buyPrice9;
    // 申买量九
    private long buyVolume9;
    // 申卖价九
    private BigDecimal sellPrice9;
    // 申卖量九
    private long sellVolume9;
    // 申买价十
    private BigDecimal buyPrice10;
    // 申买量十
    private long buyVolume10;
    // 申卖价十
    private BigDecimal sellPrice10;
    // 申卖量十
    private long sellVolume10;

    //投资者账户
    private String invActId;
    //一码通账户
    private String ymtActId;

    // 一码通申买1
    private long ymtBuyOrdVolume1;
    // 一码通申买价1
    private BigDecimal ymtBuyPrice1;
    // 一码通申买2
    private long ymtBuyOrdVolume2;
    // 一码通申买价2
    private BigDecimal ymtBuyPrice2;
    // 一码通申买3
    private long ymtBuyOrdVolume3;
    // 一码通申买价3
    private BigDecimal ymtBuyPrice3;
    // 一码通申买4
    private long ymtBuyOrdVolume4;
    // 一码通申买价4
    private BigDecimal ymtBuyPrice4;
    // 一码通申买5
    private long ymtBuyOrdVolume5;
    // 一码通申买价5
    private BigDecimal ymtBuyPrice5;

    // 一码通申卖1
    private long ymtSellOrdVolume1;
    // 一码通申买价1
    private BigDecimal ymtSellPrice1;
    // 一码通申卖2
    private long ymtSellOrdVolume2;
    // 一码通申买价2
    private BigDecimal ymtSellPrice2;
    // 一码通申卖3
    private long ymtSellOrdVolume3;
    // 一码通申买价3
    private BigDecimal ymtSellPrice3;
    // 一码通申卖4
    private long ymtSellOrdVolume4;
    // 一码通申买价4
    private BigDecimal ymtSellPrice4;
    // 一码通申卖5
    private long ymtSellOrdVolume5;
    // 一码通申买价5
    private BigDecimal ymtSellPrice5;

    // 事务日期
    private long trandat;
    // 事务时间
    private long trantim;
    // 最新价
    private BigDecimal tradePriceNew;
}
