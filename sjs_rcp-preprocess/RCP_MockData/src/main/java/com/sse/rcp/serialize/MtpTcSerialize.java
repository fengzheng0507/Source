package com.sse.rcp.serialize;


import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.SerializationException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class MtpTcSerialize {

    private static final Charset CHARSET = Charset.forName("GBK");

    public static byte[] serialize(MtpTrdcnmf tc) {
        try {
            if (tc == null) {
                return null;
            } else {

                SimpTcT buy = tc.getTrdCfmBuy();
                SimpTcT sell = tc.getTrdCfmSell();


                ByteBuffer bb = ByteBuffer.allocate(373);
                bb.put(String.format("sse.mtp.ordcnmf.0004%s", StringUtils.repeat("thisiscolumn",1)).getBytes(), 0, 32);//  ezeiTopic  32byte
                bb.putInt(1);             // 消息条数，暂定为1   RecCount 4byte
                bb.putShort((short) 330);  // 单条消息长度，暂定为600  RecLen 2byte
                bb.putInt(1);             // RecSeq 4byte
                bb.put((byte) 1);          // Exist  1byte

                bb.putShort((short) buy.getIsix());//  2byte
                bb.putShort((short) (bit2Int(buy.getOrderType(), 3, 3) + bit2Int(buy.getExecRestrict(), 6, 3) + bit2Int(buy.getResTrade(), 9, 3) + bit2Int(buy.getSellOut(), 12, 2) + bit2Int(buy.getProdBuySell(), 14, 2)));//2Byte
                bb.putLong(buy.getTranIdNo());// 8byte
                bb.putLong(buy.getTradMtchPrc().longValue());// 8byte
                bb.putLong(buy.getTrdQty().longValue()); // 8byte
                bb.putLong(buy.getOrdrNo()); // 8byte
                bb.putLong(buy.getMktVal()); // 8byte
                bb.putLong(buy.getOrdrQty().longValue());  // 8byte
                bb.putLong(buy.getOrdrExeQty().longValue());  // 8byte
                bb.putLong(buy.getOrdrEntTim());  // 8byte
                bb.putLong(buy.getTotAucQty());   // 8byte
                bb.putLong(buy.getActnSeqNum());  // 8byte
                bb.putLong(buy.getOrdrExePrc().longValue()); // 8byte
                bb.putLong(buy.getTranDatTim()); // 8byte

                bb.putInt(Integer.valueOf(buy.getInvAcctId().substring(1)));// 4byte

                bb.put(((char) buy.getTrnTypId() + "").getBytes(), 0, 1);//1byte
                bb.put(buy.getInvAcctId().substring(0, 1).getBytes()); // 1byte
//                bb.put((byte)buy.getTrdTyp());
                bb.put((buy.getTrdTyp() + "").getBytes()); // 1byte

                bb.put((byte)( bit2Byte(buy.getOrderStatus(), 0, 1) +
                        bit2Byte(buy.getFullTrade(), 1, 2) +
                        bit2Byte(buy.getCreditLabel(), 3, 3)+
                        bit2Byte(buy.getAccountType(), 6, 2)));

                //  FfTxtGrp 结构体 开始
                bb.put(buy.getFfTxtGrpT().getUserOrdNum().getBytes(), 0, 16); // UserOrdNum 16Byte
                bb.put(buy.getFfTxtGrpT().getText().getBytes(CHARSET), 0, 12);// Text 12Byte
                bb.put(buy.getFfTxtGrpT().getClgPrtpId().getBytes(), 0, 5);  // ClgPrtpId 5Byte
                bb.put(buy.getFfTxtGrpT().getPbuOsCod().getBytes(), 0, 5);   // PbuOsCod  5Byte
                bb.put(buy.getFfTxtGrpT().getPbuBizCod().getBytes(), 0, 5);  // PbuBizCod 5Byte
                //  FfTxtGrp 结构体 结束

                bb.put(buy.getCtpyPbuId().getBytes(), 0, 5); // 5byte
                bb.put(buy.getBrnId().getBytes(),0,5);       // 5byte
                bb.put(buy.getTranTypCod().getBytes(),0,3);  // 3byte
//                bb.put((buy.getPrcTypCod()+"").getBytes(charset));
                bb.put((buy.getPrcTypCod()+"").getBytes(),0,1); // 1byte
                ///////////////////////////////////////////////////////////////////////////////
                bb.putShort((short) sell.getIsix());//  2byte
                bb.putShort((short) (bit2Int(sell.getOrderType(), 3, 3) + bit2Int(sell.getExecRestrict(), 6, 3) + bit2Int(sell.getResTrade(), 9, 3) + bit2Int(sell.getSellOut(), 12, 2) + bit2Int(sell.getProdBuySell(), 14, 2)));//2Byte
                bb.putLong(sell.getTranIdNo());// 8byte
                bb.putLong(sell.getTradMtchPrc().longValue());// 8byte
                bb.putLong(sell.getTrdQty().longValue()); // 8byte
                bb.putLong(sell.getOrdrNo()); // 8byte
                bb.putLong(sell.getMktVal()); // 8byte
                bb.putLong(sell.getOrdrQty().longValue());  // 8byte
                bb.putLong(sell.getOrdrExeQty().longValue());  // 8byte
                bb.putLong(sell.getOrdrEntTim());  // 8byte
                bb.putLong(sell.getTotAucQty());   // 8byte
                bb.putLong(sell.getActnSeqNum());  // 8byte
                bb.putLong(sell.getOrdrExePrc().longValue()); // 8byte
                bb.putLong(sell.getTranDatTim()); // 8byte

                bb.putInt(Integer.valueOf(sell.getInvAcctId().substring(1)));// 4byte

                bb.put((sell.getTrnTypId() + "").getBytes(), 0, 1);//1byte
                bb.put(sell.getInvAcctId().substring(0, 1).getBytes()); // 1byte
//                bb.put((byte)buy.getTrdTyp());
                bb.put((sell.getTrdTyp() + "").getBytes()); // 1byte


                bb.put((byte)( bit2Byte(sell.getOrderStatus(), 0, 1) +
                        bit2Byte(sell.getFullTrade(), 1, 2) +
                        bit2Byte(sell.getCreditLabel(), 3, 3)+
                        bit2Byte(sell.getAccountType(), 6, 2)));
                //  FfTxtGrp 结构体 开始
                bb.put(sell.getFfTxtGrpT().getUserOrdNum().getBytes(), 0, 16); // UserOrdNum 16Byte
                bb.put(sell.getFfTxtGrpT().getText().getBytes(), 0, 12);// Text 12Byte
                bb.put(sell.getFfTxtGrpT().getClgPrtpId().getBytes(), 0, 5);  // ClgPrtpId 5Byte
                bb.put(sell.getFfTxtGrpT().getPbuOsCod().getBytes(), 0, 5);   // PbuOsCod  5Byte
                bb.put(sell.getFfTxtGrpT().getPbuBizCod().getBytes(), 0, 5);  // PbuBizCod 5Byte
                //  FfTxtGrp 结构体 结束

                bb.put(sell.getCtpyPbuId().getBytes(), 0, 5); // 5byte
                bb.put(sell.getBrnId().getBytes(),0,5);       // 5byte
                bb.put(sell.getTranTypCod().getBytes(),0,3);  // 3byte
//                bb.put((buy.getPrcTypCod()+"").getBytes(charset));
                bb.put((sell.getPrcTypCod()+"").getBytes(),0,1); // 1byte
                bb.order(ByteOrder.LITTLE_ENDIAN);
                return bb.array();
            }
        } catch (Exception e) {
            throw new SerializationException("Error when serializing mock tc data :" + e);
        }

    }

    public static int bit2Int(int value, int start, int length) {
        return (value << (16 - start - length));
    }

    public static int bit2Byte(int value, int start, int length) {
        return (value << (8 - start - length));
    }
}

