package com.sse.rcp.domains.ezei.mtp;

import lombok.Data;

// 密码激活消息
@Data
public class PnBody17 implements PnBody {
    //    pbuId	Char[5]	PBU代码	PBU代码
    private String pbuId;

    //    invAcctTypCod	Char[1]	投资者账户类型	投资者账户类型：A、B、C、D、E、F
    private char invAcctTypCod;

    //    invtAcctID	unsigned__int32	投资者账户	投资者账户代码
    private long invtAcctID;

    //    userOrdNum	Char[16]	PBU内部订单号	PBU内部订单号
    private String userOrdNum;

    //    password	Char[8]	激活码	密码激活代码
    private String password;

    //    trasactionCode	Char[12]	交易码	子功能类型代码
    private String trasactionCode;

    //    dateLstUpdDat	unsigned__int64	最新更新时间戳	最新更新时间戳，初始值全为零
    private long dateLstUpdDat;
}
