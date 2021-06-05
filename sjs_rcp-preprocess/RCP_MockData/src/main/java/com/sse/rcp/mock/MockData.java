package com.sse.rcp.mock;


import com.alibaba.fastjson.JSONObject;
import com.sse.rcp.domains.ezei.EzEIData;
import com.sse.rcp.domains.ezei.TopicHeader;
import com.sse.rcp.domains.ezei.mtp.FfTxtGrpT;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class MockData {

    public static Character[] trnTypId = {'A', 'X', 'D', 'A', 'A'};

    public static String[] invAcctId = {
            "D890436998", "A247093318", "A288011198", "B100000001", "A247479580",
            "A527520505", "D890436142", "F987654348", "D585575645", "B585575637",
            "D890012752", "B880413566", "A288011178", "A319765725", "A288011113",
            "A127976621", "A538961675", "F987654343", "D585575654", "A288011129",
            "A268316572", "D300000005", "F987654340", "C200000006", "D890757695",
            "A748466344", "A395077558", "A288011186", "A247479433", "H123456800",
            "A288011130", "A383942941", "A128051759", "A288011169", "D890712213",
            "A288011140", "A158165627", "A247479881", "A288011138", "C200000010",
            "F987654369", "A745969767", "H123456793", "D890436809", "D585575647",
            "A537317116", "A415028114", "A247479417", "A158900108", "A950000021",
            "A247480125", "F987654360", "B881028453", "A247481587", "A288011157",
            "A288189856", "A288011201", "C900000004", "A403275569", "A247481749",
            "F987654333", "F987654357", "C900000001", "H123456798", "A288011159",
            "A275001447", "F987654351", "D890437091", "D890436150", "A288011148",
            "F987654330", "A288011116", "H123456806", "B880904339", "B900000002",
            "A288011180", "B880826222", "H123456805", "B880797512", "A247480214",
            "B881010531", "A950000031", "A247093318", "D890436079", "A288188711",
            "A288011181", "D900000003", "A288011162", "H123456804", "A411922358"};

    public static String[] tranTypCod = {"015", "016", "017", "013", "000", "012", "018", "999"};
    public static Character[] prcTypCod = {'C', 'O', 'A', 'F', 'N', 'T', 'E', '1', '2', '3', '4', 'W', 'L', 'D', 'B'};

    public static MtpOrdcnmf generateOCData(AtomicLong seq) {
        EzEIData ezEIData = new EzEIData();
        TopicHeader topicHdr = ezEIData.getTopicHdr();

        Random r = new Random();
        topicHdr.setRecSeq(seq.get());
        topicHdr.setExist(1);
        MtpOrdcnmf mtpOrdcnmf = new MtpOrdcnmf();

        mtpOrdcnmf.setIsix(r.nextInt(10) + 1021);
//        mtpOrdcnmf.setProdBuySell(r.nextInt(2));
//        mtpOrdcnmf.setSellOut(r.nextInt(2));
//        mtpOrdcnmf.setResTrade(r.nextInt(5));
//        mtpOrdcnmf.setExecRestrict(r.nextInt(7));
//        mtpOrdcnmf.setOrderType(r.nextInt(5));
        mtpOrdcnmf.setProdBuySell(0);
        mtpOrdcnmf.setSellOut(1);
        mtpOrdcnmf.setResTrade(0);
        mtpOrdcnmf.setExecRestrict(1);
        mtpOrdcnmf.setOrderType(0);
        ////////////////////////////////////////////////////////
        mtpOrdcnmf.setOrdrExePrc(new BigDecimal(r.nextInt(10) * 100000 + 1000000));
        mtpOrdcnmf.setOrdrQty(new BigDecimal(r.nextInt(10) * 10000 + 1000000));
        mtpOrdcnmf.setOrdrExeQty(new BigDecimal(r.nextInt(10) * 10000 + 1000000));
        mtpOrdcnmf.setOrdrNum(3334445556667778L + r.nextInt(Integer.MAX_VALUE));
        mtpOrdcnmf.setActnSeqNum(seq.get());
        mtpOrdcnmf.setTranDatTim(Long.valueOf(String.format("%.16s", new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))));
        FfTxtGrpT ffTxtGrp = mtpOrdcnmf.getFfTxtGrp();
        if (ffTxtGrp == null) {
            ffTxtGrp = new FfTxtGrpT();
            ffTxtGrp.setUserOrdNum(String.valueOf(3000000000000000L + seq.get()));
            ffTxtGrp.setText("模拟申报数据");
            ffTxtGrp.setClgPrtpId("12345");
            ffTxtGrp.setPbuOsCod("67890");
            ffTxtGrp.setPbuBizCod("01234");
        }

        mtpOrdcnmf.setFfTxtGrp(ffTxtGrp);
        mtpOrdcnmf.setBrnId("99999");
        mtpOrdcnmf.setInvAcctId(invAcctId[r.nextInt(90)]);
        mtpOrdcnmf.setTrnTypId(trnTypId[r.nextInt(4)]);
//        mtpOrdcnmf.setAccountType(r.nextInt(3));
//        mtpOrdcnmf.setCreditLabel(r.nextInt(5));
//        mtpOrdcnmf.setFullTrade(r.nextInt(2));
        mtpOrdcnmf.setAccountType(2);
        mtpOrdcnmf.setCreditLabel(4);
        mtpOrdcnmf.setFullTrade(3);
        mtpOrdcnmf.setOrderStatus(0); //订单状态为未冻结
        mtpOrdcnmf.setFiller2("FR");

        ezEIData.setTopicData(mtpOrdcnmf);
        return mtpOrdcnmf;
    }

    public static MtpTrdcnmf generateTCData(AtomicLong seq) {

        EzEIData ezEIData = new EzEIData();
        TopicHeader topicHdr = ezEIData.getTopicHdr();

        Random r = new Random();
        topicHdr.setRecSeq(seq.get());
        topicHdr.setExist(1);
        MtpTrdcnmf mtpTrdcnmf = new MtpTrdcnmf();

        SimpTcT trdCfmBuy = mtpTrdcnmf.getTrdCfmBuy();
        SimpTcT trdCfmSell = mtpTrdcnmf.getTrdCfmSell();

        if (trdCfmBuy == null) {
            trdCfmBuy = new SimpTcT();
        }

        if (trdCfmSell == null) {
            trdCfmSell = new SimpTcT();
        }

        /**
         *  如果产生的随机数为 0 ，则使得买方的执行动作序列号 小
         *  如果产生的随机数为 1 ，则使得卖方的执行动作序列号 小
         *
         */
        if ((int) (Math.random() * 2) == 0) {
            trdCfmBuy.setActnSeqNum(seq.get());
            trdCfmSell.setActnSeqNum(seq.get() + 1);
        } else {
            trdCfmBuy.setActnSeqNum(seq.get() + 1);
            trdCfmSell.setActnSeqNum(seq.get());
        }

        trdCfmBuy.setIsix(r.nextInt(10) + 1021);
//        trdCfmBuy.setProdBuySell(r.nextInt(2));
//        trdCfmBuy.setSellOut(r.nextInt(2));
//        trdCfmBuy.setResTrade(r.nextInt(5));
//        trdCfmBuy.setExecRestrict(r.nextInt(7));
//        trdCfmBuy.setOrderType(r.nextInt(5));
        trdCfmBuy.setProdBuySell(1);
        trdCfmBuy.setSellOut(1);
        trdCfmBuy.setResTrade(1);
        trdCfmBuy.setExecRestrict(0);
        trdCfmBuy.setOrderType(0);
        trdCfmBuy.setTranIdNo(r.nextInt(2000000) + 2000000);

        BigDecimal orderPrice = new BigDecimal(r.nextInt(10) * 100000 + 1000000);

        BigDecimal trdQty = new BigDecimal(r.nextInt(10) * 100000 + 1000000);

        trdCfmBuy.setTradMtchPrc(orderPrice);
        trdCfmBuy.setTrdQty(trdQty);
        trdCfmBuy.setOrdrNo(5590020000000001L + seq.get());
        trdCfmBuy.setMktVal(orderPrice.multiply(trdQty).longValue());
        trdCfmBuy.setOrdrQty(new BigDecimal(r.nextInt(10) * 10000 + 1000000));
        trdCfmBuy.setOrdrExeQty(new BigDecimal(r.nextInt(10) * 10000 + 1000000));
        trdCfmBuy.setOrdrEntTim(Long.valueOf(String.format("%.16s", new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))));
        trdCfmBuy.setTotAucQty(r.nextInt(100) + 100); // 透传的数据此处为 0

        trdCfmBuy.setOrdrExePrc(orderPrice);
        trdCfmBuy.setTranDatTim(Long.valueOf(String.format("%.16s", new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))));
        trdCfmBuy.setTrnTypId('E');
        trdCfmBuy.setInvAcctId(invAcctId[r.nextInt(90)]);
        trdCfmBuy.setTrdTyp(trnTypId[r.nextInt(4)]);
        trdCfmBuy.setAccountType(r.nextInt(3));
        trdCfmBuy.setCreditLabel(r.nextInt(5));
        trdCfmBuy.setFullTrade(r.nextInt(2));
        trdCfmBuy.setOrderStatus(0); //订单状态为未冻结
        FfTxtGrpT ffTxtGrpT = trdCfmBuy.getFfTxtGrpT();
        if (ffTxtGrpT == null) {
            ffTxtGrpT = new FfTxtGrpT();

            ffTxtGrpT.setUserOrdNum(String.valueOf(3000000000000000L + seq.get()));
            ffTxtGrpT.setText("模拟成交数据");
            ffTxtGrpT.setClgPrtpId("12345");
            ffTxtGrpT.setPbuOsCod("54321");
            ffTxtGrpT.setPbuBizCod("98765");
        }
        trdCfmBuy.setFfTxtGrpT(ffTxtGrpT);

        trdCfmBuy.setCtpyPbuId("66666");
        trdCfmBuy.setBrnId("99999");
        trdCfmBuy.setTranTypCod(tranTypCod[r.nextInt(8)]);
        trdCfmBuy.setPrcTypCod(prcTypCod[r.nextInt(15)]);

        trdCfmSell.setIsix(r.nextInt(10) + 1021);
