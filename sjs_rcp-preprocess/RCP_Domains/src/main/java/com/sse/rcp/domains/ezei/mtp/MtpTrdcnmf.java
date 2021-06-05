package com.sse.rcp.domains.ezei.mtp;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import lombok.Data;

/**
 * 竞价成交确认消息
 */
@Data
public class MtpTrdcnmf implements TopicData {

    /**
     * trdCfmBuy	SimpTcT	买方成交结构	具体格式见下面解释
     */
    private SimpTcT trdCfmBuy;

    /**
     * trdCfmSell	SimpTcT	卖方成交结构
     */
    private SimpTcT trdCfmSell;

    public boolean eos() {
        return trdCfmBuy.getIsix() == 0;
    }

    public String dataType() {
        return KafkaConstant.DATA_TYPE_TC;
    }

    /**
     * 卖方 ISIN     String       证券12位国际代码
     */
    private String sellIsin;

    /**
     * 卖方 InstrumentId  String  证券6位国内代码
     */
    private String sellInstrumentId;

    /**
     * 卖方一码通账户   String    ymtAccountId
     */
    private String sellYmtAccountId;

    /**
     * 卖方一码通户名   String     ymtAccountName
     */
    private String sellYmtAccountName;

    /**
     * 买方 ISIN     String       证券12位国际代码
     */
    private String buyIsin;

    /**
     * 买方 InstrumentId  String  证券6位国内代码
     */
    private String buyInstrumentId;

    /**
     * 买方一码通账户   String    ymtAccountId
     */
    private String buyYmtAccountId;

    /**
     * 买方一码通户名   String     ymtAccountName
     */
    private String buyYmtAccountName;

    public long delayTimeStamp; //TODO

}
