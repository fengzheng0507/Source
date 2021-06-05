package com.sse.rcp.domains.utils;

import com.sse.rcp.domains.constant.AppConst;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteUtil {
    public static String toStringUntilZero(byte[] bs, Charset charset) {
        int idx;
        for (idx = 0; idx < bs.length; idx++) {
            if (bs[idx] == 0) {
                break;
            }
        }
        if (idx == 0) {
            return "";
        }
        if (idx == bs.length) {
            return new String(bs, charset);
        }
        return new String(bs, 0, idx, charset);
    }

    public static String readCharSequence(ByteBuffer bb, int length) {
        // NOTE: if it contains '\0', it be ' ', it's wrong
        byte[] bs = new byte[length];
        bb.get(bs);
        return toStringUntilZero(bs, AppConst.CHARSET).trim();
    }
}
