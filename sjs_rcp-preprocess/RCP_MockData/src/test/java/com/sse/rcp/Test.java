package com.sse.rcp;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

    public static void main(String[] args) {


        String thisiscolumn = String.format("sse.mtp.ordcnmf.0004%s", StringUtils.repeat("thisiscolumn", 1));
        System.out.println(thisiscolumn.length());
        System.out.println(thisiscolumn);
    }
}
