package com.sse.rcp.domains.dimension;


import lombok.Data;

import java.io.Serializable;

@Data
public class YmtAccount implements Serializable {

    /**
     * 投资者账户
     */
    private String investorAccountId;

    /**
     * 一码通账户
     */
    private String ymtAccountId;

    /**
     * 一码通户名
     */
    private String ymtAccountName;
}
