package com.sse.rcp.order;

import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;
import com.sse.rcp.domains.order.OrderBookRecord;
import com.sse.rcp.domains.order.OrderNode;
import com.sse.rcp.domains.order.PriceNode;
import com.sse.rcp.domains.order.YmtDeclareNode;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class OrderBookHelper {


    /**
     * 将kafka的OC信息转换成对应的【价格节点】数据
     *
     * @param mtpOrdcnmf OC数据
     * @param isDecrease 当前数据是对订单簿已有数据进行【加】或【减】
     * @return 转换后的【价格节点】数据
     */
    public static PriceNode fillPriceNode(MtpOrdcnmf mtpOrdcnmf, boolean isDecrease) {
        PriceNode priceNode = new PriceNode();
        priceNode.setInstrumentId(mtpOrdcnmf.getInstrumentId());
        priceNode.setOrdrExePrc(mtpOrdcnmf.getOrdrExePrc());
        // 撤单的情况下
        if (isDecrease) {
            priceNode.setOrderCounts(-1);
            priceNode.setOrdrQtyTotal(mtpOrdcnmf.getOrdrQty().negate());
            priceNode.setOrdrExeQtyTotal(BigDecimal.ZERO);
        }
        // 普通的申报订单
        else {
            priceNode.setOrderCounts(1);
            priceNode.setOrdrQtyTotal(mtpOrdcnmf.getOrdrQty());
            priceNode.setOrdrExeQtyTotal(mtpOrdcnmf.getOrdrExeQty());
        }
        return priceNode;
    }


    /**
     * 根据kafka中TC消息的成交信息，得到对应的【价格节点】修正数据
     *
     * @param simpTcT      成交结构
     * @param instrumentId instrumentId
     * @return 【价格节点】修正数据
     */
    public static PriceNode fillPriceNode(SimpTcT simpTcT, String instrumentId) {
        PriceNode priceNode = new PriceNode();
        priceNode.setInstrumentId(instrumentId);
        priceNode.setOrdrExePrc(simpTcT.getOrdrExePrc());
        priceNode.setOrdrQtyTotal(simpTcT.getTrdQty().negate());
        priceNode.setOrdrExeQtyTotal(BigDecimal.ZERO);
        priceNode.setOrderCounts(0);
        return priceNode;
    }


    /**
     * 将kafka的OC信息转换成对应的【订单节点】数据
     *
     * @param mtpOrdcnmf OC数据
     * @return 转换后的【订单节点】数据
     */
    public static OrderNode fillOrderNode(MtpOrdcnmf mtpOrdcnmf) {
        long orderNum = mtpOrdcnmf.getOrdrNum();
        BigDecimal ordrQty = mtpOrdcnmf.getOrdrQty();
        String invAcctId = mtpOrdcnmf.getInvAcctId();

        OrderNode orderNode = new OrderNode();
        orderNode.setInstrumentId(mtpOrdcnmf.getInstrumentId());
        orderNode.setOrdrNum(orderNum);
        orderNode.setInvAcctId(invAcctId);

        orderNode.setOrdrQty(ordrQty);
        return orderNode;
    }

    /**
     * 根据kafka中TC消息的成交信息，得到对应的【订单信息】修正数据
     *
     * @param simpTcT      成交结构
     * @param instrumentId instrumentId
     * @return 【订单信息】
     */
    public static OrderNode fillOrderNode(SimpTcT simpTcT, String instrumentId) {
        OrderNode orderNode = new OrderNode();
        orderNode.setInstrumentId(instrumentId);
        orderNode.setOrdrNum(simpTcT.getOrdrNo());
        orderNode.setInvAcctId(simpTcT.getInvAcctId());
        orderNode.setOrdrExePrc(simpTcT.getOrdrExePrc());
        orderNode.setOrdrQty(simpTcT.getTrdQty().negate());
        return orderNode;
    }


    public static YmtDeclareNode fillYmtNodeCancel(MtpOrdcnmf mtpOrdcnmf) {
        YmtDeclareNode ymtDeclareNode = fillYmtNodeRaw(mtpOrdcnmf);
        ymtDeclareNode.setCancelCounts(1L);
        ymtDeclareNode.setOrderCounts(-1L);
        return ymtDeclareNode;
    }

    public static YmtDeclareNode fillYmtNodeNew(MtpOrdcnmf mtpOrdcnmf) {
        YmtDeclareNode ymtDeclareNode = fillYmtNodeRaw(mtpOrdcnmf);
        ymtDeclareNode.setCancelCounts(0L);
        ymtDeclareNode.setOrderCounts(1L);
        return ymtDeclareNode;
    }

    public static YmtDeclareNode fillYmtNodeRaw(MtpOrdcnmf mtpOrdcnmf) {
        YmtDeclareNode ymtDeclareNode = new YmtDeclareNode();
        ymtDeclareNode.setOrdrQtyTotal(mtpOrdcnmf.getOrdrQty());
        ymtDeclareNode.setOrdrExePrc(mtpOrdcnmf.getOrdrExePrc());
        ymtDeclareNode.setInstrumentId(mtpOrdcnmf.getInstrumentId());
        ymtDeclareNode.setTradeCounts(0L);
        ymtDeclareNode.setYmtActId(mtpOrdcnmf.getYmtAccountId());
        return ymtDeclareNode;
    }

    public static YmtDeclareNode fillYmtNode(SimpTcT simpTcT, String instrumentId) {
        YmtDeclareNode ymtDeclareNode = new YmtDeclareNode();
        ymtDeclareNode.setOrdrQtyTotal(simpTcT.getOrdrQty());
        ymtDeclareNode.setOrdrExePrc(simpTcT.getOrdrExePrc());
        ymtDeclareNode.setInstrumentId(instrumentId);
        ymtDeclareNode.setTradeCounts(1L);
        ymtDeclareNode.setCancelCounts(0L);
        ymtDeclareNode.setOrderCounts(0L);
        return ymtDeclareNode;
    }


    /**
     * 对只有十档行情数据的OrderBookRecord进行填充，得到一个完整的Recrod可以发到kafka
     *
     * @param rawRecord  只有十档行情数据的OrderBookRecord
     * @param mtpOrdcnmf OC数据
     * @return 完整的Recrod
     */
    public static OrderBookRecord fillOrderBookRecord(OrderBookRecord rawRecord, MtpOrdcnmf mtpOrdcnmf) {
        String instrumentId = mtpOrdcnmf.getInstrumentId();
        rawRecord.setActSeqNo(mtpOrdcnmf.getActnSeqNum());
        rawRecord.setSetNo(OrderBookCache.getCurrentSetNo());

        long ordNum = mtpOrdcnmf.getOrdrNum();
        rawRecord.setOrdrNo(ordNum);

        rawRecord.setLastOrdrNo(OrderBookCache.getCurrentOrderNo(instrumentId));
        OrderBookCache.setCurrentOrderNo(instrumentId, ordNum);

        rawRecord.setIsinCod(mtpOrdcnmf.getIsin());
        rawRecord.setInstrumentId(mtpOrdcnmf.getInstrumentId());
        Integer ordType = getOrdType(mtpOrdcnmf);
        rawRecord.setOrdType(ordType);
        rawRecord.setLastOrdType(OrderBookCache.getCurrentOrderType(instrumentId));
        OrderBookCache.setCurrentOrderType(instrumentId, ordType);

        rawRecord.setOrdrBuyCod(String.valueOf(mtpOrdcnmf.getProdBuySell()));
        rawRecord.setLevelPrice(OrderBookCache.getCurrentPriceSort(instrumentId));
        rawRecord.setPrice(mtpOrdcnmf.getOrdrExePrc());
        rawRecord.setTradePriceNew(OrderBookCache.getCurrentPrice(instrumentId));

        rawRecord.setOrdrQty(mtpOrdcnmf.getOrdrQty().longValue());
        // TODO  日期和时间拆开存
        rawRecord.setTrantim(mtpOrdcnmf.getTranDatTim());
        rawRecord.setTrandat(mtpOrdcnmf.getTranDatTim());

        rawRecord.setInvActId(mtpOrdcnmf.getInvAcctId());
        rawRecord.setYmtActId(mtpOrdcnmf.getYmtAccountId());
        return rawRecord;
    }

    // 得到当前OC数据的订单类型
    public static Integer getOrdType(MtpOrdcnmf mtpOrdcnmf) {
        // 数据类型(1:订单,2:补订单,3:撤单)
        if (mtpOrdcnmf.getActnSeqNum() <= 0) {
            return 2;
        }
        switch (mtpOrdcnmf.getTrnTypId()) {
            // A - 增加
            case 'A':
                return 1;
            // X - 撤销 | D - 删除
            case 'X':
            case 'D':
                return 3;
            default:
                // * T-止损触发 | * S-交易时段改变  | | C - 修改 * ‘‘-非交易订单
                log.error("订单簿重演：OC数据[事务类型]方向数据异常！-> {}", mtpOrdcnmf.toString());
        }
        return 9;
    }


    /**
     * 获取十档行情时，得到同时有买卖十档行情数据的原始OrderBookRecord
     *
     * @param buys     十档行情（买）
     * @param sells    十档行情（卖）
     * @param ymtBuys  一码通5档行情（买）
     * @param ymtSells 一码通5档行情（卖）
     * @return 属性待填充的OrderBookRecord
     */
    public static OrderBookRecord getRawRecord(List<PriceNode> buys, List<PriceNode> sells
            , List<YmtDeclareNode> ymtBuys, List<YmtDeclareNode> ymtSells) {
        OrderBookRecord orderBookRecord = new OrderBookRecord();
        // Buy
        if (buys.size() > 9) {
            orderBookRecord.setBuyPrice10(buys.get(9).getOrdrExePrc());
            orderBookRecord.setBuyVolume10(buys.get(9).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 8) {
            orderBookRecord.setBuyPrice9(buys.get(8).getOrdrExePrc());
            orderBookRecord.setBuyVolume9(buys.get(8).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 7) {
            orderBookRecord.setBuyPrice8(buys.get(7).getOrdrExePrc());
            orderBookRecord.setBuyVolume8(buys.get(7).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 6) {
            orderBookRecord.setBuyPrice7(buys.get(6).getOrdrExePrc());
            orderBookRecord.setBuyVolume7(buys.get(6).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 5) {
            orderBookRecord.setBuyPrice6(buys.get(5).getOrdrExePrc());
            orderBookRecord.setBuyVolume6(buys.get(5).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 4) {
            orderBookRecord.setBuyPrice5(buys.get(4).getOrdrExePrc());
            orderBookRecord.setBuyVolume5(buys.get(4).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 3) {
            orderBookRecord.setBuyPrice4(buys.get(3).getOrdrExePrc());
            orderBookRecord.setBuyVolume4(buys.get(3).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 2) {
            orderBookRecord.setBuyPrice3(buys.get(2).getOrdrExePrc());
            orderBookRecord.setBuyVolume3(buys.get(2).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 1) {
            orderBookRecord.setBuyPrice2(buys.get(1).getOrdrExePrc());
            orderBookRecord.setBuyVolume2(buys.get(1).getOrdrExeQtyTotal().longValue());
        }
        if (buys.size() > 0) {
            orderBookRecord.setBuyPrice1(buys.get(0).getOrdrExePrc());
            orderBookRecord.setBuyVolume1(buys.get(0).getOrdrExeQtyTotal().longValue());
        }

        // Sell
        if (sells.size() > 9) {
            orderBookRecord.setSellPrice10(sells.get(9).getOrdrExePrc());
            orderBookRecord.setSellVolume10(sells.get(9).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 8) {
            orderBookRecord.setSellPrice9(sells.get(8).getOrdrExePrc());
            orderBookRecord.setSellVolume9(sells.get(8).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 7) {
            orderBookRecord.setSellPrice8(sells.get(7).getOrdrExePrc());
            orderBookRecord.setSellVolume8(sells.get(7).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 6) {
            orderBookRecord.setSellPrice7(sells.get(6).getOrdrExePrc());
            orderBookRecord.setSellVolume7(sells.get(6).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 5) {
            orderBookRecord.setSellPrice6(sells.get(5).getOrdrExePrc());
            orderBookRecord.setSellVolume6(sells.get(5).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 4) {
            orderBookRecord.setSellPrice5(sells.get(4).getOrdrExePrc());
            orderBookRecord.setSellVolume5(sells.get(4).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 3) {
            orderBookRecord.setSellPrice4(sells.get(3).getOrdrExePrc());
            orderBookRecord.setSellVolume4(sells.get(3).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 2) {
            orderBookRecord.setSellPrice3(sells.get(2).getOrdrExePrc());
            orderBookRecord.setSellVolume3(sells.get(2).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 1) {
            orderBookRecord.setSellPrice2(sells.get(1).getOrdrExePrc());
            orderBookRecord.setSellVolume2(sells.get(1).getOrdrExeQtyTotal().longValue());
        }
        if (sells.size() > 0) {
            orderBookRecord.setSellPrice1(sells.get(0).getOrdrExePrc());
            orderBookRecord.setSellVolume1(sells.get(0).getOrdrExeQtyTotal().longValue());
        }

        // 一码通买
        if (ymtBuys.size() > 4) {
            orderBookRecord.setYmtBuyPrice5(ymtBuys.get(4).getOrdrExePrc());
            orderBookRecord.setYmtBuyOrdVolume5(ymtBuys.get(4).getOrdrQtyTotal().longValue());
        }
        if (ymtBuys.size() > 3) {
            orderBookRecord.setYmtBuyPrice4(ymtBuys.get(3).getOrdrExePrc());
            orderBookRecord.setYmtBuyOrdVolume4(ymtBuys.get(3).getOrdrQtyTotal().longValue());
        }
        if (ymtBuys.size() > 2) {
            orderBookRecord.setYmtBuyPrice3(ymtBuys.get(2).getOrdrExePrc());
            orderBookRecord.setYmtBuyOrdVolume3(ymtBuys.get(2).getOrdrQtyTotal().longValue());
        }
        if (ymtBuys.size() > 1) {
            orderBookRecord.setYmtBuyPrice2(ymtBuys.get(1).getOrdrExePrc());
            orderBookRecord.setYmtBuyOrdVolume2(ymtBuys.get(1).getOrdrQtyTotal().longValue());
        }
        if (ymtBuys.size() > 0) {
            orderBookRecord.setYmtBuyPrice1(ymtBuys.get(0).getOrdrExePrc());
            orderBookRecord.setYmtBuyOrdVolume1(ymtBuys.get(0).getOrdrQtyTotal().longValue());
        }

        // 一码通卖
        if (ymtSells.size() > 4) {
            orderBookRecord.setYmtSellPrice5(ymtSells.get(4).getOrdrExePrc());
            orderBookRecord.setYmtSellOrdVolume5(ymtSells.get(4).getOrdrQtyTotal().longValue());
        }
        if (ymtSells.size() > 3) {
            orderBookRecord.setYmtSellPrice4(ymtSells.get(3).getOrdrExePrc());
            orderBookRecord.setYmtSellOrdVolume4(ymtSells.get(3).getOrdrQtyTotal().longValue());
        }
        if (ymtSells.size() > 2) {
            orderBookRecord.setYmtSellPrice3(ymtSells.get(2).getOrdrExePrc());
            orderBookRecord.setYmtSellOrdVolume3(ymtSells.get(2).getOrdrQtyTotal().longValue());
        }
        if (ymtSells.size() > 1) {
            orderBookRecord.setYmtSellPrice2(ymtSells.get(1).getOrdrExePrc());
            orderBookRecord.setYmtSellOrdVolume2(ymtSells.get(1).getOrdrQtyTotal().longValue());
        }
        if (ymtSells.size() > 0) {
            orderBookRecord.setYmtSellPrice1(ymtSells.get(0).getOrdrExePrc());
            orderBookRecord.setYmtSellOrdVolume1(ymtSells.get(0).getOrdrQtyTotal().longValue());
        }
        return orderBookRecord;
    }

}
