package com.sse.rcp.dbsink;

import com.sse.rcp.dao.StorageDao;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;
import com.sse.rcp.domains.order.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class StorageService {
    @Autowired
    private StorageDao storageDao;

    /**
     * 订单簿重演数据直接入库
     *
     * @param orderBookRecords
     */
    public void storeOrderBooKRecord(List<OrderBookRecord> orderBookRecords) {
        storageDao.batchInsertOrdBook(orderBookRecords);
    }

    public void storeOrderDetail(List<OrderDetail> orderDetails) {
        storageDao.batchInsertOrderDetail(orderDetails);
    }

    public void storeOrderWithCancel(List<OrderWithdraw> ocDataCancels) {
        storageDao.batchInsertOrderWithdraw(ocDataCancels);
    }

    public void storeTradeDetail(List<TradeDetail> tradeDetail) {
        storageDao.batchInsertTradeDetail(tradeDetail);
    }

    public static OrderWithdraw transToOrderWithCancel(MtpOrdcnmf mtpOrdcnmf) {
        OrderWithdraw orderWithdraw = new OrderWithdraw();
        orderWithdraw.setIsinCod(mtpOrdcnmf.getIsin());
        orderWithdraw.setTrnTpyId(String.valueOf(mtpOrdcnmf.getTrnTypId()));
        orderWithdraw.setOrdrQty(mtpOrdcnmf.getOrdrQty());
        orderWithdraw.setOrdrNo(BigDecimal.valueOf(mtpOrdcnmf.getOrdrNum()));
        orderWithdraw.setTranDat(mtpOrdcnmf.getTranDatTim() / 100000000);
        orderWithdraw.setTranTim(mtpOrdcnmf.getTranDatTim() % 100000000);
        return orderWithdraw;
    }

    public static OrderDetail transToOrderDetail(MtpOrdcnmf mtpOrdcnmf) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setIsinCod(mtpOrdcnmf.getIsin());
        orderDetail.setTrnTpyId(String.valueOf(mtpOrdcnmf.getTrnTypId()));
        orderDetail.setOrdrBuyCod(String.valueOf(mtpOrdcnmf.getProdBuySell()));
        orderDetail.setOrdrExePrc(mtpOrdcnmf.getOrdrExePrc());
        orderDetail.setOrdrQty(mtpOrdcnmf.getOrdrQty());
        orderDetail.setTrdQty(mtpOrdcnmf.getOrdrExeQty());
        orderDetail.setMktVal(mtpOrdcnmf.getOrdrExePrc().multiply(mtpOrdcnmf.getOrdrExeQty()));
        orderDetail.setInvAcctTypCod(mtpOrdcnmf.getInvAcctId().trim().substring(0, 1));
        orderDetail.setInvAcctNo(mtpOrdcnmf.getInvAcctId().substring(1, mtpOrdcnmf.getInvAcctId().trim().length()));
        orderDetail.setPbuOsCod(mtpOrdcnmf.getFfTxtGrp().getPbuOsCod());
        orderDetail.setBrnId(mtpOrdcnmf.getBrnId());
//        orderDetail.setMemberId();
        orderDetail.setOrdrTypCod(String.valueOf(mtpOrdcnmf.getOrderType()));
        orderDetail.setOrdrNo(mtpOrdcnmf.getOrdrNum());
        orderDetail.setTranDat(mtpOrdcnmf.getTranDatTim() / 100000000);
        orderDetail.setTranTim(mtpOrdcnmf.getTranDatTim() % 100000000);
        orderDetail.setCreditTag(String.valueOf(mtpOrdcnmf.getCreditLabel()));

        // invacctno是信用账户（E账户）时，此列为信用账户对应的普通账户；invacctno不是信用账户（E账户）时，此列与invacctno一致
        orderDetail.setMapInvacctNo(orderDetail.getInvAcctNo());

        orderDetail.setYmtAccountId(mtpOrdcnmf.getYmtAccountId());
        orderDetail.setOrdrResCod(String.valueOf(mtpOrdcnmf.getExecRestrict()));
        return orderDetail;
    }

    public static TradeDetail transToTradeDetail(MtpTrdcnmf mtpTrdcnmf) {
        //     1~2位   产品买卖  0:买 1:卖 2:无
        TradeDetail td = new TradeDetail();
        processBuyTradeDetail(mtpTrdcnmf, td);
        processSellTradeDetail(mtpTrdcnmf, td);
        return td;
    }


    private static void processSellTradeDetail(MtpTrdcnmf mtpTrdcnmf, TradeDetail td) {
        SimpTcT trdCfmSell = mtpTrdcnmf.getTrdCfmSell();
        td.setDriverFlg(1);
        td.setTranIdNo(trdCfmSell.getTranIdNo());
        td.setIsinCod(mtpTrdcnmf.getSellIsin());
        td.setTradePriceNew(trdCfmSell.getOrdrExePrc());
        td.setTradMtchPrc(trdCfmSell.getTradMtchPrc().setScale(5, BigDecimal.ROUND_HALF_UP));
        td.setTrdQty(trdCfmSell.getTrdQty());
        td.setMktVal(BigDecimal.valueOf(trdCfmSell.getMktVal()));
        td.setTotaUcQty(trdCfmSell.getOrdrExeQty().add(trdCfmSell.getOrdrQty()));
        td.setTrdQty(trdCfmSell.getTrdQty());
//        td.setTrdResTypCod(new BigDecimal(trdCfmSell.getResTrade()));
//        td.setTrnTpyId();
        td.setOrdrnoS(trdCfmSell.getOrdrNo());
        if (Objects.equals(trdCfmSell.getFullTrade(), 0)) {
            td.setOrdrComplCodS("P");
        } else {
            td.setOrdrComplCodS("F");
        }
        td.setOrdrTypCodS(String.valueOf(trdCfmSell.getOrderType()));
        td.setOrdrExePrcS(trdCfmSell.getOrdrExePrc());
        td.setOrdQtyS(trdCfmSell.getOrdrQty());
        td.setInvAcctTypCodS(trdCfmSell.getInvAcctId().substring(0, 1));
        td.setInvAcctNoS(trdCfmSell.getInvAcctId().substring(1, trdCfmSell.getInvAcctId().trim().length()));
        td.setPbuIdS(trdCfmSell.getFfTxtGrpT().getPbuBizCod());
        td.setOsPbuIdS(trdCfmSell.getFfTxtGrpT().getPbuOsCod());
        td.setBrnIdS(trdCfmSell.getBrnId());
//        td.setMemberIdS();
        td.setCreditTagB(String.valueOf(trdCfmSell.getCreditLabel()));
        if (trdCfmSell.getCreditLabel() < 1) {
            td.setCreditTagB("9");
        }
        td.setTranDat(trdCfmSell.getTranDatTim() / 100000000);
        td.setTranTim(trdCfmSell.getTranDatTim() % 100000000);
        //        td.setBatchNum();
        //        td.setMapInvAcctNoS();
        td.setYmtAccountIdS(mtpTrdcnmf.getBuyYmtAccountId());
        td.setOrdrResCodS(String.valueOf(trdCfmSell.getExecRestrict()));
    }

    private static void processBuyTradeDetail(MtpTrdcnmf mtpTrdcnmf, TradeDetail td) {
        SimpTcT trdCfmBuy = mtpTrdcnmf.getTrdCfmBuy();
        td.setDriverFlg(0);
        td.setTranIdNo(trdCfmBuy.getTranIdNo());
        td.setIsinCod(mtpTrdcnmf.getBuyIsin());
        td.setTradePriceNew(trdCfmBuy.getOrdrExePrc());
        td.setTradMtchPrc(trdCfmBuy.getTradMtchPrc().setScale(5, BigDecimal.ROUND_HALF_UP));
        td.setTrdQty(trdCfmBuy.getTrdQty());
        td.setMktVal(BigDecimal.valueOf(trdCfmBuy.getMktVal()));
        td.setTotaUcQty(trdCfmBuy.getOrdrExeQty().add(trdCfmBuy.getOrdrQty()));
        td.setTrdQty(trdCfmBuy.getTrdQty());
//        td.setTrdResTypCod(new BigDecimal(trdCfmBuy.getResTrade()));
//        td.setTrnTpyId();
        td.setOrdrNoB(trdCfmBuy.getOrdrNo());
        if (Objects.equals(trdCfmBuy.getFullTrade(), 0)) {
            td.setOrdrComplCodB("P");
        } else {
            td.setOrdrComplCodB("F");
        }
        td.setOrdrTypCodB(String.valueOf(trdCfmBuy.getOrderType()));
        td.setOrdrExePrcB(trdCfmBuy.getOrdrExePrc());
        td.setOrdQtyB(trdCfmBuy.getOrdrQty());
        td.setInvAcctTypCodB(trdCfmBuy.getInvAcctId().substring(0, 1));
        td.setInvAcctNoB(trdCfmBuy.getInvAcctId().substring(1, trdCfmBuy.getInvAcctId().length()));
        td.setPbuIdB(trdCfmBuy.getFfTxtGrpT().getPbuBizCod());
        td.setOsPbuIdB(trdCfmBuy.getFfTxtGrpT().getPbuOsCod());
        td.setBrnIdB(trdCfmBuy.getBrnId());
//        td.setMemberIdB();
        td.setCreditTagB(String.valueOf(trdCfmBuy.getCreditLabel()));
        if (trdCfmBuy.getCreditLabel() < 1) {
            td.setCreditTagB("9");
        }
        td.setTranDat(trdCfmBuy.getTranDatTim() / 100000000);
        td.setTranTim(trdCfmBuy.getTranDatTim() % 100000000);
        //        td.setBatchNum();
//        td.setMapInvAcctNoB();
        td.setYmtAccountIdB(mtpTrdcnmf.getBuyYmtAccountId());
        td.setOrdrResCodB(String.valueOf(trdCfmBuy.getExecRestrict()));
    }

    /**
     * 成交数据转换为持仓数据,并存入集合中
     * @param positionDatas
     * @param td
     * @return
     */
    public static String putPositionDetail(List<PositionDetail> positionDatas ,TradeDetail td) {

        PositionDetail buy_pd = new PositionDetail();
        String dealDay = String.valueOf(td.getTranDat());
        //买方持仓
        buy_pd.setInvestorAccountId(td.getInvAcctNoB());
        buy_pd.setIsinCod(td.getIsinCod());
        buy_pd.setPositionAmount(td.getTrdQty());
        buy_pd.setPbuId(td.getPbuIdB());
        buy_pd.setBrnId(td.getBrnIdB());
        buy_pd.setTradeDate(dealDay);
        buy_pd.setMapInvacctno(td.getMapInvAcctNoB());
        buy_pd.setYmtAccountId(td.getYmtAccountIdB());
        //卖方持仓
        PositionDetail sell_pd = new PositionDetail();
        sell_pd.setInvestorAccountId(td.getInvAcctNoS());
        sell_pd.setIsinCod(td.getIsinCod());
        //持仓量减少
        sell_pd.setPositionAmount(td.getTrdQty().negate());
        sell_pd.setPbuId(td.getPbuIdS());
        sell_pd.setBrnId(td.getBrnIdS());
        sell_pd.setTradeDate(dealDay);
        sell_pd.setMapInvacctno(td.getMapInvAcctNoS());
        sell_pd.setYmtAccountId(td.getYmtAccountIdS());

        //持仓信息放入集合中
        positionDatas.add(buy_pd);
        positionDatas.add(sell_pd);
        return dealDay;
    }

    /**
     * 存入持仓数据
     *
     * @param positionDetail
     */
    public void storePositionData(List<PositionDetail> positionDetail) {
        storageDao.batchInsertPositionData(positionDetail);
    }

    /**
     * 更新持仓数据
     * 清空临时表中数据
     */
    public void updatePositionData() {
        storageDao.updatePositionData();
        storageDao.truncateTempPositionData();
    }

    /**
     * 清理持仓数据临时表
     */
    public void truncateTempPositionData() {
        storageDao.truncateTempPositionData();
    }

    /**
     * 综业盘后固价申报表
     * @param ocDataTrads_pfp
     */
    public void storePfpOrderDetail(List<AtpTo> ocDataTrads_pfp) {
        storageDao.batchInsertPfpOrderDetail(ocDataTrads_pfp);
    }

    /**
     * 综业盘后固价撤单表
     * @param ocDataCancels_pfp
     */
    public void storePfpOrderWithCancel(List<AtpTo> ocDataCancels_pfp) {
        storageDao.batchInsertPfpOrderWithCancel(ocDataCancels_pfp);
    }

    /**
     * 综业盘后固价成交表
     * @param tcDatas_pfp
     */
    public void storePfpTradeDetail(List<AtpTt> tcDatas_pfp) {
        storageDao.batchInsertPfpTradeDetail(tcDatas_pfp);
    }
}
