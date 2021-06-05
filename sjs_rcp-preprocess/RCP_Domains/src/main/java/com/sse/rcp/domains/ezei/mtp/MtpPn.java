package com.sse.rcp.domains.ezei.mtp;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import lombok.Data;

/**
 * 私有消息
 */
@Data
public class MtpPn implements TopicData {
    /**
     * isinSeqNo	unsigned __int64	订单序列号	系统分配产品的序列号
     */
    private long isinSeqNo;

    /**
     * Isix	unsigned __int16	产品isix编号
     */
    private int isix;

    /**
     * privNews Char[512] 下面的私有消息将会嵌入此结构 表明此数据包包含多少条PN消息
     */
    private String privNews;

    /**
     * msgTyp	Char[2]	消息类型	10 - 投资者账号冻结和解冻
     * 11 - PBU冻结和解冻
     * 12 - 投资者持仓冻结和解冻
     * 13 -投资者指定交易创建消息
     * 17 - 密码激活
     * 21 -投资者指定交易撤销消息
     */
    private String msgTyp;

    /**
     * newsDatTim	unsigned __int64	消息发布日期时间	交易所发布新消息日期时间。YYYYMMDDHHMMSSss
     */
    private long newsDatTim;

    /**
     * newsPrio	Char[1]	消息优先级	交易所分配的公告消息的优先级。
     * H- 高优先级;
     * ''- 普通优先级
     */
    private char newsPrio;

    /**
     * newsSubj	Char[55]	消息标题 	消息标题
     */
    private String newsSubj;

    /**
     * mktId	Char[4]	市场代码	市场代码
     */
    private String mktId;

    /**
     * membExcIdCod	Char[5]	PBU代码	PBU代码
     */
    private String membExcIdCod;

    /**
     * newsSeqNum	unsigned__int64	消息顺序号	消息的顺序标号。
     */
    private long newsSeqNum;

    /**
     * Isix	unsigned __int16	产品isix编号
     */
    private int isix2;

    /**
     * exchMicId	Char[3]	交易所代码	产品所属市场身份代码(MIC)的最后三个字符。
     */
    private String exchMicId;

    /**
     * newsKey	Char[20]	消息KEY值	消息KEY值
     */
    private String newsKey;

    /**
     * msgBody	Char[350]	消息体	根据不同的消息类型嵌入不同的消息体结构，具体结构请参考3.4.1~3.4.5。
     * 当消息类型为17时，密码服务确认信息将显示在此字段中，没有具体的具体结构。
     */
    private PnBody msgBody;

    public boolean eos() {
        return isix == 0;
    }

    public String dataType() {
        return KafkaConstant.DATA_TYPE_PN;
    }

    /**
     * isin   String     证券12位国际代码
     */
    private String isin;

    /**
     * InstrumentId   String    证券6位国内代码
     */
    private String instrumentId;

    public long delayTimeStamp; //TODO
}
