package com.sse.rcp.domains.ezei.mtp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 注1：价格字段，精度5位，单位为元；数量字段，精度3位，单位为股；金额字段，精度5位，单位为元
 */
@Data
public class SimpTcT {
    /**
     * Isix	unsigned __int16	产品isix编号
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
     * tranIdNo	unsigned __int64	成交编号 	产品每日交易的唯一标识符
     */
    private long tranIdNo;

    /**
     * tradMtchPrc	__int64	成交价格	交易执行的价格（撮合价）
     */
    private BigDecimal tradMtchPrc;

    /**
     * trdQty	__int64	成交数量	本次交易的数量
     */
    private BigDecimal trdQty;

    /**
     * ordrNo	unsigned __int64	订单编号	该笔成交对应的订单编号
     */
    private long ordrNo;

    /**
     * mktVal	__int64	成交金额	订单执行数量乘以交易价格
     */
    private long mktVal;

    /**
     * ordrQty	__int64	剩余订单数量	未执行的订单数量。
     */
    private BigDecimal ordrQty;

    /**
     * ordrExeQty	__int64	订单累计执行数量	订单/报价中被撮合/执行并被交易的累计数量。对于非交易订单，缺省值全部填’0’，以’+’开头。
     */
    private BigDecimal ordrExeQty;

    /**
     * ordrEntTim	unsigned __int64	订单输入时间	订单输入时间
     */
    private long ordrEntTim;

    /**
     * totAucQty	__int64	已完成的集合竞价交易中的总交易数量	已完成的集合竞价交易中的总交易数量。此总数量在基于同一宗集合竞价交易（属于同一行为序列号）而发出的所有交易确认中皆相同。如交易为非集合竞价交易，则此值为0。
     */
    private long totAucQty;

    /**
     * actnSeqNum	unsigned __int64	执行动作序列号	所有订单和成交的序列号。
     */
    private long actnSeqNum;

    /**
     * ordrExePrc	__int64	订单价格	订单价格
     */
    private BigDecimal ordrExePrc;

    /**
     * tranDatTim	unsigned __int64	交易日期时间	交易日期时间 YYYYMMDDHHMMSSss
     */
    private long tranDatTim;

    /**
     * trnTypId	char	事务类型	E - 执行
     * ‘ ‘ – 非交易订单
     */
    private char trnTypId;

    /**
     * 投资者账户 由 invAcctTypCod 和 invAcct 拼接得到
     */
    private String invAcctId;

    /**
     * trdTyp	char	交易类型	C - 跨交易所交易
     * E - ETF 申购
     * F - ETF 赎回
     * N - 协商交易
     * O - OTC 场外交易
     * X - 普通交易
     * W - 权证创设
     * L - 权证注销
     * B - 质押债券入库
     * D - 质押债券出库
     * 1 - 股票配股
     * 2 - 股票转配股配股
     * 3 - 职工转配股配股
     * 4 - 股票配转债
     * R：成交撤销
     */
    private char trdTyp;

    /**
     * 1~2位  账户类型  0：私有投资者  1：代理商  2：做市商
     */
    private int accountType;

    /**
     * 3~5位   信用标签   0：无   1：融资   2：融券   3：平仓   4：担保品
     */
    private int creditLabel;

    /**
     * 6~7位   完全成交标志   0：部分撮合   1：完全撮合
     */
    private int fullTrade;

    /**
     * 第8位   订单状态   0：订单未冻结   1：订单冻结
     */
    private int orderStatus;

    private FfTxtGrpT ffTxtGrpT;

    /**
     * ctpyPbuId	char[5]	对手方PBU	对手方PBU ID（仅对场外交易及协商交易适用）
     */
    private String ctpyPbuId;

    /**
     * brnId	char[5]	营业部代码	营业部代码
     */
    private String brnId;

    /**
     * tranTypCod	char[3]	交易类型代码	015: ETF 创设赎回
     * 016: 权证创设赎回
     * 017: 国债出库入库
     * 013: OTC成交
     * 000: 普通成交
     * 012: 成交删除
     * 018: 配股成交
     * 999: 其他成交
     */
    private String tranTypCod;

    /**
     * prcTypCod	char	处理类型代码	处理类型
     * C：连续交易
     * O：开盘集合竞价
     * A：盘中集合竞价或闭市集合竞价
     * F：固定价格
     * N：协商交易
     * T：场外交易
     * E：ETF成交
     * 1：股票配股行权行权
     * 2：股票转配股配股行权
     * 3：职工股转配股配股行权
     * 4：股票配转债行权
     * W：权证创设
     * L：权证注销
     * D：国债出库
     * B：国债入库
     * 空格：其他类
     */
    private char prcTypCod;
}
