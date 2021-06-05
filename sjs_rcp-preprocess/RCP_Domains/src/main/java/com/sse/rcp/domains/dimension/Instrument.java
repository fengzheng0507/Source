package com.sse.rcp.domains.dimension;

import lombok.Data;

import java.io.Serializable;

@Data
public class Instrument implements Serializable {

    /**
     * isix int 产品isix编号
     */
    private int isix;

    /**
     * isin  String  证券12位国际代码
     */
    private String isin;

    /**
     * instrumentId  String 证券6位国内代码
     */
    private String instrumentId;
}
