package com.sse.rcp.order;

import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;
import com.sse.rcp.domains.order.OrderBookRecord;
import com.sse.rcp.domains.order.OrderNode;
import com.sse.rcp.domains.order.PriceNode;
import com.sse.rcp.domains.order.YmtDeclareNode;
import com.sse.rcp.kafka.KafkaProducerTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component("orderOperator")
public class OrderOperatorImpl implements OrderOperator {
    @Override
    public void processTrdcnmf(MtpTrdcnmf mtpTrdcnmf) {
        // TODO 没数据啊啊啊
        if (StringUtils.isEmpty(mtpTrdcnmf.getBuyInstrumentId())) {
            mtpTrdcnmf.setBuyInstrumentId("999996");
            mtpTrdcnmf.setSellInstrumentId("999996");
        }


        // 根据成交记录的【订单号】和【对手方信息摘除挂单】
        //产品买卖prodBuySell  0:买 1:卖 2:无
        SimpTcT tcBuy = mtpTrdcnmf.getTrdCfmBuy();
        processTrade(tcBuy, mtpTrdcnmf.getBuyInstrumentId(), mtpTrdcnmf.getBuyYmtAccountId(), 0);

        SimpTcT tcSell = mtpTrdcnmf.getTrdCfmSell();
        processTrade(tcSell, mtpTrdcnmf.getSellInstrumentId(), mtpTrdcnmf.getSellYmtAccountId(), 1);

    }

    @Override
    public void processOrdcnmf(MtpOrdcnmf mtpOrdcnmf) {
        // TODO 没数据啊啊啊
        if (StringUtils.isEmpty(mtpOrdcnmf.getInstrumentId())) {
            mtpOrdcnmf.setInstrumentId("999996");
        }
        if (StringUtils.isEmpty(mtpOrdcnmf.getYmtAccountId())) {
            mtpOrdcnmf.setYmtAccountId("666669");
        }
        if ('S' == mtpOrdcnmf.getTrnTypId() || 'E' == mtpOrdcnmf.getTrnTypId()) {
            mtpOrdcnmf.setTrnTypId('A');
        }
        // TODO 没数据啊啊


        if (mtpOrdcnmf.getOrderStatus() == 1 || mtpOrdcnmf.getProdBuySell() == 2) {
            // 订单冻结 | 买卖方向异常
            log.error("订单簿重演：订单被冻结或产品买卖方向异常！ -> {}", mtpOrdcnmf.toString());
            return;
        }
        switch (mtpOrdcnmf.getTrnTypId()) {
            // A - 增加
            case 'A':
                orderIncrease(mtpOrdcnmf);
                break;
            // X - 撤销 | D - 删除
            case 'X':
            case 'D':
                orderDecrease(mtpOrdcnmf);
                break;
            default:
                // T-止损触发 | * S-交易时段改变  | | C - 修改 * ‘‘-非交易订单
                log.error("订单簿重演：OC数据[事务类型]方向数据异常！-> {}", mtpOrdcnmf.toString());
        }
    }

    /**
     * 对于OC类型的消息，A - 增加 的情况下，应当对节点进行【加】操作
     *
     * @param mtpOrdcnmf oc消息
     */
    private static void orderIncrease(MtpOrdcnmf mtpOrdcnmf) {
        PriceNode priceNode = OrderBookHelper.fillPriceNode(mtpOrdcnmf, false);
        OrderNode orderNode = OrderBookHelper.fillOrderNode(mtpOrdcnmf);
        YmtDeclareNode modifyNode = OrderBookHelper.fillYmtNodeNew(mtpOrdcnmf);

        Integer instrumentKey = getPriceNodeKey(mtpOrdcnmf.getInstrumentId(), mtpOrdcnmf.getProdBuySell());
        // 维护产品价格链表
        OrderBook.modifyPriceChain(instrumentKey, priceNode);
        // 维护商品订单节点
        OrderBook.putOrderNodeData(orderNode);
        // 维护一码通链
        YmtBook.modifyChain(instrumentKey, mtpOrdcnmf.getYmtAccountId(), modifyNode);
        // 提取产品和一码通维度的档位行情，并推送kafka
        processOrder(mtpOrdcnmf);
    }

