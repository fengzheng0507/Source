package com.sse.rcp.dao;

import com.sse.rcp.domains.dimension.Instrument;

import java.util.List;

public interface InstrumentDao {

    List<Instrument> findAll(Integer rowNum , Integer offset) ;

    Long getCounts();

    int insertInstrument(Instrument instrument );

}
