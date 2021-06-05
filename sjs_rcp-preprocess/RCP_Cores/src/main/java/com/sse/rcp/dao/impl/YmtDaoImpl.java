package com.sse.rcp.dao.impl;

import com.sse.rcp.domains.dimension.YmtAccount;
import com.sse.rcp.dao.YmtDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YmtDaoImpl implements YmtDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
//
//    @Override
//    public List<YmtAccount> findAll() {
//        String sql = " SELECT *  FROM pd_dwd.act_ymt_account_rec where ymt_account_id is not null  and ymt_account_name is not null ";
//        List<YmtAccount> ymtAccountList = jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<>(YmtAccount.class));
//        return ymtAccountList;
//    }

    @Override
    public List<YmtAccount> findAll(Integer rowNum, Integer offset) {
        String sql = String.format(" select *  from pd_stage.act_ymt_account_rec  limit %d offset %d", rowNum, offset);
        List<YmtAccount> ymtAccountList = jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<>(YmtAccount.class));
        return ymtAccountList;
    }

    @Override
    public Long getCounts() {
//        String sql = " select count(*) from  pd_dwd.act_ymt_account_rec   where ymt_account_id is not null  and ymt_account_name is not null ";
        String sql = " select count(*) from pd_stage.act_ymt_account_rec    ";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    public int insertYmt(YmtAccount ymtAccount) {

        String sql1 = "INSERT INTO pd_stage.act_ymt_account_rec (investorAccountId, ymtAccountId, ymtAccountName) VALUES (?, ?, ?); ";

        String investorAccountId = ymtAccount.getInvestorAccountId();
        String ymtAccountId = ymtAccount.getYmtAccountId();
        String ymtAccountName = ymtAccount.getYmtAccountName();
        return jdbcTemplate.update(sql1, investorAccountId, ymtAccountId, ymtAccountName);
    }
}
