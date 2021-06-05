package com.sse.rcp.domains.constant;

import java.nio.charset.Charset;

public interface AppConst {
    /**
     * 编码格式
     */
    Charset CHARSET = Charset.forName("GBK");

    String ORDER_PRICE_SCALE = "0.00001";
    String ORDER_QTY_SCALE = "0.001";
    long SEQ_SCALE = 10000_000_000_000L;
    long SEQ_TAG_SCALE = 1000_000_000L;

    String EMPTY = "";
    String SPLIT_COMMA = ",";
    String SPLIT_COLON = ":";
    String SPLIT_DASH = "-";

    String NO_REGULAR_DOC = "\\.";
    String REGULAR_BLANK = "\\s";
    String REGULAR_ENTER = "\n";
    /**
     * MTP_PN结构体大小
     */
    int PN_BODY_SIZE = 350;
    String PN_BODY_10 = "10";
    String PN_BODY_11 = "11";
    String PN_BODY_12 = "12";
    String PN_BODY_13 = "13";
    String PN_BODY_17 = "17";
    String PN_BODY_21 = "21";
}
