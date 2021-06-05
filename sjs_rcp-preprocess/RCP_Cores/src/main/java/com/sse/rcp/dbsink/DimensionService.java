package com.sse.rcp.dbsink;

import com.sse.rcp.dao.InstrumentDao;
import com.sse.rcp.dao.YmtDao;
import com.sse.rcp.domains.dimension.Instrument;
import com.sse.rcp.domains.dimension.YmtAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DimensionService {
    @Autowired
    private InstrumentDao instrumentDao;

    @Autowired
    private YmtDao ymtDao;

    public List<YmtAccount> findAllYmt(Integer rowNum, Integer offset) {
        return ymtDao.findAll(rowNum, offset);
    }

    public List<Instrument> findAllInstrument(Integer rowNum, Integer offset) {
        return instrumentDao.findAll(rowNum, offset);
    }

    public Long getYmtCounts() {
        return ymtDao.getCounts();
    }

    public Long getInstrumentCounts() {
        return instrumentDao.getCounts();
    }

    public int insertYmt(YmtAccount ymtAccount) {
        return ymtDao.insertYmt(ymtAccount);
    }

    public int insertInstrument(Instrument instrument) {
        return instrumentDao.insertInstrument(instrument);
    }
}
