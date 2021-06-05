package com.sse.rcp.domains.ezei.mtp;

import lombok.Data;

//    ffTxtGrp	ffTxtGrpT
@Data
public class FfTxtGrpT {
    /**
     * char[16]  PBU内部订单号
     */
    private String userOrdNum;
    /**
     * text	char[12]	备注
     * TC: 自由文本字段，供用户自定义
     * OC: 当trnTypId不为’S’时，供会员填写自定义的备注
     * 当trnTypId 取值为’S’时由交易系统赋值为：
     * 5字节交易时段英文简称，
     * 英文简称不足5字节用空格补足。
     * 以下为交易时段英文简称及意义:
     * NOTRD - 非交易
     * ADD - 新增产品
     * DEL - 产品删除
     * SUSP - 停牌
     * START - 启动
     * OCALL - 开市集合竞价
     * TRADE - 连续自动撮合
     * CCALL –闭市集合竞价
     * CLOSE - 闭市
     * ENDTR - 交易结束
     * OOBB - 开市集合竞价订单簿平衡期
     * OPOBB - 开市集合竞价订单簿平衡前期
     * IOBB - 盘中集合竞价订单簿平衡
     * POBB - 盘中集合竞价订单簿平衡前期
     * ICALL - 盘中集合竞价
     * PRETR - 盘前处理
     * POSTR - 盘后处理
     * VOLA - 连续交易和集合竞价交易的波动性中断
     * FCALL - 固定价格集合竞价
     * HALT - 暂停交易
     */
    private String text;
    /**
     * clgPrtpId	char[5]	清算会员代码	清算会员代码
     */
    private String clgPrtpId;

    /**
     * pbuOsCod	char[5]	登录PBU代码	登录PBU代码
     */
    private String pbuOsCod;

    /**
     * pbuBizCod	char[5]	业务PBU代码	业务PBU代码
     */
    private String pbuBizCod;


}
