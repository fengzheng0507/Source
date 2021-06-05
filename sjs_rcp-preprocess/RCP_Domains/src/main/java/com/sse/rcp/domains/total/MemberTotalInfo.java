package com.sse.rcp.domains.total;

import lombok.Data;

@Data
public class MemberTotalInfo  {
    /**
     * 持仓数量
     */
    private long lLastQTY;
    /**
     * 申报买数量
     */
    private long lOrdrBUYQty;
    /**
     * 申报卖数量
     */
    private long lOrdrSELLQty;
    /**
     * 申报买笔数
     */
    private long lOrdrBuyNums;
    /**
     * 申报卖笔数
     */
    private long lOrdrSellNums;
    /**
     * 申报撤单数量
     */
    private long lOrdrDELQty;
    /**
     * 申报撤单数量--买
     */
    private long lOrdrDELBuyQty;
    /**
     * 申报撤单笔数
     */
    private long lOrdrDELNums;
    /**
     * 申报撤单笔数--买
     */
    private long lOrdeDELBuyNums;
    /**
     * 成交买数量
     */
    private long lTradBUYQty;
    /**
     * 成交卖数量
     */
    private long lTradSELLQty;
    /**
     * 其它买数量
     */
    private long lOthBUYQty;
    /**
     * 其它卖数量
     */
    private long lOthSELLQty;
    /**
     * 集合竞价成交买数量
     */
    private long lAucBUYQty;
    /**
     * 集合竞价成交卖数量
     */
    private long lAucSELLQty;
    /**
     * 价格向上变动次数
     */
    private long upNums;
    /**
     * 价格向下变动次数
     */
    private long downNums;
    /**
     * 价格向上变动差价之和
     */
    private long upPrice;
    /**
     * 价格向下变动差价之和
     */
    private long downPrice;
    /**
     * 申报买金额
     */
    private double dOrdrBUYAmt;
    /**
     * 申报卖金额
     */
    private double dOrdrSELLAmt;
    /**
     * 成交买金额
     */
    private double dTradBUYAmt;
    /**
     * 成交卖金额
     */
    private double dTradSELLAmt;
    /**
     * 一般机构的买成交金额
     */
    private double dOrganTrdAmtB;
    /**
     * 一般机构的卖成交金额
     */
    private double dOrganTrdAmtS;
    /**
     * 一般机构的成交买数量
     */
    private long lOrganBUYQty;
    /**
     * 一般机构的成交卖数量
     */
    private long lOrganSELLQty;
    /**
     * 一般机构E账户的买成交金额
     */
    private double dEOrganTrdAmtB;
    /**
     * 一般机构E账户的卖成交金额
     */
    private double dEOrganTrdAmtS;
    /**
     * 一般机构E账户的成交买数量
     */
    private long lEOrganBUYQty;
    /**
     * 一般机构E账户的成交卖数量
     */
    private long lEOrganSELLQty;


}
