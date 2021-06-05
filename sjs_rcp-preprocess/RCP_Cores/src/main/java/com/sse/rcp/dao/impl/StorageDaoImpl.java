package com.sse.rcp.dao.impl;

import com.sse.rcp.dao.StorageDao;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.order.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.lang.model.element.NestingKind;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service("storageDao")
public class StorageDaoImpl implements StorageDao {
    private final static String SQL_ORDBOOK = " INSERT INTO pd_stage.txn_ordbook_h\n" +
            "(\"SETNO\", \"ACTSEQNO\", \"ORDRNO\", \"LAST_ORDRNO\", \"ISINCOD\", \"INSTRUMENT_ID\", " +
            "\"ORDTYPE\", \"LAST_ORDTYPE\", \"ORDRBUYCOD\", \"LEVELPRICE\", \"PRICE\", \"ORDRQTY\", " +
            "\"BUYPRICE1\", \"BUYVOLUME1\", \"SELLPRICE1\", \"SELLVOLUME1\", \"BUYPRICE2\", \"BUYVOLUME2\", " +
            "\"SELLPRICE2\", \"SELLVOLUME2\", \"BUYPRICE3\", \"BUYVOLUME3\", \"SELLPRICE3\", \"SELLVOLUME3\", " +
            "\"BUYPRICE4\", \"BUYVOLUME4\", \"SELLPRICE4\", \"SELLVOLUME4\", \"BUYPRICE5\", \"BUYVOLUME5\", " +
            "\"SELLPRICE5\", \"SELLVOLUME5\", \"BUYPRICE6\", \"BUYVOLUME6\", \"SELLPRICE6\", \"SELLVOLUME6\", " +
            "\"BUYPRICE7\", \"BUYVOLUME7\", \"SELLPRICE7\", \"SELLVOLUME7\", \"BUYPRICE8\", \"BUYVOLUME8\", " +
            "\"SELLPRICE8\", \"SELLVOLUME8\", \"BUYPRICE9\", \"BUYVOLUME9\", \"SELLPRICE9\", \"SELLVOLUME9\", " +
            "\"BUYPRICE10\", \"BUYVOLUME10\", \"SELLPRICE10\", \"SELLVOLUME10\", \"INVESTORACCOUNTID\", \"YMTACCOUNTID\", " +
            "\"YMTBUYORDVOLUME1\", \"YMTBUYPRICE1\", \"YMTBUYORDVOLUME2\", \"YMTBUYPRICE2\", \"YMTBUYORDVOLUME3\", \"YMTBUYPRICE3\", " +
            "\"YMTBUYORDVOLUME4\", \"YMTBUYPRICE4\", \"YMTBUYORDVOLUME5\", \"YMTBUYPRICE5\", \"YMTSELLORDVOLUME1\", \"YMTSELLPRICE1\", " +
            "\"YMTSELLORDVOLUME2\", \"YMTSELLPRICE2\", \"YMTSELLORDVOLUME3\", \"YMTSELLPRICE3\", \"YMTSELLORDVOLUME4\", \"YMTSELLPRICE4\", " +
            "\"YMTSELLORDVOLUME5\", \"YMTSELLPRICE5\", \"TRANDAT\", \"TRANTIM\", \"TRADE_PRICE_NEW\")\n" +
            "VALUES(?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?); ";
    private final static String SQL_OC = " INSERT INTO pd_stage.txn_order_detail_h\n" +
            "(\"ISINCOD\", \"TRNTPYID\", \"ORDRBUYCOD\", \"ORDREXEPRC\", \"ORDRQTY\", \"TRDQTY\", " +
            "\"MKTVAL\", \"INVACCTTYPCOD\", \"INVACCTNO\", \"PBUOSCOD\", \"BRNID\", \"MEMBER_ID\", " +
            "\"ORDRTYPCOD\", \"ORDRNO\", \"TRANDAT\", \"TRANTIM\", \"CREDITTAG\", \"MAP_INVACCTNO\", " +
            "\"YMT_ACCOUNT_ID\", \"ORDRRESCOD\")\n" +
            "VALUES(?,?,?,?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?,?, ?, ?, ?, ?, " +
            "?, ?);";
    private final static String SQL_WITHDRAW = " INSERT INTO pd_stage.txn_order_withdraw_h\n" +
            "(\"ISINCOD\", \"TRNTPYID\", \"ORDRQTY\", \"ORDRNO\", \"TRANDAT\", \"TRANTIM\")\n" +
            "VALUES(?, ?, ?, ?, ?, ?) ";
    private final static String SQL_TC = " INSERT INTO pd_stage.txn_trade_detail_h\n" +
            "(\"DRIVER_FLG\", \"TRANIDNO\", \"ISINCOD\", \"TRADE_PRICE_NEW\", \"TRADMTCHPRC\", \"TRDQTY\", " +
            "\"MKTVAL\", \"TOTAUCQTY\", \"TRDTYP\", \"TRDRESTYPCOD\", \"TRNTPYID\", \"ORDRNO_B\", " +
            "\"ORDRNO_S\", \"ORDRCOMPLCOD_B\", \"ORDRCOMPLCOD_S\", \"ORDRTYPCOD_B\", \"ORDRTYPCOD_S\", \"ORDREXEPRC_B\", " +
            "\"ORDREXEPRC_S\", \"ORDQTY_B\", \"ORDQTY_S\", \"ORDREXEQTY_B\", \"ORDREXEQTY_S\", \"INVACCTTYPCOD_B\", " +
            "\"INVACCTTYPCOD_S\", \"INVACCTNO_B\", \"INVACCTNO_S\", \"PBUID_B\", \"PBUID_S\", \"OSPBUID_B\", " +
            "\"OSPBUID_S\", \"BRNID_B\", \"BRNID_S\", \"MEMBER_ID_B\", \"MEMBER_ID_S\", \"CREDITTAG_B\", " +
            "\"CREDITTAG_S\", \"TRANDAT\", \"TRANTIM\", \"BATCH_NUM\", \"MAP_INVACCTNO_B\", \"MAP_INVACCTNO_S\", " +
            "\"YMT_ACCOUNT_ID_B\", \"YMT_ACCOUNT_ID_S\", \"ORDRRESCOD_B\", \"ORDRRESCOD_S\")\n" +
            "VALUES(?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?);";
    private final static String SQL_POSITION = "INSERT INTO pd_stage.txn_position_data_h_tmp\n" +
            "(investor_account_id, isincod, position_amount, pbu_id, brnid, trade_date, map_invacctno, ymt_account_id)" +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?) ";

    private final static String SQL_UPDATE_POSITION = "MERGE INTO pd_stage.txn_position_data_h AS tgt\n" +
            "USING (select investor_account_id, isincod, sum(position_amount) position_amount, \n" +
            "max(pbu_id) pbu_id, max(brnid) brnid, \n" +
            "max(trade_date) trade_date, max(map_invacctno) map_invacctno, \n" +
            "max(ymt_account_id)   ymt_account_id\n" +
            "from  pd_stage.txn_position_data_h_tmp group by  investor_account_id, isincod ) AS src \n" +
            "on src.investor_account_id=tgt.investor_account_id and src.isincod=tgt.isincod\n" +
            "WHEN  MATCHED\n" +
            "THEN UPDATE SET\n" +
            "tgt.position_amount=src.position_amount \n" +
            "WHEN NOT MATCHED\n" +
            "THEN INSERT (investor_account_id, isincod, position_amount, pbu_id, brnid, trade_date, map_invacctno, ymt_account_id)\n" +
            "VALUES (src.investor_account_id, src.isincod, src.position_amount, src.pbu_id, src.brnid, src.trade_date, src.map_invacctno, src.ymt_account_id);\n";

    private final static String SQL_TRUNCATE_TMP_POSITION = " truncate  table pd_stage.txn_position_data_h_tmp ";
    //综业数据 综业盘后固价申报表
    private final static String SQL_PFP_OC = " INSERT INTO pd_stage.txn_pfp_order_detail_h\n" +
            "(\"ACTSEQNUM\", \"ORDRNO\", \"TRNTPYID\", \"INSTRUMENT_ID\", \"ISINCOD\", \"ORDREXEPRC\", \"ORDRQTY\", \"ORDRBUYCOD\", \"ORDRTYPCOD\", \"INVACCTNO\", \"PBUID\", \"BRNID\", \"MEMBER_ID\", \"YMT_ACCOUNT_ID\", \"TRANDAT\", \"TRANTIM\", \"BATCH_NUM\")\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private final static String SQL_PFP_OC_WITHDRAW = "INSERT INTO pd_stage.txn_pfp_order_withdraw_h\n" +
            "(\"ORDRNO\", \"INSTRUMENT_ID\", \"ISINCOD\", \"TRNTPYID\", \"ORDRQTY\", \"TRANDAT\", \"TRANTIM\")\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?);";

    private final static String SQL_PFP_TC = " INSERT INTO pd_stage.txn_pfp_trade_detail_h\n" +
            "(\"TRANIDNO\", \"INSTRUMENT_ID\", \"ISINCOD\", \"TRADE_PRICE\", \"TRDQTY\", \"MKTVAL\", \"PBUORDRNO_B\", " +
            "\"ACTSEQNUM_B\", \"ORDRNO_B\", \"INVACCTNO_B\", \"PBUID_B\", \"BRNID_B\", \"TRDLVSQTY_B\", " +
            "\"PBUORDRNO_S\", \"ACTSEQNUM_S\", \"ORDRNO_S\", \"INVACCTNO_S\", \"PBUID_S\", \"BRNID_S\"," +
            " \"TRDLVSQTY_S\", \"MEMBER_ID_B\", \"MEMBER_ID_S\", \"YMT_ACCOUNT_ID_B\", \"YMT_ACCOUNT_ID_S\"," +
            " \"TRANDAT\", \"TRANTIM\", \"BATCH_NUM\")\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?," +
                " ?, ?, ?, ?, ?, ?, " +
                " ?, ?, ?, ?, ?, ?, " +
                " ?, ?, ?, ?, ?, " +
                " ?, ?, ?);\n";

    @Value("${postgresql.batchsize}")
    private int batchSize;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int insertOrdBook(OrderBookRecord orderBookRecord) {
        return jdbcTemplate.update(
                SQL_ORDBOOK,
                // String.valueOf(orderBookRecord.getSetNo()),
                orderBookRecord.getSetNo(),
                orderBookRecord.getActSeqNo(),
                orderBookRecord.getOrdrNo(),
                orderBookRecord.getLastOrdrNo(),
                orderBookRecord.getIsinCod(),
                orderBookRecord.getInstrumentId(),
                orderBookRecord.getOrdType(),
                orderBookRecord.getLastOrdType(),
                orderBookRecord.getOrdrBuyCod(),
                orderBookRecord.getLevelPrice(),
                orderBookRecord.getPrice(),
                orderBookRecord.getOrdrQty(),
                orderBookRecord.getBuyPrice1(),
                orderBookRecord.getBuyVolume1(),
                orderBookRecord.getSellPrice1(),
                orderBookRecord.getSellVolume1(),
                orderBookRecord.getBuyPrice2(),
                orderBookRecord.getBuyVolume2(),
                orderBookRecord.getSellPrice2(),
                orderBookRecord.getSellVolume2(),
                orderBookRecord.getBuyPrice3(),
                orderBookRecord.getBuyVolume3(),
                orderBookRecord.getSellPrice3(),
                orderBookRecord.getSellVolume3(),
                orderBookRecord.getBuyPrice4(),
                orderBookRecord.getBuyVolume4(),
                orderBookRecord.getSellPrice4(),
                orderBookRecord.getSellVolume4(),
                orderBookRecord.getBuyPrice5(),
                orderBookRecord.getBuyVolume5(),
                orderBookRecord.getSellPrice5(),
                orderBookRecord.getSellVolume5(),
                orderBookRecord.getBuyPrice6(),
                orderBookRecord.getBuyVolume6(),
                orderBookRecord.getSellPrice6(),
                orderBookRecord.getSellVolume6(),
                orderBookRecord.getBuyPrice7(),
                orderBookRecord.getBuyVolume7(),
                orderBookRecord.getSellPrice7(),
                orderBookRecord.getSellVolume7(),
                orderBookRecord.getBuyPrice8(),
                orderBookRecord.getBuyVolume8(),
                orderBookRecord.getSellPrice8(),
                orderBookRecord.getSellVolume8(),
                orderBookRecord.getBuyPrice9(),
                orderBookRecord.getBuyVolume9(),
                orderBookRecord.getSellPrice9(),
                orderBookRecord.getSellVolume9(),
                orderBookRecord.getBuyPrice10(),
                orderBookRecord.getBuyVolume10(),
                orderBookRecord.getSellPrice10(),
                orderBookRecord.getSellVolume10(),
                orderBookRecord.getInvActId(),
                orderBookRecord.getYmtActId(),
                orderBookRecord.getYmtBuyOrdVolume1(),
                orderBookRecord.getYmtBuyPrice1(),
                orderBookRecord.getYmtBuyOrdVolume2(),
                orderBookRecord.getYmtBuyPrice2(),
                orderBookRecord.getYmtBuyOrdVolume3(),
                orderBookRecord.getYmtBuyPrice3(),
                orderBookRecord.getYmtBuyOrdVolume4(),
                orderBookRecord.getYmtBuyPrice4(),
                orderBookRecord.getYmtBuyOrdVolume5(),
                orderBookRecord.getYmtBuyPrice5(),
                orderBookRecord.getYmtSellOrdVolume1(),
                orderBookRecord.getYmtSellPrice1(),
                orderBookRecord.getYmtSellOrdVolume2(),
                orderBookRecord.getYmtSellPrice2(),
                orderBookRecord.getYmtSellOrdVolume3(),
                orderBookRecord.getYmtSellPrice3(),
                orderBookRecord.getYmtSellOrdVolume4(),
                orderBookRecord.getYmtSellPrice4(),
                orderBookRecord.getYmtSellOrdVolume5(),
                orderBookRecord.getYmtSellPrice5(),
                orderBookRecord.getTrandat(),
                orderBookRecord.getTrantim(),
                orderBookRecord.getTradePriceNew());
    }

    @Override
    public int insertOrderDetail(OrderDetail orderDetail) {
        return jdbcTemplate.update(SQL_OC,
                orderDetail.getIsinCod(),
                orderDetail.getTrnTpyId(),
                orderDetail.getOrdrBuyCod(),
                orderDetail.getOrdrExePrc(),
                orderDetail.getOrdrQty(),
                orderDetail.getTrdQty(),
                orderDetail.getMktVal(),
                orderDetail.getInvAcctTypCod(),
                orderDetail.getInvAcctNo(),
                orderDetail.getPbuOsCod(),
                orderDetail.getBrnId(),
                orderDetail.getMemberId(),
                orderDetail.getOrdrTypCod(),
                orderDetail.getOrdrNo(),
                orderDetail.getTranDat(),
                orderDetail.getTranTim(),
                orderDetail.getCreditTag(),
                orderDetail.getMapInvacctNo(),
                orderDetail.getYmtAccountId(),
                orderDetail.getOrdrResCod());
    }

    @Override
    public int insertOrderWithdraw(OrderWithdraw orderWithdraw) {
        return jdbcTemplate.update(SQL_WITHDRAW,
                orderWithdraw.getIsinCod(),
                orderWithdraw.getTrnTpyId(),
                orderWithdraw.getOrdrQty(),
                orderWithdraw.getOrdrNo(),
                orderWithdraw.getTranDat(),
                orderWithdraw.getTranTim());
    }

    @Override
    public int insertTradeDetail(TradeDetail tradeDetail) {
        return jdbcTemplate.update(
                SQL_TC,
                tradeDetail.getDriverFlg(),
                tradeDetail.getTranIdNo(),
                tradeDetail.getIsinCod(),
                tradeDetail.getTradePriceNew(),
                tradeDetail.getTradMtchPrc(),
                tradeDetail.getTrdQty(),
                tradeDetail.getMktVal(),
                tradeDetail.getTotaUcQty(),
                tradeDetail.getTrdTyp(),
                tradeDetail.getTrdResTypCod(),
                tradeDetail.getTrnTpyId(),
                tradeDetail.getOrdrNoB(),
                tradeDetail.getOrdrnoS(),
                tradeDetail.getOrdrComplCodB(),
                tradeDetail.getOrdrComplCodS(),
                tradeDetail.getOrdrTypCodB(),
                tradeDetail.getOrdrTypCodS(),
                tradeDetail.getOrdrExePrcB(),
                tradeDetail.getOrdrExePrcS(),
                tradeDetail.getOrdQtyB(),
                tradeDetail.getOrdQtyS(),
                tradeDetail.getOrdrExeQtyB(),
                tradeDetail.getOrdrExeQtyS(),
                tradeDetail.getInvAcctTypCodB(),
                tradeDetail.getInvAcctTypCodS(),
                tradeDetail.getInvAcctNoB(),
                tradeDetail.getInvAcctNoS(),
                tradeDetail.getPbuIdB(),
                tradeDetail.getPbuIdS(),
                tradeDetail.getOsPbuIdB(),
                tradeDetail.getOsPbuIdS(),
                tradeDetail.getBrnIdB(),
                tradeDetail.getBrnIdS(),
                tradeDetail.getMemberIdB(),
                tradeDetail.getMemberIdS(),
                tradeDetail.getCreditTagB(),
                tradeDetail.getCreditTagS(),
                tradeDetail.getTranDat(),
                tradeDetail.getTranTim(),
                tradeDetail.getBatchNum(),
                tradeDetail.getMapInvAcctNoB(),
                tradeDetail.getMapInvAcctNoS(),
                tradeDetail.getYmtAccountIdB(),
                tradeDetail.getYmtAccountIdS(),
                tradeDetail.getOrdrResCodB(),
                tradeDetail.getOrdrResCodS());
    }

    @Override
    public int[] batchInsertOrdBook(List<OrderBookRecord> orderBookRecords) {
        return jdbcTemplate.batchUpdate(SQL_ORDBOOK, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, orderBookRecords.get(i).getSetNo());
                ps.setLong(2, orderBookRecords.get(i).getActSeqNo());
                ps.setLong(3, orderBookRecords.get(i).getOrdrNo());
                ps.setLong(4, orderBookRecords.get(i).getLastOrdrNo());
                ps.setString(5, orderBookRecords.get(i).getIsinCod());
                ps.setString(6, orderBookRecords.get(i).getInstrumentId());
                ps.setInt(7, orderBookRecords.get(i).getOrdType());
                ps.setInt(8, orderBookRecords.get(i).getLastOrdType());
                ps.setString(9, orderBookRecords.get(i).getOrdrBuyCod());
                ps.setInt(10, orderBookRecords.get(i).getLevelPrice());
                ps.setBigDecimal(11, orderBookRecords.get(i).getPrice());
                ps.setLong(12, orderBookRecords.get(i).getOrdrQty());
                ps.setBigDecimal(13, orderBookRecords.get(i).getBuyPrice1());
                ps.setLong(14, orderBookRecords.get(i).getBuyVolume1());
                ps.setBigDecimal(15, orderBookRecords.get(i).getSellPrice1());
                ps.setLong(16, orderBookRecords.get(i).getSellVolume1());
                ps.setBigDecimal(17, orderBookRecords.get(i).getBuyPrice2());
                ps.setLong(18, orderBookRecords.get(i).getBuyVolume2());
                ps.setBigDecimal(19, orderBookRecords.get(i).getSellPrice2());
                ps.setLong(20, orderBookRecords.get(i).getSellVolume2());
                ps.setBigDecimal(21, orderBookRecords.get(i).getBuyPrice3());
                ps.setLong(22, orderBookRecords.get(i).getBuyVolume3());
                ps.setBigDecimal(23, orderBookRecords.get(i).getSellPrice3());
                ps.setLong(24, orderBookRecords.get(i).getSellVolume3());
                ps.setBigDecimal(25, orderBookRecords.get(i).getBuyPrice4());
                ps.setLong(26, orderBookRecords.get(i).getBuyVolume4());
                ps.setBigDecimal(27, orderBookRecords.get(i).getSellPrice4());
                ps.setLong(28, orderBookRecords.get(i).getSellVolume4());
                ps.setBigDecimal(29, orderBookRecords.get(i).getBuyPrice5());
                ps.setLong(30, orderBookRecords.get(i).getBuyVolume5());
                ps.setBigDecimal(31, orderBookRecords.get(i).getSellPrice5());
                ps.setLong(32, orderBookRecords.get(i).getSellVolume5());
                ps.setBigDecimal(33, orderBookRecords.get(i).getBuyPrice6());
                ps.setLong(34, orderBookRecords.get(i).getBuyVolume6());
                ps.setBigDecimal(35, orderBookRecords.get(i).getSellPrice6());
                ps.setLong(36, orderBookRecords.get(i).getSellVolume6());
                ps.setBigDecimal(37, orderBookRecords.get(i).getBuyPrice7());
                ps.setLong(38, orderBookRecords.get(i).getBuyVolume7());
                ps.setBigDecimal(39, orderBookRecords.get(i).getSellPrice7());
                ps.setLong(40, orderBookRecords.get(i).getSellVolume7());
                ps.setBigDecimal(41, orderBookRecords.get(i).getBuyPrice8());
                ps.setLong(42, orderBookRecords.get(i).getBuyVolume8());
                ps.setBigDecimal(43, orderBookRecords.get(i).getSellPrice8());
                ps.setLong(44, orderBookRecords.get(i).getSellVolume8());
                ps.setBigDecimal(45, orderBookRecords.get(i).getBuyPrice9());
                ps.setLong(46, orderBookRecords.get(i).getBuyVolume9());
                ps.setBigDecimal(47, orderBookRecords.get(i).getSellPrice9());
                ps.setLong(48, orderBookRecords.get(i).getSellVolume9());
                ps.setBigDecimal(49, orderBookRecords.get(i).getBuyPrice10());
                ps.setLong(50, orderBookRecords.get(i).getBuyVolume10());
                ps.setBigDecimal(51, orderBookRecords.get(i).getSellPrice10());
                ps.setLong(52, orderBookRecords.get(i).getSellVolume10());
                ps.setString(53, orderBookRecords.get(i).getInvActId());
                ps.setString(54, orderBookRecords.get(i).getYmtActId());
                ps.setLong(55, orderBookRecords.get(i).getYmtBuyOrdVolume1());
                ps.setBigDecimal(56, orderBookRecords.get(i).getYmtBuyPrice1());
                ps.setLong(57, orderBookRecords.get(i).getYmtBuyOrdVolume2());
                ps.setBigDecimal(58, orderBookRecords.get(i).getYmtBuyPrice2());
                ps.setLong(59, orderBookRecords.get(i).getYmtBuyOrdVolume3());
                ps.setBigDecimal(60, orderBookRecords.get(i).getYmtBuyPrice3());
                ps.setLong(61, orderBookRecords.get(i).getYmtBuyOrdVolume4());
                ps.setBigDecimal(62, orderBookRecords.get(i).getYmtBuyPrice4());
                ps.setLong(63, orderBookRecords.get(i).getYmtBuyOrdVolume5());
                ps.setBigDecimal(64, orderBookRecords.get(i).getYmtBuyPrice5());
                ps.setLong(65, orderBookRecords.get(i).getYmtSellOrdVolume1());
                ps.setBigDecimal(66, orderBookRecords.get(i).getYmtSellPrice1());
                ps.setLong(67, orderBookRecords.get(i).getYmtSellOrdVolume2());
                ps.setBigDecimal(68, orderBookRecords.get(i).getYmtSellPrice2());
                ps.setLong(69, orderBookRecords.get(i).getYmtSellOrdVolume3());
                ps.setBigDecimal(70, orderBookRecords.get(i).getYmtSellPrice3());
                ps.setLong(71, orderBookRecords.get(i).getYmtSellOrdVolume4());
                ps.setBigDecimal(72, orderBookRecords.get(i).getYmtSellPrice4());
                ps.setLong(73, orderBookRecords.get(i).getYmtSellOrdVolume5());
                ps.setBigDecimal(74, orderBookRecords.get(i).getYmtSellPrice5());
                ps.setLong(75, orderBookRecords.get(i).getTrandat());
                ps.setLong(76, orderBookRecords.get(i).getTrantim());
                ps.setBigDecimal(77, orderBookRecords.get(i).getTradePriceNew());
            }

            @Override
            public int getBatchSize() {
                return orderBookRecords.size();
            }
        });
    }

    @Override
    public int[] batchInsertOrderDetail(List<OrderDetail> orderDetails) {
        return jdbcTemplate.batchUpdate(SQL_OC, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, orderDetails.get(i).getIsinCod());
                ps.setString(2, orderDetails.get(i).getTrnTpyId());
                ps.setString(3, orderDetails.get(i).getOrdrBuyCod());
                ps.setBigDecimal(4, orderDetails.get(i).getOrdrExePrc());
                ps.setBigDecimal(5, orderDetails.get(i).getOrdrQty());
                ps.setBigDecimal(6, orderDetails.get(i).getTrdQty());
                ps.setBigDecimal(7, orderDetails.get(i).getMktVal());
                ps.setString(8, orderDetails.get(i).getInvAcctTypCod());
                ps.setString(9, orderDetails.get(i).getInvAcctNo());
                ps.setString(10, orderDetails.get(i).getPbuOsCod());
                ps.setString(11, orderDetails.get(i).getBrnId());
                ps.setString(12, orderDetails.get(i).getMemberId());
                ps.setString(13, orderDetails.get(i).getOrdrTypCod());
                ps.setLong(14, orderDetails.get(i).getOrdrNo());
                ps.setLong(15, orderDetails.get(i).getTranDat());
                ps.setLong(16, orderDetails.get(i).getTranTim());
                ps.setString(17, orderDetails.get(i).getCreditTag());
                ps.setString(18, orderDetails.get(i).getMapInvacctNo());
                ps.setString(19, orderDetails.get(i).getYmtAccountId());
                ps.setString(20, orderDetails.get(i).getOrdrResCod());
            }

            @Override
            public int getBatchSize() {
                return orderDetails.size();
            }
        });
    }

    @Override
    public int[] batchInsertOrderWithdraw(List<OrderWithdraw> orderWithdraws) {
        return jdbcTemplate.batchUpdate(SQL_WITHDRAW, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, orderWithdraws.get(i).getIsinCod());
                ps.setString(2, orderWithdraws.get(i).getTrnTpyId());
                ps.setBigDecimal(3, orderWithdraws.get(i).getOrdrQty());
                ps.setBigDecimal(4, orderWithdraws.get(i).getOrdrNo());
                ps.setLong(5, orderWithdraws.get(i).getTranDat());
                ps.setLong(6, orderWithdraws.get(i).getTranTim());
            }

            @Override
            public int getBatchSize() {
                return orderWithdraws.size();
            }
        });
    }

    @Override
    public int[] batchInsertTradeDetail(List<TradeDetail> tradeDetails) {
        return jdbcTemplate.batchUpdate(SQL_TC, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, tradeDetails.get(i).getDriverFlg());
                ps.setLong(2, tradeDetails.get(i).getTranIdNo());
                ps.setString(3, tradeDetails.get(i).getIsinCod());
                ps.setBigDecimal(4, tradeDetails.get(i).getTradePriceNew());
                ps.setBigDecimal(5, tradeDetails.get(i).getTradMtchPrc());
                ps.setBigDecimal(6, tradeDetails.get(i).getTrdQty());
                ps.setBigDecimal(7, tradeDetails.get(i).getMktVal());
                ps.setBigDecimal(8, tradeDetails.get(i).getTotaUcQty());
                ps.setString(9, tradeDetails.get(i).getTrdTyp());
                ps.setString(10, tradeDetails.get(i).getTrdResTypCod());
                ps.setString(11, tradeDetails.get(i).getTrnTpyId());
                ps.setLong(12, tradeDetails.get(i).getOrdrNoB());
                ps.setLong(13, tradeDetails.get(i).getOrdrnoS());
                ps.setString(14, tradeDetails.get(i).getOrdrComplCodB());
                ps.setString(15, tradeDetails.get(i).getOrdrComplCodS());
                ps.setString(16, tradeDetails.get(i).getOrdrTypCodB());
                ps.setString(17, tradeDetails.get(i).getOrdrTypCodS());
                ps.setBigDecimal(18, tradeDetails.get(i).getOrdrExePrcB());
                ps.setBigDecimal(19, tradeDetails.get(i).getOrdrExePrcS());
                ps.setBigDecimal(20, tradeDetails.get(i).getOrdQtyB());
                ps.setBigDecimal(21, tradeDetails.get(i).getOrdQtyS());
                ps.setBigDecimal(22, tradeDetails.get(i).getOrdrExePrcB());
                ps.setBigDecimal(23, tradeDetails.get(i).getOrdrExePrcS());
                ps.setString(24, tradeDetails.get(i).getInvAcctTypCodB());
                ps.setString(25, tradeDetails.get(i).getInvAcctTypCodS());
                ps.setString(26, tradeDetails.get(i).getInvAcctNoB());
                ps.setString(27, tradeDetails.get(i).getInvAcctNoS());
                ps.setString(28, tradeDetails.get(i).getPbuIdB());
                ps.setString(29, tradeDetails.get(i).getPbuIdS());
                ps.setString(30, tradeDetails.get(i).getOsPbuIdB());
                ps.setString(31, tradeDetails.get(i).getOsPbuIdS());
                ps.setString(32, tradeDetails.get(i).getBrnIdB());
                ps.setString(33, tradeDetails.get(i).getBrnIdS());
                ps.setString(34, tradeDetails.get(i).getMemberIdB());
                ps.setString(35, tradeDetails.get(i).getMemberIdS());
                ps.setString(36, tradeDetails.get(i).getCreditTagB());
                ps.setString(37, tradeDetails.get(i).getCreditTagS());
                ps.setLong(38, tradeDetails.get(i).getTranDat());
                ps.setLong(39, tradeDetails.get(i).getTranTim());
                ps.setLong(40, tradeDetails.get(i).getBatchNum());
                ps.setString(41, tradeDetails.get(i).getMapInvAcctNoB());
                ps.setString(42, tradeDetails.get(i).getMapInvAcctNoS());
                ps.setString(43, tradeDetails.get(i).getYmtAccountIdB());
                ps.setString(44, tradeDetails.get(i).getYmtAccountIdS());
                ps.setString(45, tradeDetails.get(i).getOrdrResCodB());
                ps.setString(46, tradeDetails.get(i).getOrdrResCodS());
            }

            @Override
            public int getBatchSize() {
                return tradeDetails.size();
            }
        });
    }

    @Override
    public int[] batchInsertPositionData(List<PositionDetail> positionDetails) {
        return jdbcTemplate.batchUpdate(SQL_POSITION, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, positionDetails.get(i).getInvestorAccountId());
                ps.setString(2, positionDetails.get(i).getIsinCod());
                ps.setBigDecimal(3, positionDetails.get(i).getPositionAmount());
                ps.setString(4, positionDetails.get(i).getPbuId());
                ps.setString(5, positionDetails.get(i).getBrnId());
                ps.setString(6, positionDetails.get(i).getTradeDate());
                ps.setString(7, positionDetails.get(i).getMapInvacctno());
                ps.setString(8, positionDetails.get(i).getYmtAccountId());
            }

            @Override
            public int getBatchSize() {
                return positionDetails.size();
            }
        });
    }

    @Override
    public int updatePositionData() {
        return jdbcTemplate.update(SQL_UPDATE_POSITION);
    }

    @Override
    public int truncateTempPositionData() {
        return jdbcTemplate.update(SQL_TRUNCATE_TMP_POSITION);
    }

    @Override
    public int[] batchInsertPfpOrderDetail(List<AtpTo> ocDataTrads_pfp) {
        return jdbcTemplate.batchUpdate(SQL_PFP_OC, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, ocDataTrads_pfp.get(i).getActionSeqNum());
                ps.setLong(2, ocDataTrads_pfp.get(i).getOrdrNo());
                ps.setString(3, String.valueOf(ocDataTrads_pfp.get(i).getMsgTyp()));
                ps.setString(4, ocDataTrads_pfp.get(i).getInstId());
                ps.setString(5, ocDataTrads_pfp.get(i).getIsinCod());
                ps.setBigDecimal(6, ocDataTrads_pfp.get(i).getPrice());
                ps.setBigDecimal(7, ocDataTrads_pfp.get(i).getQuantity());
                ps.setString(8, String.valueOf(ocDataTrads_pfp.get(i).getSide()));
                ps.setString(9, String.valueOf(ocDataTrads_pfp.get(i).getOrdTyp()));
                ps.setString(10, ocDataTrads_pfp.get(i).getInvAcctId());
                ps.setString(11, ocDataTrads_pfp.get(i).getPbuId());
                ps.setString(12, ocDataTrads_pfp.get(i).getBranchId());
                ps.setString(13, ocDataTrads_pfp.get(i).getMemberId());
                ps.setString(14, ocDataTrads_pfp.get(i).getYmtAccountId());
                ps.setLong(15, Long.valueOf(ocDataTrads_pfp.get(i).getOrdDate()));
                ps.setLong(16, Long.valueOf(ocDataTrads_pfp.get(i).getOrdTime()));
                ps.setLong(17, ocDataTrads_pfp.get(i).getBatchNum());
            }

            @Override
            public int getBatchSize() {
                return ocDataTrads_pfp.size();
            }
        });
    }

    @Override
    public int[] batchInsertPfpOrderWithCancel(List<AtpTo> ocDataCancels_pfp) {
        return jdbcTemplate.batchUpdate(SQL_PFP_OC_WITHDRAW, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, ocDataCancels_pfp.get(i).getOrdrNo());
                ps.setString(2, ocDataCancels_pfp.get(i).getInstId());
                ps.setString(3, ocDataCancels_pfp.get(i).getIsinCod());
                ps.setString(4, String.valueOf(ocDataCancels_pfp.get(i).getMsgTyp()));
                ps.setBigDecimal(5, ocDataCancels_pfp.get(i).getQuantity());
                ps.setLong(6, Long.valueOf(ocDataCancels_pfp.get(i).getOrdDate()));
                ps.setLong(7, Long.valueOf(ocDataCancels_pfp.get(i).getOrdTime()));
            }

            @Override
            public int getBatchSize() {
                return ocDataCancels_pfp.size();
            }
        });
    }

    @Override
    public int[] batchInsertPfpTradeDetail(List<AtpTt> tcDatas_pfp) {
        return jdbcTemplate.batchUpdate(SQL_PFP_TC, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, tcDatas_pfp.get(i).getTradeSeqNum());
                ps.setString(2, tcDatas_pfp.get(i).getInstId());
                ps.setString(3, tcDatas_pfp.get(i).getIsinCod());
                ps.setBigDecimal(4, tcDatas_pfp.get(i).getTradePrice());
                ps.setBigDecimal(5, tcDatas_pfp.get(i).getTradeQuantity());
                ps.setBigDecimal(6, tcDatas_pfp.get(i).getTradeAmount());
                ps.setString(7, tcDatas_pfp.get(i).getBuyPBUOrderNo());
                ps.setLong(8, tcDatas_pfp.get(i).getBuyActionSeqNum());
                ps.setLong(9, tcDatas_pfp.get(i).getBuyNGTSOrderNo());
                ps.setString(10, tcDatas_pfp.get(i).getBuyInvAcctId());
                ps.setString(11, tcDatas_pfp.get(i).getBuyPbuId());
                ps.setString(12, tcDatas_pfp.get(i).getBuyBranchId());
                ps.setLong(13, tcDatas_pfp.get(i).getBuyTrdLvsQty());
                ps.setString(14, tcDatas_pfp.get(i).getSellPBUOrderNo());
                ps.setLong(15, tcDatas_pfp.get(i).getSellActionSeqNum());
                ps.setLong(16, tcDatas_pfp.get(i).getSellNGTSOrderNo());
                ps.setString(17, tcDatas_pfp.get(i).getSellInvAcctId());
                ps.setString(18, tcDatas_pfp.get(i).getSellPbuId());
                ps.setString(19, tcDatas_pfp.get(i).getSellBranchId());
                ps.setLong(20, tcDatas_pfp.get(i).getSellTrdLvsQty());
                //TODO kafka原始数据无成员代码
                ps.setString(21, tcDatas_pfp.get(i).getMemberIdB());
                ps.setString(22, tcDatas_pfp.get(i).getMemberIdS());
                ps.setString(23, tcDatas_pfp.get(i).getBuyYmtAccountId());
                ps.setString(24, tcDatas_pfp.get(i).getSellYmtAccountId());
                ps.setLong(25, Long.valueOf(tcDatas_pfp.get(i).getTradeDate()));
                ps.setLong(26, Long.valueOf(tcDatas_pfp.get(i).getTradeTime()));
                //TODO kafka原始数据无批次号
                ps.setString(27, tcDatas_pfp.get(i).getBatchNum());
            }

            @Override
            public int getBatchSize() {
                return tcDatas_pfp.size();
            }
        });
    }


}



















