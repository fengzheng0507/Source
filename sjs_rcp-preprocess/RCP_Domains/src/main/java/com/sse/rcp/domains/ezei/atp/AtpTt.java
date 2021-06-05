package com.sse.rcp.domains.ezei.atp;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘后固定价格交易成交确认
 */
@Data
public class AtpTt implements TopicData {
    private AtpHeader header = new AtpHeader();

    /**
     * AppMthTrdCnfMsgT	　	　	消息体
     * dataTag	char	5	数据类型	TC001=撮合成交确认
     */
    private String dataTag;

    /**
     * bizTyp	char	3	业务类型	PFP=盘后固定价格交易
     */
    private String bizTyp;

    /**
     * tradeSeqNum	uint64	8	成交编号
     */
    private long tradeSeqNum;

    /**
     * tradeDate	char	8	成交日期	格式：YYYYMMDD
     */
    private String tradeDate;

    /**
     * tradeTime	char	6	成交时间	格式：HHMMSS
     */
    private String tradeTime;

    /**
     * tradeTimestamp	uint64	8	成交时间戳	使用Openvms标准的距离1858年11月17日的百纳秒，单位为百纳秒
     */
    private long tradeTimestamp;

    /**
     * instId	char	6	证券代码
     */
    private String instId;

    /**
     * tradePrice	uint64	8	成交价格	格式：精度为五位小数位
     */
    private BigDecimal tradePrice;

    /**
     * tradeQuantity	uint64	8	成交数量	格式：精度为三位小数位
     */
    private BigDecimal tradeQuantity;

    /**
     * tradeAmount	uint64	8	成交金额	格式：精度为三位小数位
     * 对于盘后固定价格交易，计算方式为成交价格x数量。
     */
    private BigDecimal tradeAmount;

    /**
     * tradeStatus	char	1	成交状态	不启用
     */
    private char tradeStatus;

    /**
     * buyPBUOrderNo	char	10	买方券商订单编号
     */
    private String buyPBUOrderNo;

    /**
     * buyNGTSOrderNo	uint64	8	买方交易所订单编号
     */
    private long buyNGTSOrderNo;

    /**
     * buyActionSeqNum	uint64	8	买方时序编号	时序编号将申报确认与成交确认递增编号
     */
    private long buyActionSeqNum;

    /**
     * buyOrderTime	uint64	8	买方申报时间戳
     */
    private long buyOrderTime;

    /**
     * buyInvAcctId	char	10	买方证券账户
     */
    private String buyInvAcctId;

    /**
     * buyPbuId	char	5	买方交易单元号
     */
    private String buyPbuId;

    /**
     * buyBranchId	char	5	买方营业部代码
     */
    private String buyBranchId;

    /**
     * buyClrPbuId	char	5	买方结算交易单元号	不启用
     */
    private String buyClrPbuId;

    /**
     * buyTrdLvsQty	uint64	8	买方成交后剩余余额
     */
    private long buyTrdLvsQty;

    /**
     * sellPBUOrderNo	char	10	卖方券商订单编号
     */
    private String sellPBUOrderNo;

    /**
     * sellNGTSOrderNo	uint64	8	卖方交易所订单编号
     */
    private long sellNGTSOrderNo;

    /**
     * sellActionSeqNum	uint64	8	卖方时序编号	时序编号将申报确认与成交确认递增编号
     */
    private long sellActionSeqNum;

    /**
     * sellOrderTime	uint64	8	卖方订单提交时间
     */
    private long sellOrderTime;

    /**
     * sellInvAcctId	char	10	卖方证券账户
     */
    private String sellInvAcctId;

    /**
     * sellPbuId	char	5	卖方交易单元号
     */
    private String sellPbuId;

    /**
     * sellBranchId	char	5	卖方营业部代码
     */
    private String sellBranchId;

    /**
     * sellClrPbuId	char	5	卖方结算交易单元号	不启用
     */
    private String sellClrPbuId;

    /**
     * SellTrdLvsQty	uint64	8	卖方成交后剩余余额
     */
    private long sellTrdLvsQty;

    /**
     * reserved	char	50	保留	前十位表示系统内部订单编号，从1开始，连续递增，左对齐，右补空格
     */
    private String reserved;

    public boolean eos() {
        return header.getEosFlag() == '1';
    }

    public String dataType() {
        return KafkaConstant.DATA_TYPE_TT;
    }

    /**
     * 买方一码通账户   String     ymtAccountId
     */
    private String sellYmtAccountId;

    /**
     * 卖方一码通账户   String     ymtAccountId
     */
    private String buyYmtAccountId;

    /**
     * 买方一码通户名   String     ymtAccountName
     */
    private String sellYmtAccountName;

    /**
     * 卖方一码通户名    String     ymtAccountName
     */
    private String buyYmtAccountName;

    /**
     * 买方 会员代码
     */
    private String memberIdB;

    /**
     * 卖方会员代码
     */
    private String memberIdS;

    /**
     * 批次号,标识一批入库数据
     */
    private String batchNum;

    /**
     * instId	char	12	国际证券代码	证券代码
     */
    private String isinCod;

    public long delayTimeStamp; //TODO
}
