package com.sse.rcp.dao;

import com.sse.rcp.domains.dimension.YmtAccount;

import java.util.List;

public interface YmtDao {

    List<YmtAccount> findAll(Integer rowNum , Integer offset);

    Long getCounts();

    int insertYmt(YmtAccount ymtAccount );
}
