package com.sse.rcp.domains.ezei.mtp;

import lombok.Data;

// 投资者账号冻结和解冻消息
@Data
public class PnBody10 implements PnBody {
    //    invAcctTypCod	Char[1]	投资者账户类型	投资者账户类型：A、B、C、D、E、F
    private char invAcctTypCod;

    //    invAcct	unsigned__int32	投资者账户	投资者账户代码
    private long invAcct;

    //    suspSts	Char[1]	暂停状态	Y –暂停
    //    N –非暂停
    private char suspSts;

    //    dateLstUpdDat	unsigned__int64	时间戳	时间戳
    private long dateLstUpdDat;
}
