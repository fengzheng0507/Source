package com.sse.rcp.serialize;


import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.SerializationException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class MtpOcSerialize {

    private static final Charset CHARSET = Charset.forName("GBK");

    public static byte[] serialize(MtpOrdcnmf oc) {
        try {
            if (oc == null) {
                return null;
            } else {
                ByteBuffer buffer = ByteBuffer.allocate(152);
                buffer.put(String.format("sse.mtp.ordcnmf.0004%s", StringUtils.repeat("thisiscolumn",1)).getBytes(), 0, 32);//  ezeiTopic  32byte
                buffer.putInt(1);             //   RecCount 4byte
                buffer.putShort((short) 109);  //  RecLen 2byte
                buffer.putInt(1);             // RecSeq 4byte
                buffer.put((byte) 1);          // Exist  1byte
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.putShort((short) oc.getIsix());//  2byte
                buffer.putShort((short) (bit2Int(oc.getOrderType(), 3, 3) + bit2Int(oc.getExecRestrict(), 6, 3) + bit2Int(oc.getResTrade(), 9, 3) + bit2Int(oc.getSellOut(), 12, 2) + bit2Int(oc.getProdBuySell(), 14, 2)));
                buffer.putLong(oc.getOrdrExePrc().longValue());// OrdrExePrc 8byte
                buffer.putLong(oc.getOrdrQty().longValue());   // OrdrQty 8byte
                buffer.putLong(oc.getOrdrExeQty().longValue());// OrdrExeQty 8byte
                buffer.putLong(oc.getOrdrNum());               // OrdrNum 8byte
                buffer.putLong(oc.getActnSeqNum());            // ActnSeqNum 8byte
                buffer.putLong(oc.getTranDatTim());            // TranDatTim 8byte
                buffer.putInt(Integer.valueOf(oc.getInvAcctId().substring(1, oc.getInvAcctId().length())));// InvAcctId 4byte
                //  FfTxtGrp
                buffer.put(oc.getFfTxtGrp().getUserOrdNum().getBytes(), 0, 16); // UserOrdNum 16Byte
                buffer.put(oc.getFfTxtGrp().getText().getBytes(CHARSET), 0, 12);// Text 12Byte
                buffer.put(oc.getFfTxtGrp().getClgPrtpId().getBytes(), 0, 5);  // ClgPrtpId 5Byte
                buffer.put(oc.getFfTxtGrp().getPbuOsCod().getBytes(), 0, 5);   // PbuOsCod  5Byte
                buffer.put(oc.getFfTxtGrp().getPbuBizCod().getBytes(), 0, 5);  // PbuBizCod 5Byte
                //  FfTxtGrp
                buffer.put(oc.getBrnId().getBytes(), 0, 5);        // BrnId 5Byte
                //
                buffer.put(oc.getInvAcctId().getBytes(), 0, 1); // InvAcctId 2Byte
                buffer.put((oc.getTrnTypId() + "").getBytes(), 0, 1);            // TrnTypId 2Byte

                buffer.put((byte) (bit2Byte(oc.getOrderStatus(), 0, 1) +
                        bit2Byte(oc.getFullTrade(), 1, 2) +
                        bit2Byte(oc.getCreditLabel(), 3, 3) +
                        bit2Byte(oc.getAccountType(), 6, 2)));
                buffer.put(oc.getFiller2().getBytes(), 0, 2);  // Filler2 2Byte
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                return buffer.array();
            }
        } catch (Exception e) {
            throw new SerializationException("Error when serializing mock oc data :" + e);
        }

    }

    public static int bit2Int(int value, int start, int length) {
        return (value << (16 - start - length));
    }

    public static int bit2Byte(int value, int start, int length) {
        return (value << (8 - start - length));
    }
}
