package com.sse.rcp.domains.ezei.mtp;

import lombok.Data;

// 投资者持仓冻结和解冻消息
@Data
public class PnBody12 implements PnBody {
    //    invAcctTypCod	Char[1]	投资者账户类型	投资者账户类型：A、B、C、D、E、F
    private char invAcctTypCod;

    //    invAcct	unsigned__int32	投资者账户	投资者账户代码
    private long invAcct;

    //    Isix	unsigned __int16	产品isix编号
    private int isix;

    //    suspUnsuspQty	unsigned__int64	冻结／非冻结数量	冻结／非冻结数量
    private long suspUnsuspQty;

    //    suspSts	Char[1]	暂停状态	S –冻结
    //    U –解冻
    private char suspSts;

    //    dateLstUpdDat	unsigned__int64	时间戳	时间戳
    private long dateLstUpdDat;
}