    /**
     * 对于OC类型的消息，X - 撤销 | D - 删除 的情况下，应当对节点进行【减】操作
     *
     * @param mtpOrdcnmf oc消息
     */
    private static void orderDecrease(MtpOrdcnmf mtpOrdcnmf) {
        PriceNode priceNode = OrderBookHelper.fillPriceNode(mtpOrdcnmf, true);
        OrderNode orderNode = OrderBookHelper.fillOrderNode(mtpOrdcnmf);
        YmtDeclareNode modifyNode = OrderBookHelper.fillYmtNodeCancel(mtpOrdcnmf);

        Integer instrumentKey = getPriceNodeKey(mtpOrdcnmf.getInstrumentId(), mtpOrdcnmf.getProdBuySell());

        // 维护产品价格链表
        OrderBook.modifyPriceChain(instrumentKey, priceNode);
        // 维护商品订单节点
        OrderBook.removeOrderNode(orderNode.getOrdrNum());
        // 维护一码通链表
        YmtBook.modifyChain(instrumentKey, mtpOrdcnmf.getYmtAccountId(), modifyNode);
        // 提取产品和一码通维度的档位行情，并推送kafka
        processOrder(mtpOrdcnmf);
    }

    /**
     * 提取产品和一码通维度的档位行情，并推送kafka
     *
     * @param mtpOrdcnmf OC
     */
    private static void processOrder(MtpOrdcnmf mtpOrdcnmf) {
        List<PriceNode> tenSortBuy = OrderBook.getFrontSort(getPriceNodeKey(mtpOrdcnmf.getInstrumentId(), 0));
        List<PriceNode> tenSortSell = OrderBook.getFrontSort(getPriceNodeKey(mtpOrdcnmf.getInstrumentId(), 1));

        List<YmtDeclareNode> ymtSortBuy = YmtBook.getFrontSort(
                getPriceNodeKey(mtpOrdcnmf.getInstrumentId(), 0), mtpOrdcnmf.getYmtAccountId());
        List<YmtDeclareNode> ymtSortSell = YmtBook.getFrontSort(
                getPriceNodeKey(mtpOrdcnmf.getInstrumentId(), 1), mtpOrdcnmf.getYmtAccountId());

        OrderBookRecord orderBookRecord = OrderBookHelper.fillOrderBookRecord(
                OrderBookHelper.getRawRecord(tenSortBuy, tenSortSell, ymtSortBuy, ymtSortSell), mtpOrdcnmf);

        KafkaProducerTool.pushDataToKafka(OrderBookCache.getTargetTopic(),orderBookRecord);
    }


    /**
     * 通过TC消息中的订单信息，处理对应买\卖方向的订单簿
     *
     * @param simpTcT      simpTcT
     * @param instrumentId instrumentId
     * @param ymtId        ymtId
     * @param prodBuySell  产品买卖prodBuySell  0:买 1:卖 2:无
     */
    private static void processTrade(SimpTcT simpTcT, String instrumentId, String ymtId, int prodBuySell) {
        Integer instrumentKey = getPriceNodeKey(instrumentId, prodBuySell);
        if (OrderBook.hadPriceNode(Integer.valueOf(instrumentId), simpTcT.getOrdrExePrc()) < 0) {
//            log.error("订单簿重演：该TC消息数据与订单簿数据不整合 -> SimpTcT:{}", simpTcT.toString()); TODO
            return;
        }

        PriceNode priceNode = OrderBookHelper.fillPriceNode(simpTcT, instrumentId);
        OrderNode orderNode = OrderBookHelper.fillOrderNode(simpTcT, instrumentId);
        boolean decreasePriceNode = OrderBook.modifyOrderNode(orderNode.getOrdrNum(), orderNode.getOrdrQty());
        if (decreasePriceNode) {
            priceNode.setOrderCounts(-1);
        }
        OrderBook.modifyPriceChain(instrumentKey, priceNode);
        // 更新当前产品的最新价格
        OrderBookCache.setCurrentPrice(instrumentId, simpTcT.getTradMtchPrc());

        // 维护一码通链表
        YmtDeclareNode modifyNode = OrderBookHelper.fillYmtNode(simpTcT, instrumentId);
        YmtBook.modifyChain(instrumentKey, ymtId, modifyNode);
    }

    /**
     * 通过instrumentId，分别获取价格节点 买行情链表和卖行情链表的key
     *
     * @param instrumentId instrumentId
     * @param prodBuySell  产品买卖 0:买 1:卖 2:无
     * @return instrumentKey
     */
    private static Integer getPriceNodeKey(String instrumentId, int prodBuySell) {
        //产品买卖prodBuySell  0:买 1:卖 2:无
        return prodBuySell == 0 ?
                OrderBook.makeInstrumentKeyBuy(Integer.parseInt(instrumentId))
                : OrderBook.makeInstrumentKeySell(Integer.parseInt(instrumentId));
    }


}