//        trdCfmSell.setProdBuySell(r.nextInt(2));
//        trdCfmSell.setSellOut(r.nextInt(2));
//        trdCfmSell.setResTrade(r.nextInt(5));
//        trdCfmSell.setExecRestrict(r.nextInt(7));
//        trdCfmSell.setOrderType(r.nextInt(5));
        trdCfmSell.setProdBuySell(0);
        trdCfmSell.setSellOut(0);
        trdCfmSell.setResTrade(0);
        trdCfmSell.setExecRestrict(1);
        trdCfmSell.setOrderType(1);
        ////////////////////////////////////////////////
        trdCfmSell.setTranIdNo(r.nextInt(2000000) + 2000000);

        trdCfmSell.setTradMtchPrc(orderPrice);
        trdCfmSell.setTrdQty(trdQty);
        trdCfmSell.setOrdrNo(859002000000000L + seq.get());
        trdCfmSell.setMktVal(orderPrice.multiply(trdQty).longValue());
        trdCfmSell.setOrdrQty(new BigDecimal(r.nextInt(10) * 10000 + 1000000));
        trdCfmSell.setOrdrExeQty(new BigDecimal(r.nextInt(10) * 10000 + 1000000));
        trdCfmSell.setOrdrEntTim(Long.valueOf(String.format("%.16s", new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))));
        trdCfmSell.setTotAucQty(r.nextInt(100) + 100); // 透传的数据此处为 0

        trdCfmSell.setOrdrExePrc(orderPrice);
        trdCfmSell.setTranDatTim(Long.valueOf(String.format("%.16s", new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))));
        trdCfmSell.setTrnTypId('E');
        trdCfmSell.setInvAcctId(invAcctId[r.nextInt(90)]);
        trdCfmSell.setTrdTyp(trnTypId[r.nextInt(4)]);
        trdCfmSell.setAccountType(r.nextInt(3));
        trdCfmSell.setCreditLabel(r.nextInt(5));
        trdCfmSell.setFullTrade(r.nextInt(2));
        trdCfmSell.setOrderStatus(0); //订单状态为未冻结
        FfTxtGrpT sellffTxtGrpT = trdCfmSell.getFfTxtGrpT();
        if (sellffTxtGrpT == null) {
            sellffTxtGrpT = new FfTxtGrpT();

            sellffTxtGrpT.setUserOrdNum(String.valueOf(3000000000000000L + seq.get()));
            sellffTxtGrpT.setText("模拟成交数据");
            sellffTxtGrpT.setClgPrtpId("12345");
            sellffTxtGrpT.setPbuOsCod("54321");
            sellffTxtGrpT.setPbuBizCod("98765");
        }
        trdCfmSell.setFfTxtGrpT(sellffTxtGrpT);

        trdCfmSell.setCtpyPbuId("66666");
        trdCfmSell.setBrnId("99999");
        trdCfmSell.setTranTypCod(tranTypCod[r.nextInt(8)]);
        trdCfmSell.setPrcTypCod(prcTypCod[r.nextInt(15)]);

        mtpTrdcnmf.setTrdCfmBuy(trdCfmBuy);
        mtpTrdcnmf.setTrdCfmSell(trdCfmSell);

        ezEIData.setTopicData(mtpTrdcnmf);
        return mtpTrdcnmf;
    }
}
