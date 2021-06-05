package com.sse.rcp.domains.utils;

import com.sse.rcp.domains.constant.AppConst;
import com.sse.rcp.domains.ezei.BcastType;
import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.atp.AtpHeader;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.ezei.mtp.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class DomainsDecodeUtil {
    public static TopicData decode(byte[] bytes, BcastType bcastType) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        if (bcastType.getRecLen() == bytes.length && bb.remaining() == 0) {
            //throw exception
        }
        TopicData td = null;
        switch (bcastType) {
            case OC:
                td = mtpOrdcnmf(bb);
                break;
            case TC:
                td = mtpTrdcnmf(bb);
                break;
            case PN:
                td = mtpPn(bb);
                break;
            case NC:
                td = mtpNc(bb);
                break;
            case TO:
                td = atpTo(bb);
                break;
            case TT:
                td = atpTt(bb);
                break;
        }
        return td;
    }

    private static void atpHeader(ByteBuffer bb, AtpHeader r) {
        r.setEosFlag((char) bb.get());
        r.setFiller(ByteUtil.readCharSequence(bb, 15));
        r.setSendTimeStamp(bb.getLong());
    }

    private static AtpTo atpTo(ByteBuffer bb) {
        AtpTo r = new AtpTo();
        atpHeader(bb, r.getHeader());
        r.setDataTag(ByteUtil.readCharSequence(bb, 5));
        r.setBizTyp(ByteUtil.readCharSequence(bb, 3));
        r.setActionSeqNum(bb.getLong());
        r.setOrdDate(ByteUtil.readCharSequence(bb, 8));
        r.setOrdTime(ByteUtil.readCharSequence(bb, 6));
        r.setOrdTimestamp(bb.getLong());
        r.setOrdrNo(bb.getLong());
        r.setMsgTyp((char) bb.get());
        r.setReff(ByteUtil.readCharSequence(bb, 10));
        r.setCnclReff(ByteUtil.readCharSequence(bb, 10));
        r.setInstId(ByteUtil.readCharSequence(bb, 6));
        r.setPrice(getRealOrderExePrc(bb.getLong()));
        r.setQuantity(getRealOrderQty(bb.getLong()));
        r.setSide((char) bb.get());
        r.setOrdTyp((char) bb.get());
        r.setInvAcctId(ByteUtil.readCharSequence(bb, 10));
        r.setPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setBranchId(ByteUtil.readCharSequence(bb, 5));
        r.setClrPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setText(ByteUtil.readCharSequence(bb, 50));
        return r;
    }

    private static AtpTt atpTt(ByteBuffer bb) {
        AtpTt r = new AtpTt();
        atpHeader(bb, r.getHeader());
        r.setDataTag(ByteUtil.readCharSequence(bb, 5));
        r.setBizTyp(ByteUtil.readCharSequence(bb, 3));
        r.setTradeSeqNum(bb.getLong());
        r.setTradeDate(ByteUtil.readCharSequence(bb, 8));
        r.setTradeTime(ByteUtil.readCharSequence(bb, 6));
        r.setTradeTimestamp(bb.getLong());
        r.setInstId(ByteUtil.readCharSequence(bb, 6));
        r.setTradePrice(getRealOrderExePrc(bb.getLong()));
        r.setTradeQuantity(getRealOrderQty(bb.getLong()));
        r.setTradeAmount(getRealOrderQty(bb.getLong()));
        r.setTradeStatus((char) bb.get());
        r.setBuyPBUOrderNo(ByteUtil.readCharSequence(bb, 10));
        r.setBuyNGTSOrderNo(bb.getLong());
        r.setBuyActionSeqNum(bb.getLong());
        r.setBuyOrderTime(bb.getLong());
        r.setBuyInvAcctId(ByteUtil.readCharSequence(bb, 10));
        r.setBuyPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setBuyBranchId(ByteUtil.readCharSequence(bb, 5));
        r.setBuyClrPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setBuyTrdLvsQty(bb.getLong());
        r.setSellPBUOrderNo(ByteUtil.readCharSequence(bb, 10));
        r.setSellNGTSOrderNo(bb.getLong());
        r.setSellActionSeqNum(bb.getLong());
        r.setSellOrderTime(bb.getLong());
        r.setSellInvAcctId(ByteUtil.readCharSequence(bb, 10));
        r.setSellPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setSellBranchId(ByteUtil.readCharSequence(bb, 5));
        r.setSellClrPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setSellTrdLvsQty(bb.getLong());
        r.setReserved(ByteUtil.readCharSequence(bb, 50));
        return r;
    }

    public static MtpOrdcnmf mtpOrdcnmf(ByteBuffer bb) {
        MtpOrdcnmf r = new MtpOrdcnmf();

        r.setDelayTimeStamp(System.nanoTime()); //TODO

        r.setIsix(bb.getShort());
        // 由 OrderMask ,解析成对应的属性
        short orderMask = bb.getShort();
        r.setProdBuySell(shiftBit(orderMask, 14, 2));
        r.setSellOut(shiftBit(orderMask, 12, 2));
        r.setResTrade(shiftBit(orderMask, 9, 3));
        r.setExecRestrict(shiftBit(orderMask, 6, 3));
        r.setOrderType(shiftBit(orderMask, 3, 3));
        r.setOrdrExePrc(getRealOrderExePrc(bb.getLong()));
        r.setOrdrQty(getRealOrderQty(bb.getLong()));
        r.setOrdrExeQty(getRealOrderQty(bb.getLong()));
        r.setOrdrNum(bb.getLong());
        r.setActnSeqNum(bb.getLong());
        r.setTranDatTim(bb.getLong());
        // 从 ByteBuffer 中获取  invAcctNo
        int invAcctNo = bb.getInt();
        r.setFfTxtGrp(ffTxtGrp(bb));
        r.setBrnId(ByteUtil.readCharSequence(bb, 5));
        // 从 ByteBuffer 中获取  invAcctTypCod
//        String s = (String) bb.get();
        // 增加拼接的 投资者账户
//        r.setInvAcctId(String.valueOf(bb.get()) + invAcctNo);
        r.setInvAcctId(String.valueOf((char)(bb.get())) + invAcctNo);
        r.setTrnTypId((char) bb.get());
        // 取出 ordrCod ,解析成对应的属性
        char ordrCod = (char) bb.get();
        r.setOrderStatus(shiftBit(ordrCod, 8, 1));
        r.setFullTrade(shiftBit(ordrCod, 9, 2));
        r.setAccountType(shiftBit(ordrCod, 11, 3));
        r.setCreditLabel(shiftBit(ordrCod, 14, 2));
        r.setFiller2(ByteUtil.readCharSequence(bb, 2));
        return r;
    }

    public static MtpTrdcnmf mtpTrdcnmf(ByteBuffer bb) {
        MtpTrdcnmf r = new MtpTrdcnmf();

        r.setDelayTimeStamp(System.nanoTime()); //TODO

        r.setTrdCfmBuy(simpTc(bb));
        r.setTrdCfmSell(simpTc(bb));
        return r;
    }

    private static FfTxtGrpT ffTxtGrp(ByteBuffer bb) {
        FfTxtGrpT r = new FfTxtGrpT();
        r.setUserOrdNum(ByteUtil.readCharSequence(bb, 16));
        r.setText(ByteUtil.readCharSequence(bb, 12));
        r.setClgPrtpId(ByteUtil.readCharSequence(bb, 5));
        r.setPbuOsCod(ByteUtil.readCharSequence(bb, 5));
        r.setPbuBizCod(ByteUtil.readCharSequence(bb, 5));
        return r;
    }

    private static SimpTcT simpTc(ByteBuffer bb) {
        SimpTcT r = new SimpTcT();
        r.setIsix(bb.getShort());
        // 由 OrderMask ,解析成对应的属性
        short i = bb.getShort();
        r.setProdBuySell(shiftBit(i, 14, 2));
        r.setSellOut(shiftBit(i, 12, 2));
        r.setResTrade(shiftBit(i, 9, 3));
        r.setExecRestrict(shiftBit(i, 6, 3));
        r.setOrderType(shiftBit(i, 3, 3));
        r.setTranIdNo(bb.getLong());
        r.setTradMtchPrc(getRealOrderExePrc(bb.getLong()));
        r.setTrdQty(getRealOrderQty(bb.getLong()));
        r.setOrdrNo(bb.getLong());
        r.setMktVal(getRealOrderExePrc(bb.getLong()).longValue());
        r.setOrdrQty(getRealOrderQty(bb.getLong()));
        r.setOrdrExeQty(getRealOrderQty(bb.getLong()));
        r.setOrdrEntTim(bb.getLong());
        r.setTotAucQty(bb.getLong());
        r.setActnSeqNum(bb.getLong());
        r.setOrdrExePrc(getRealOrderExePrc(bb.getLong()));
        r.setTranDatTim(bb.getLong());
        // 从 ByteBuffer 中获取  invAcctNo
        int invAcctNo = bb.getInt();
        r.setTrnTypId((char) bb.get());
        // 从 ByteBuffer 中获取  invAcctTypCod
        char invAcctTypCod = (char) (bb.get());
        r.setInvAcctId(invAcctTypCod + "" + invAcctNo);
        r.setTrdTyp((char) bb.get());
        // 取出 ordrCod ,解析成对应的属性
        char ordrCod = (char) bb.get();
        r.setOrderStatus(shiftBit(ordrCod, 8, 1));
        r.setFullTrade(shiftBit(ordrCod, 9, 2));
        r.setCreditLabel(shiftBit(ordrCod, 11, 3));
        r.setAccountType(shiftBit(ordrCod, 14, 2));
        r.setFfTxtGrpT(ffTxtGrp(bb));
        r.setCtpyPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setBrnId(ByteUtil.readCharSequence(bb, 5));
        r.setTranTypCod(ByteUtil.readCharSequence(bb, 3));
        r.setPrcTypCod((char) bb.get());
        return r;
    }

    private static PnBody10 pnBody10(ByteBuffer bb) {
        PnBody10 r = new PnBody10();
        r.setInvAcctTypCod((char) bb.get());
        r.setInvAcct(bb.getInt());
        r.setSuspSts((char) bb.get());
        r.setDateLstUpdDat(bb.getLong());
        return r;
    }

    private static PnBody11 pnBody11(ByteBuffer bb) {
        PnBody11 r = new PnBody11();
        r.setPbuIdCod(ByteUtil.readCharSequence(bb, 5));
        r.setSuspSts((char) bb.get());
        r.setDateLstUpdDat(bb.getLong());
        return r;
    }

    private static PnBody12 pnBody12(ByteBuffer bb) {
        PnBody12 r = new PnBody12();
        r.setInvAcctTypCod((char) bb.get());
        r.setInvAcct(bb.getInt());
        r.setIsix(bb.getShort());
        r.setSuspUnsuspQty(bb.getLong());
        r.setSuspSts((char) bb.get());
        r.setDateLstUpdDat(bb.getLong());
        return r;
    }

    private static void pnBody13(ByteBuffer bb, PnBody13 r) {
        r.setInvAcctTypCod((char) bb.get());
        r.setInvAcct(bb.getInt());
        r.setSrcMembIstIdCod(ByteUtil.readCharSequence(bb, 5));
        r.setTgtMembIstIdCod(ByteUtil.readCharSequence(bb, 5));
        r.setPIOrdNo(ByteUtil.readCharSequence(bb, 16));
        r.setText(ByteUtil.readCharSequence(bb, 12));
        r.setOrderNo(bb.getLong());
        r.setDateLstUpdDat(bb.getLong());
    }

    private static PnBody13 pnBody13(ByteBuffer bb) {
        PnBody13 r = new PnBody13();
        pnBody13(bb, r);
        return r;
    }

    private static PnBody21 pnBody21(ByteBuffer bb) {
        PnBody21 r = new PnBody21();
        pnBody13(bb, r);
        return r;
    }

    private static PnBody17 pnBody17(ByteBuffer bb) {
        PnBody17 r = new PnBody17();
        r.setPbuId(ByteUtil.readCharSequence(bb, 5));
        r.setInvAcctTypCod((char) bb.get());
        r.setInvtAcctID(bb.getInt());
        r.setUserOrdNum(ByteUtil.readCharSequence(bb, 16));
        r.setPassword(ByteUtil.readCharSequence(bb, 8));
        r.setTrasactionCode(ByteUtil.readCharSequence(bb, 12));
        r.setDateLstUpdDat(bb.getLong());
        return r;
    }

    private static MtpPn mtpPn(ByteBuffer bb) {
        MtpPn r = new MtpPn();
        r.setIsinSeqNo(bb.getLong());
        r.setIsix(bb.getShort());
        r.setMsgTyp(ByteUtil.readCharSequence(bb, 2));
        r.setNewsDatTim(bb.getLong());
        r.setNewsPrio((char) bb.get());
        r.setNewsSubj(ByteUtil.readCharSequence(bb, 55));
        r.setMktId(ByteUtil.readCharSequence(bb, 4));
        r.setMembExcIdCod(ByteUtil.readCharSequence(bb, 5));
        r.setNewsSeqNum(bb.getLong());
        r.setIsix2(bb.getShort());
        r.setExchMicId(ByteUtil.readCharSequence(bb, 3));
        r.setNewsKey(ByteUtil.readCharSequence(bb, 20));
        int rb = bb.position();
        switch (r.getMsgTyp()) {
            case AppConst.PN_BODY_10:
                r.setMsgBody(pnBody10(bb));
                break;
            case AppConst.PN_BODY_11:
                r.setMsgBody(pnBody11(bb));
                break;
            case AppConst.PN_BODY_12:
                r.setMsgBody(pnBody12(bb));
                break;
            case AppConst.PN_BODY_13:
                r.setMsgBody(pnBody13(bb));
                break;
            case AppConst.PN_BODY_17:
                r.setMsgBody(pnBody17(bb));
                break;
            case AppConst.PN_BODY_21:
                r.setMsgBody(pnBody21(bb));
                break;
            default:
                log.warn("mtpPn(): unknown type {}", r);
                //throw exception
        }
        rb = bb.position() - rb;
        if (rb < AppConst.PN_BODY_SIZE) {
            bb.position(bb.position() + AppConst.PN_BODY_SIZE - rb);
        }
        bb.position(bb.position() + 54);
        // NOTE: pn skip ending 54 bytes now
        return r;
    }

    private static MtpNc mtpNc(ByteBuffer bb) {
        MtpNc r = new MtpNc();
        r.setIsinSeqNo(bb.getLong());
        r.setIsix(bb.getShort());
        r.setNonTrdTypCod(ByteUtil.readCharSequence(bb, 2).trim());
        r.setPbuId(ByteUtil.readCharSequence(bb, 5).trim());
        r.setPartSubGrpIdCod(ByteUtil.readCharSequence(bb, 3).trim());
        r.setPartNoTxt(ByteUtil.readCharSequence(bb, 3).trim());
        r.setOsPbuId(ByteUtil.readCharSequence(bb, 5).trim());
        r.setPartOsSubGrpCod(ByteUtil.readCharSequence(bb, 3).trim());
        r.setPartOsNoTxt(ByteUtil.readCharSequence(bb, 3).trim());
        // 从 ByteBuffer 中获取  invAcctTypCod
        char invAcctTypCod = (char) bb.get();
        // 从 ByteBuffer 中获取  invAcctNo
        int invAcctNo = bb.getInt();
        r.setInvAcctId(invAcctTypCod + "" + invAcctNo);
        r.setOrdrNo(bb.getLong());
        r.setMaintainCode((char) bb.get());
        r.setDateLstUpdDat(bb.getLong());
        r.setOrdrEntTim(bb.getLong());
        r.setOrdrQty(bb.getLong());
        r.setOrdrAmt(bb.getLong());
        r.setOrdrPrc(bb.getLong());
        r.setAcctTypCod((char) bb.get());
        r.setBrnId(ByteUtil.readCharSequence(bb, 5));
        r.setUserOrdNum(ByteUtil.readCharSequence(bb, 16));
        r.setText(ByteUtil.readCharSequence(bb, 12));
        r.setHldFromTo(ByteUtil.readCharSequence(bb, 20));
        r.setDivSelct((char) bb.get());
        r.setToFundId(ByteUtil.readCharSequence(bb, 12));
        r.setMainIntention(ByteUtil.readCharSequence(bb, 4));
        r.setSubIntention(ByteUtil.readCharSequence(bb, 4));
        r.setPreference(ByteUtil.readCharSequence(bb, 23));
        return r;
    }

    public static int shiftBit(int value, int start, int length) {
        return (value >>> (16 - start - length) & 0xFFFF >>> (16 - length));
    }

    public static BigDecimal getRealOrderExePrc(long rawOrderExePrc) {
        return BigDecimal.valueOf(rawOrderExePrc).multiply(new BigDecimal(AppConst.ORDER_PRICE_SCALE));
    }

    public static BigDecimal getRealOrderQty(long rawOrderQty) {
        return BigDecimal.valueOf(rawOrderQty).multiply(new BigDecimal(AppConst.ORDER_QTY_SCALE));
    }
}
