package com.sse.rcp.domains.ezei.mtp;

import lombok.Data;

// 投资者指定交易创建消息
@Data
public class PnBody13 implements PnBody {
    //    invAcctTypCod	Char[1]	投资者账户类型	投资者账户类型：A、B、C、D、E、F
    private char invAcctTypCod;

    //    invAcct	unsigned__int32	投资者账户	投资者账户代码
    private long invAcct;

    //    srcMembIstIdCod	Char[5]	PBU代码	PBU代码
    private String srcMembIstIdCod;

    //    tgtMembIstIdCod	Char[5]	目标PBU代码	不可用
    private String tgtMembIstIdCod;

    //    pIOrdNo	Char[16]	PBU内部订单号	PBU内部订单号
    private String pIOrdNo;

    //    text	Char[12]	备注	自由文本字段，供用户自定义的备注
    private String text;

    //    orderNo	unsigned__int64	订单编号	订单编号
    private long orderNo;

    //    dateLstUpdDat	unsigned__int64	最新更新时间戳	最新更新时间戳
    private long dateLstUpdDat;
}
