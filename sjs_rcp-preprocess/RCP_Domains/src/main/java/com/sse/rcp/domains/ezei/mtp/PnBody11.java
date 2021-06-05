package com.sse.rcp.domains.ezei.mtp;

import lombok.Data;

// PBU冻结和解冻消息
@Data
public class PnBody11 implements PnBody {
    //    pbuIdCod	Char[5]	PBU代码	PBU代码
    private String pbuIdCod;

    //    suspSts	Char[1]	暂停状态	S –暂停
    //    N –非暂停
    private char suspSts;

    //    dateLstUpdDat	unsigned__int64	时间戳	时间戳
    private long dateLstUpdDat;
}
