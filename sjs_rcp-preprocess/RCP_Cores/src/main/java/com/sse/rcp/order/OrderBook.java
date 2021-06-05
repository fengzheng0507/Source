package com.sse.rcp.order;

import com.sse.rcp.domains.order.OrderNode;
import com.sse.rcp.domains.order.PriceNode;
import com.sse.rcp.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OrderBook {

    private static final int INSTRUMENT_ID_SCALE = 10;

    private static final int SORT_MAX = 10;

    // key->instrumentKey 证券6位国内代码*10 + 0（买）或1（卖）
    // value->priceNode
    public static volatile Map<Integer, List<PriceNode>> ORDER_BOOK = new ConcurrentHashMap<>();

    /**
     * 通过instrumentKey 获取对应的买\卖十档行情链表
     *
     * @param instrumentKey instrumentKey
     * @return 十档行情链表（size最大为10，最小为0）
     */
    public static List<PriceNode> getFrontSort(Integer instrumentKey) {
        List<PriceNode> orderSort = ORDER_BOOK.get(instrumentKey);
        List<PriceNode> tenSort = new LinkedList<>();
        if (orderSort != null) {
            for (int i = 0; i < SORT_MAX; i++) {
                PriceNode priceNode = new PriceNode();
                if (i < orderSort.size()) {
                    priceNode.setInstrumentId(orderSort.get(i).getInstrumentId());
                    priceNode.setOrdrExePrc(orderSort.get(i).getOrdrExePrc());
                    priceNode.setOrdrExeQtyTotal(orderSort.get(i).getOrdrExeQtyTotal());
                    priceNode.setOrdrQtyTotal(orderSort.get(i).getOrdrQtyTotal());
                    priceNode.setOrderCounts(orderSort.get(i).getOrderCounts());
                } else {
                    priceNode.setInstrumentId("");
                    priceNode.setOrdrExePrc(BigDecimal.ZERO);
                    priceNode.setOrdrExeQtyTotal(BigDecimal.ZERO);
                    priceNode.setOrdrQtyTotal(BigDecimal.ZERO);
                    priceNode.setOrderCounts(0L);
                }
                tenSort.add(priceNode);
            }
        }
        return tenSort;
    }

    /**
     * 将一个【价格节点】的数据放入对应的买\卖档位行情链表中
     * 如果是新的【价格节点】，则插入新节点
     * 如果已有当前价格对应的【价格节点】，则更新该节点的数据
     *
     * @param instrumentKey   instrumentKey
     * @param modifyPriceNode 【价格节点】
     */
    public static void modifyPriceChain(Integer instrumentKey, PriceNode modifyPriceNode) {
        List<PriceNode> priceChain = ORDER_BOOK.computeIfAbsent(instrumentKey, k -> new LinkedList<PriceNode>());
        // 如果价格结点的链表为空，说明是该产品第一笔信息
        if (priceChain.isEmpty()) {
            priceChain.add(modifyPriceNode);
        }
        // 如果有链表，就修正链表数据
        else {
            for (int nodeIdx = 0; nodeIdx < priceChain.size(); nodeIdx++) {
                PriceNode currentPriceNode = priceChain.get(nodeIdx);
                // 如果有该价格的价格节点，修正该节点内容
                if (modifyPriceNode.getOrdrExePrc().compareTo(currentPriceNode.getOrdrExePrc()) == 0) {
                    currentPriceNode.setOrderCounts(currentPriceNode.getOrderCounts()
                            + modifyPriceNode.getOrderCounts());

                    currentPriceNode.setOrdrExeQtyTotal(currentPriceNode.getOrdrExeQtyTotal()
                            .add(modifyPriceNode.getOrdrExeQtyTotal()));

                    currentPriceNode.setOrdrQtyTotal(currentPriceNode.getOrdrQtyTotal()
                            .add(modifyPriceNode.getOrdrQtyTotal()));
                    // 如果当前【价格节点】的笔数剩余为0，则该节点应移除掉
                    if (currentPriceNode.getOrderCounts() < 1) {
                        ORDER_BOOK.get(instrumentKey).remove(nodeIdx);
                    }
                    break;
                }
                // 如果没有对应的价格节点，则应找到对应的档位插入节点
                else {
                    // instrumentKey 为奇数，则为卖单
                    if ((instrumentKey & 1) == 1) {
                        if (modifyPriceNode.getOrdrExePrc().compareTo(currentPriceNode.getOrdrExePrc()) < 0) {
                            ORDER_BOOK.get(instrumentKey).add(nodeIdx, modifyPriceNode);
                            break;
                        }
                    }
                    // instrumentKey 为偶数，则为买单
                    else {
                        if (modifyPriceNode.getOrdrExePrc().compareTo(currentPriceNode.getOrdrExePrc()) > 0) {
                            ORDER_BOOK.get(instrumentKey).add(nodeIdx, modifyPriceNode);
                            break;
                        }
                    }
                }

                // 如果已经是最大index，还不能满足修正既有节点和插入链表的条件，则追加到链表末尾
                if (nodeIdx == priceChain.size() - 1) {
                    ORDER_BOOK.get(instrumentKey).add(modifyPriceNode);
                }
            }
        }
    }

    /**
     * 通过TC消息中的订单信息，修正对应买\卖方向的订单簿的订单节点内的信息
     *
     * @param orderNum 订单编号
     * @param trdQty   TC中成交的量
     * @return 该节点是否消费完
     */
    public static boolean modifyOrderNode(long orderNum, BigDecimal trdQty) {
        boolean decreasePriceNode = false;
        OrderNode currentOrderNode = getOrderNodeData(orderNum);
        trdQty = trdQty.add(currentOrderNode.getOrdrQty() == null ? BigDecimal.ZERO : currentOrderNode.getOrdrQty());
        // 更新价格节点信息后，然后再写回redis
        if (trdQty.compareTo(BigDecimal.ZERO) <= 0) {
            // 如果一笔订单消费完成，则移除订单节点，并减少价格节点的笔数
            removeOrderNode(orderNum);
            decreasePriceNode = true;
        } else {
            currentOrderNode.setOrdrQty(trdQty);
            putOrderNodeData(currentOrderNode);
        }
        return decreasePriceNode;
    }

    /**
     * 判断当前价格是否有对应的价格档位
     *
     * @param instrumentKey instrumentKey
     * @param price         价格
     * @return 当前价格的档位（-1代表不存在当前价格对应的档位）
     */
    public static int hadPriceNode(Integer instrumentKey, BigDecimal price) {
        int idx = -1;
        List<PriceNode> priceNodeLinked = ORDER_BOOK.get(instrumentKey);
        if (priceNodeLinked == null || priceNodeLinked.isEmpty()) {
            return idx;
        }
        for (int i = 0; i < priceNodeLinked.size(); i++) {
            if (priceNodeLinked.get(i).getOrdrExePrc().compareTo(price) == 0) {
                return i;
            }
        }
        return idx;
    }

    // 生成买方的InstrumentKey
    public static Integer makeInstrumentKeyBuy(Integer instrumentId) {
        return instrumentId * INSTRUMENT_ID_SCALE;
    }

    // 生成卖方的InstrumentKey
    public static Integer makeInstrumentKeySell(Integer instrumentId) {
        return instrumentId * INSTRUMENT_ID_SCALE + 1;
    }


    // ↓↓↓ 处理缓存的部分单独独立成方法，以便切换缓存方案时统一修改此处  ↓↓↓
    public static void removeOrderNode(long orderNo) {
        RedisUtil.removeOrderNodeData(orderNo);
    }

    public static OrderNode getOrderNodeData(long orderNo) {
        return RedisUtil.getOrderNodeData(orderNo);
    }

    public static void putOrderNodeData(OrderNode orderNode) {
        RedisUtil.putOrderNodeData(orderNode.getOrdrNum(), orderNode);
    }
}
