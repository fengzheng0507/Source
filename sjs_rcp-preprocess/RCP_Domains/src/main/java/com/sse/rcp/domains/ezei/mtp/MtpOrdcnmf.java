package com.sse.rcp.domains.ezei.mtp;

import com.sse.rcp.domains.constant.KafkaConstant;
import com.sse.rcp.domains.ezei.TopicData;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 竞价订单确认消息
 * 注1：价格字段，精度5位，单位为元；数量字段，精度3位，单位为股；金额字段，精度5位，单位为元
 */
@Data
public class MtpOrdcnmf implements TopicData {

    /**
     * isix	unsigned __int16	产品isix编号
     */
    private int isix;

    /**
     * 1~2位   产品买卖  0:买 1:卖 2:无
     */
    private int prodBuySell;

    /**
     * 3~4位  卖空标志  0：卖空订单  1：非卖空订单  2：无
     */
    private int sellOut;

    /**
     * 5~7位  交易限制
     * 0：只参与开盘集合竞价
     * 1：只参与集合竞价
     * 2：只参与订单簿平衡时段
     * 3：只参与固定价格时段
     * 4：无交易时段限制/非交易订单
     */
    private int resTrade;

    /**
     * 8~10位  执行限制
     * 0：非全部执行即撤销
     * 1：即时执行剩余撤销
     * 2：止损订单STP
     * 3：触发的止损订单TRG
     * 4：无限制  /非交易订单
     * 5：市价本方最优
     * 6：市价对手方最优
     */
    private int execRestrict;

    /**
     * 11~13位  订单类型
     * 0：限价订单
     * 1：市价订单
     * 2：市价转限价
     * 3：冰山订单
     * 4：报价
     */
    private int orderType;


    /**
     * ordrExePrc	__int64	订单价格	订单价格
     */
    private BigDecimal ordrExePrc;

    /**
     * ordrQty	__int64	剩余订单数量	未执行的订单数量
     */
    private BigDecimal ordrQty;

    /**
     * ordrExeQty	__int64	订单累计执行数量	订单/报价中被撮合/执行并被交易的累计数量
     */
    private BigDecimal ordrExeQty;

    /**
     * ordrNum	unsigned __int64	订单号码	原始订单号
     */
    private long ordrNum;

    /**
     * actnSeqNum	unsigned __int64	执行动作序列号	所有订单和成交的序列号。
     */
    private long actnSeqNum;

    /**
     * tranDatTim	unsigned __int64	交易日期时间	交易日期时间 YYYYMMDDHHMMSSss
     */
    private long tranDatTim;


    private FfTxtGrpT ffTxtGrp;

    /**
     * brnId	char[5]	营业部代码	营业部代码
     */
    private String brnId;

    /**
     * 投资者账户 由 invAcctTypCod 和 invAcct 拼接得到
     */
    private String invAcctId;

    /**
     * trnTypId	char	事务类型	A - 增加
     * X - 撤销
     * D - 删除
     * C - 修改
     * T - 止损触发
     * S - 交易时段改变
     * ‘‘ - 非交易订单
     */
    private char trnTypId;


    /**
     * 1~2位  账户类型  0：私有投资者  1：代理商  2：做市商
     */
    private int accountType;

    /**
     * 3~5位   信用标签   0：无   1：融资   2：融券   3：平仓   4：担保品
     */
    private int creditLabel;

    /**
     * 6~7位   完全成交标志   0：部分撮合   1：未撮合成交   2：完全撮合
     */
    private int fullTrade;

    /**
     * 第8位   订单状态   0：订单未冻结   1：订单冻结
     */
    private int orderStatus;


    /**
     * filler2	char[2]	填充字节
     */
    private String filler2;


    /**
     * 一码通账户   String     ymtAccountId
     */
    private String ymtAccountId;

    /**
     * 一码通户名   String     ymtAccountName
     */
    private String ymtAccountName;

    /**
     * isin   String     证券12位国际代码
     */
    private String isin;

    /**
     * InstrumentId   String    证券6位国内代码
     */
    private String instrumentId;


    public String dataType() {
        return KafkaConstant.DATA_TYPE_OC;
    }

    public boolean eos() {
        return isix == 0;
    }

    public long delayTimeStamp; //TODO
}
