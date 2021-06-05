package com.sse.rcp.sort;

import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ComplementOcByTc {
    /**
     * 补订单
     *
     * @param data 当前订单数据
     * @return 如果需要补订单，则返回补好的数据；如果不用补订单，则返回空
     */
    public TopicData addOrder(TopicData data) {
        if (data instanceof MtpOrdcnmf) {
            SortOperator.updateLocalOrderNo(((MtpOrdcnmf) data).getOrdrNum());
            return null;
        } else if (data instanceof MtpTrdcnmf) {
            SimpTcT target = null;
            boolean isBuySide = false;
            SimpTcT buySimpTcT = ((MtpTrdcnmf) data).getTrdCfmBuy();
            SimpTcT sellSimpTcT = ((MtpTrdcnmf) data).getTrdCfmSell();
            // 订单编号（买 & 卖）
            long buyOrderNo = buySimpTcT.getOrdrNo();
            long sellOrderNo = sellSimpTcT.getOrdrNo();

            // 如果待补订单编号小于当前消费订单编号，则不用补订单。
            // 如果是即时成交，补市价这一侧。市价成交单必须补订单，【orderMask】的11~13位=1市价订单，补类型为市价单的一方
            if (buySimpTcT.getOrderType() == 1 && buyOrderNo > SortOperator.getLocalOrderNo()) {
                target = ((MtpTrdcnmf) data).getTrdCfmBuy();
                SortOperator.updateLocalOrderNo(buyOrderNo);
                isBuySide = true;
            } else if (sellSimpTcT.getOrderType() == 1 && sellOrderNo > SortOperator.getLocalOrderNo()) {
                target = ((MtpTrdcnmf) data).getTrdCfmSell();
                SortOperator.updateLocalOrderNo(sellOrderNo);
                isBuySide = false;
            }
            // 如果剩余订单数量为0，则补订单编号较大的一侧肯定为0 ，去主动成交了订单簿上的数据。但是不一定完全消费掉。
            else if (sellSimpTcT.getOrdrQty().compareTo(BigDecimal.ZERO) == 0
                    || buySimpTcT.getOrdrQty().compareTo(BigDecimal.ZERO) == 0) {
                if (sellOrderNo >= buyOrderNo && sellOrderNo > SortOperator.getLocalOrderNo()) {
                    target = ((MtpTrdcnmf) data).getTrdCfmSell();
                    SortOperator.updateLocalOrderNo(sellOrderNo);
                    isBuySide = false;
                } else if (buyOrderNo > SortOperator.getLocalOrderNo()) {
                    target = ((MtpTrdcnmf) data).getTrdCfmBuy();
                    SortOperator.updateLocalOrderNo(buyOrderNo);
                    isBuySide = true;
                }
            }
            // 补订单
            return doAdd((MtpTrdcnmf) data, target, isBuySide);
        } else if (data instanceof AtpTo || data instanceof AtpTt) {//综业数据不需要补订单
            return null;
        } else {
            log.warn("线程{}补订单数据类型异常->{}", Thread.currentThread().getName(), data.toString());
            return null;
        }
    }

    /**
     * 针对需要补订单的TC，开始补充OC
     *
     * @param data   TC数据
     * @param target 待补充的数据
     * @return 补好的OC数据
     */
    private TopicData doAdd(MtpTrdcnmf data, SimpTcT target, boolean isBuySide) {
        // 待补充的数据为null，则说明不需要补。直接返回
        if (target == null) {
            return null;
        }
        MtpOrdcnmf mtpOrdcnmf = new MtpOrdcnmf();

        mtpOrdcnmf.setIsix(target.getIsix());
        mtpOrdcnmf.setProdBuySell(target.getProdBuySell());
        mtpOrdcnmf.setSellOut(target.getSellOut());
        mtpOrdcnmf.setResTrade(target.getResTrade());
        mtpOrdcnmf.setExecRestrict(target.getExecRestrict());
        mtpOrdcnmf.setOrderType(target.getOrderType());
        mtpOrdcnmf.setOrdrExePrc(target.getOrdrExePrc());
        mtpOrdcnmf.setOrdrQty(target.getOrdrQty());
        mtpOrdcnmf.setOrdrExeQty(target.getOrdrExeQty());
        mtpOrdcnmf.setOrdrNum(target.getOrdrNo());
        mtpOrdcnmf.setTranDatTim(target.getTranDatTim());
        mtpOrdcnmf.setInvAcctId(target.getInvAcctId());
        mtpOrdcnmf.setFfTxtGrp(target.getFfTxtGrpT());
        mtpOrdcnmf.setBrnId(target.getBrnId());
        mtpOrdcnmf.setTrnTypId(target.getTrnTypId());
        mtpOrdcnmf.setAccountType(target.getAccountType());
        mtpOrdcnmf.setCreditLabel(target.getCreditLabel());
        mtpOrdcnmf.setFullTrade(target.getFullTrade());
        mtpOrdcnmf.setOrderStatus(target.getOrderStatus());

        if (isBuySide) {
            mtpOrdcnmf.setIsin(data.getBuyIsin());
            mtpOrdcnmf.setInstrumentId(data.getBuyInstrumentId());
            mtpOrdcnmf.setYmtAccountId(data.getBuyYmtAccountId());
            mtpOrdcnmf.setYmtAccountName(data.getBuyYmtAccountName());
        } else {
            mtpOrdcnmf.setIsin(data.getSellIsin());
            mtpOrdcnmf.setInstrumentId(data.getSellInstrumentId());
            mtpOrdcnmf.setYmtAccountId(data.getSellYmtAccountId());
            mtpOrdcnmf.setYmtAccountName(data.getSellYmtAccountName());
        }

//        mtpOrdcnmf.setFiller2();
//        mtpOrdcnmf.setActnSeqNum();
        return mtpOrdcnmf;
    }
}
