package com.sse.rcp.dao.impl;

import com.sse.rcp.domains.dimension.Instrument;

import com.sse.rcp.dao.InstrumentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentDaoImpl implements InstrumentDao {


    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @Override
//    public List<Instrument> findAll() {
//        String sql = String.format(" select * from  pd_stage.act_inst_account_rec  ");
//        List<Instrument> instrumentList = jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<Instrument>(Instrument.class));
//        return instrumentList;
//    }

    @Override
    public List<Instrument> findAll(Integer rowNum, Integer offsetNum) {
        String sql = String.format(" select * from pd_stage.act_inst_account_rec  limit %d  offset %d", rowNum, offsetNum);
        List<Instrument> instrumentList = jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<Instrument>(Instrument.class));
        return instrumentList;
    }

    @Override
    public Long getCounts() {
        String sql = " select count(*) from pd_stage.act_inst_account_rec ;";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }


    @Override
    public int insertInstrument(Instrument instrument) {
        String sql = " INSERT INTO  pd_stage.act_inst_account_rec   (isix, isin, instrument_id) VALUES (?, ?, ?);";
        Integer isix = instrument.getIsix();
        String isin = instrument.getIsin();
        String instrumentId = instrument.getInstrumentId();
        return jdbcTemplate.update(sql, isix, isin, instrumentId);
    }
}
