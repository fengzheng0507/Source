package com.sse.rcp.domains.ezei.mtp;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import lombok.Data;

/**
 * 竞价平台非交易确认消息体
 */
@Data
public class MtpNc implements TopicData {
    /**
     * isinSeqNo	unsigned __int64	订单序列号	系统分配给订单（对相同的产品）的序列号
     */
    private long isinSeqNo;

    /**
     * Isix	unsigned __int16	产品isix编号
     */
    private int isix;

    /**
     * nonTrdTypCod	Char[2]	非交易订单类型	IN - 上网发行
     * IO - 备用上网发行
     * IE - 市值配售
     * IS - 增发
     * CV - 转股
     * CR - 回售
     * OS - OEF 认购
     * OC - OEF 申购
     * OR - OEF 赎回
     * OD - OEF 分红
     * OV - OEF 基金转移
     * OT - OEF 转出
     * WE - 权证行权
     * VT - 投票
     * FS - 要约预售
     * FC - 要约撤销
     * CI - 担保品划入
     * CO - 担保品划出
     * SI - 券源划入
     * SO - 券源划出
     * SR - 还券划转
     * ST - 余券划转
     * TH –全天自设额度调整
     */
    private String nonTrdTypCod;

    /**
     * pbuId	Char[5]	PBU代码	订单所属PBU代码
     */
    private String pbuId;

    /**
     * partSubGrpIdCod	Char[3]	用户组	订单所属用户组
     */
    private String partSubGrpIdCod;

    /**
     * partNoTxt	Char[3]	用户代码	订单所属用户代码
     */
    private String partNoTxt;

    /**
     * osPbuId	Char[5]	订单提交者PBU代码	订单提交者PBU代码
     */
    private String osPbuId;

    /**
     * partOsSubGrpCod	Char[3]	订单提交者用户组	订单提交者用户组
     */
    private String partOsSubGrpCod;

    /**
     * partOsNoTxt	Char[3]	订单提交者交易员代码	订单提交者交易员代码
     */
    private String partOsNoTxt;

    /**
     * 投资者账户 由 invAcctTypCod 和 invAcct 拼接得到
     */
    private String invAcctId;

    /**
     * ordrNo	unsigned __int64	订单号码	由交易所分配给订单的订单代码/标识符，在订单录入确认时广播给提交者。
     */
    private long ordrNo;

    /**
     * maintainCode	char[1]	事务代码	A - 增加
     * D - 删除
     */
    private char maintainCode;

    /**
     * dateLstUpdDat	unsigned __int64	时间戳	时间戳
     */
    private long dateLstUpdDat;

    /**
     * ordrEntTim	unsigned __int64	订单输入时间	订单输入时间
     */
    private long ordrEntTim;

    /**
     * ordrQty	unsigned __int64	订单数量	非交易订单数量
     */
    private long ordrQty;

    /**
     * ordrAmt	unsigned __int64	订单金额	非交易订单金额
     */
    private long ordrAmt;

    /**
     * ordrPrc	unsigned __int64	订单价格	非交易订单价格
     */
    private long ordrPrc;

    /**
     * acctTypCod	char[1]	账户类型	A - 代理商
     * P - 私人投资者
     * M - 做市商
     */
    private char acctTypCod;

    /**
     * brnId	Char[5]	营业部代码	营业部代码
     */
    private String brnId;

    /**
     * userOrdNum	Char[16]	PBU内部订单号
     * 会员内部订单号，对应订单申报中的会员内部订单号reff字段 (参见IS101 上海证券交易所竞价撮合平台市场参与者接口规格说明书中申报接口)
     */
    private String userOrdNum;

    /**
     * text	Char[12]	备注	自由文本字段，供用户自定义的备注
     */
    private String text;

    /**
     * hldFromTo	Char[20]	持仓转移地点	持仓转移的地点，仅适用于OT类型
     */
    private String hldFromTo;

    /**
     * divSelct	Char[1]	分红类型	仅适用于OD类型
     * C - 现金
     * U - 份额
     */
    private char divSelct;

    /**
     * toFundId	Char[12]	基金转换对象	原始基金切换的基金ID,保存为isinCod，仅适用于OV类型
     * 对于OEF基金转换的撤销订单，缺省值为空格。
     */
    private String toFundId;

    /**
     * mainIntention	Char[4]	投票主议案	产品主议案，仅适用VT类型
     */
    private String mainIntention;

    /**
     * subIntention	Char[4]	投票副议案	产品副议案，仅适用VT类型
     */
    private String subIntention;

    /**
     * preference	Char[23]	投票意向	投票意向，仅适用VT类型
     */
    private String preference;

    public boolean eos() {
        return isix == 0;
    }

    public String dataType() {
        return KafkaConstant.DATA_TYPE_NC;
    }

    /**
     * ymtAccountId    String   一码通账户
     */
    private String ymtAccountId;

    /**
     * ymtAccountName   String  一码通户名
     */
    private String ymtAccountName;

    /**
     * isin    String  证券12位国际代码
     */
    private String isin;

    /**
     * InstrumentId    String    证券6位国内代码
     */
    private String instrumentId;

    public long delayTimeStamp; //TODO
}
