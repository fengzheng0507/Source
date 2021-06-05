package com.sse.rcp.domains.ezei.atp;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘后固定价格交易申报确认
 */
@Data
public class AtpTo implements TopicData {
    private AtpHeader header = new AtpHeader();

    /**
     * AppMthOdrCnfMsgT	　	　	消息体
     * dataTag	char	5	数据类型	OC007-撮合申报确认
     */
    private String dataTag;

    /**
     * bizTyp	char	3	业务类型	PFP=盘后固定价格交易
     */
    private String bizTyp;

    /**
     * actionSeqNum	uint64	8	时序编号
     */
    private long actionSeqNum;

    /**
     * ordDate	char	8	订单接收日期	格式：YYYYMMDD
     */
    private String ordDate;

    /**
     * ordTime	char	6	订单接收时间	格式：HHMMSS
     */
    private String ordTime;

    /**
     * ordTimestamp	uint64	8	订单接收时间戳	使用Openvms标准的距离1858年11月17日的百纳秒，单位为百纳秒
     */
    private long ordTimestamp;

    /**
     * ordrNo	uint64	8	交易所订单编号	对于申报指令,为交易所订单编号；
     * 对于撤单指令,为原申报订单原始编号；对于转状态和收盘价同步消息，无意义
     */
    private long ordrNo;

    /**
     * msgTyp	char	1	消息类型	D=申报，F=申报撤单，S=转状态,C=收盘价同步消息
     */
    private char msgTyp;

    /**
     * Reff	char	10	订单申报编号	订单会员内部编号，转状态消息时为产品状态,收盘价同步消息时不启用，左对齐，不足补空格
     */
    private String reff;

    /**
     * cnclReff	char	10	撤单原始订单编号	申报撤单，填被撤销的原订单会员内部编号
     * 其余，不启用
     */
    private String cnclReff;

    /**
     * instId	char	6	证券代码	证券代码
     */
    private String instId;

    /**
     * price	uint64	8	价格	格式：精度为五位小数位，单位为元
     * 收盘价同步消息时，为同步到的竞价收盘价 盘后固定价格交易转状态消息时，不启用
     */
    private BigDecimal price;

    /**
     * quantity	uint64	8	数量	格式：精度为三位小数位 转状态和收盘价同步消息时，不启用
     */
    private BigDecimal quantity;

    /**
     * Side	char	1	买卖方向	1=买，2=卖 转状态和收盘价同步消息时，不启用
     */
    private char side;

    /**
     * ordTyp	char	1	订单类型	L=限价 转状态和收盘价同步消息时，不启用
     */
    private char ordTyp;

    /**
     * invAcctId	char	10	发起方投资者账户	转状态和收盘价同步消息时，不启用
     */
    private String invAcctId;

    /**
     * pbuId	char	5	发起方业务PBU代码	转状态和收盘价同步消息时，不启用
     */
    private String pbuId;

    /**
     * branchId	char	5	发起方营业部代码	转状态和收盘价同步消息时，不启用
     */
    private String branchId;

    /**
     * clrPbuId	char	5	发起方结算代码	不启用
     */
    private String clrPbuId;

    /**
     * text	char	50	备注	前十位表示系统内部订单编号，从1开始，连续递增，左对齐，右补空格
     */
    private String text;

    public boolean eos() {
        return header.getEosFlag() == '1';
    }

    public String dataType() {
        return KafkaConstant.DATA_TYPE_TO;
    }

    /**
     * 一码通账户   String     ymtAccountId
     */
    private String ymtAccountId;

    /**
     * 一码通户名   String     ymtAccountName
     */
    private String ymtAccountName;

    /**
     * instId	char	12	国际证券代码	证券代码
     */
    private String isinCod;

    /**
     * 会员代码
     */
    private String memberId;

    /**
     * 批次号,标识一批入库数据
     */
    private long batchNum;

    public long delayTimeStamp; //TODO

}
