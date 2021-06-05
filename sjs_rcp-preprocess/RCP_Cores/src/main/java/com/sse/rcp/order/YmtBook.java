package com.sse.rcp.order;

import com.sse.rcp.domains.order.YmtDeclareNode;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YmtBook {
    private static final String NAME_SPLIT = "-";

    private static final int SORT_MAX = 5;

    private static volatile StringBuilder stringBuilder = new StringBuilder();

    // key->instrumentKey 证券6位国内代码+"-"+一码通账号
    // value->YmtDeclareNode
    private static ThreadLocal<Map<String, List<YmtDeclareNode>>> YMT_BOOK = new ThreadLocal<>();

    private static ThreadLocal<StringBuffer> LOCAL_BUFFER = new ThreadLocal<>();


    public static void modifyChain(int instrumentKey, String ymtAct, YmtDeclareNode modifyNode) {
        YmtDeclareNode currentNode = getTargetNodeByPrice(instrumentKey, ymtAct, modifyNode.getOrdrExePrc());
        // 如果不存在该节点，则将该节点插入链表
        if (currentNode == null) {
            List<YmtDeclareNode> currentNodes = getTargetChain(instrumentKey, ymtAct);
            // 如果链表为空链表，则是该链表第一条数据
            if (currentNodes.isEmpty()) {
                currentNodes.add(modifyNode);
                return;
            }
            // 买的规则是价高在前  证券6位国内代码*10 + 0（买）或1（卖）
            if ((instrumentKey & 1) == 0) {
                for (int nodeIdx = 0; nodeIdx < currentNodes.size(); nodeIdx++) {
                    if (modifyNode.getOrdrExePrc().compareTo(currentNodes.get(nodeIdx).getOrdrExePrc()) > 0) {
                        currentNodes.add(nodeIdx, modifyNode);
                        return;
                    }
                }
            }
            // 卖的规则是价低在前
            else {
                for (int nodeIdx = 0; nodeIdx < currentNodes.size(); nodeIdx++) {
                    if (modifyNode.getOrdrExePrc().compareTo(currentNodes.get(nodeIdx).getOrdrExePrc()) < 0) {
                        currentNodes.add(nodeIdx, modifyNode);
                        return;
                    }
                }
            }
            // 如果已经是最大index，还不能满足修正既有节点和插入链表的条件，则追加到链表末尾
            currentNodes.add(modifyNode);
        }
        // 如果存在该节点，则修改
        else {
            // 如果该节点没有剩余单量，则移除节点
            BigDecimal newOrderQtyTotal = currentNode.getOrdrQtyTotal().add(modifyNode.getOrdrQtyTotal());
            if (newOrderQtyTotal.compareTo(BigDecimal.ZERO) == 0) {
                getTargetChain(instrumentKey, ymtAct).remove(currentNode);
            } else {
                currentNode.setCancelCounts(currentNode.getCancelCounts() + modifyNode.getCancelCounts());
                currentNode.setOrderCounts(currentNode.getOrderCounts() + modifyNode.getOrderCounts());
                currentNode.setTradeCounts(currentNode.getTradeCounts() + modifyNode.getTradeCounts());
                currentNode.setOrdrQtyTotal(newOrderQtyTotal);
            }
        }
    }

    public static YmtDeclareNode getTargetNodeByPrice(int instrumentKey, String ymtAct, BigDecimal price) {
        YmtDeclareNode targetNode = null;
        for (YmtDeclareNode node : getTargetChain(instrumentKey, ymtAct)) {
            if (node.getOrdrExePrc().compareTo(price) == 0) {
                targetNode = node;
                break;
            }
        }
        return targetNode;
    }

    public static List<YmtDeclareNode> getFrontSort(int instrumentKey, String ymtAct) {
        List<YmtDeclareNode> currentNoes = getTargetChain(instrumentKey, ymtAct);

        List<YmtDeclareNode> targetNodes = new LinkedList<>();
        for (int i = 0; i < SORT_MAX; i++) {
            if (i < targetNodes.size()) {
                targetNodes.add(currentNoes.get(i));
            } else {
                YmtDeclareNode ymtDeclareNode = new YmtDeclareNode();
                ymtDeclareNode.setYmtActId("");
                ymtDeclareNode.setCancelCounts(0);
                ymtDeclareNode.setOrderCounts(0);
                ymtDeclareNode.setTradeCounts(0);
                ymtDeclareNode.setInstrumentId("");
                ymtDeclareNode.setOrdrExePrc(BigDecimal.ZERO);
                ymtDeclareNode.setOrdrQtyTotal(BigDecimal.ZERO);
                targetNodes.add(ymtDeclareNode);
            }
        }
        return targetNodes;
    }

    public static String getNodeKey(int instrumentKey, String ymtAct) {
        StringBuffer localBuffer = LOCAL_BUFFER.get();
        if (localBuffer == null) {
            LOCAL_BUFFER.set(new StringBuffer());
            localBuffer = LOCAL_BUFFER.get();
        }
        localBuffer.setLength(0);
        localBuffer.append(instrumentKey).append(NAME_SPLIT).append(ymtAct);
        return localBuffer.toString();
    }

    public static Map<String, List<YmtDeclareNode>> getLocalMap() {
        if (YMT_BOOK.get() == null) {
            synchronized (YMT_BOOK) {
                if (YMT_BOOK.get() == null) {
                    Map<String, List<YmtDeclareNode>> localMap = new HashMap();
                    YMT_BOOK.set(localMap);
                }
            }
        }
        return YMT_BOOK.get();
    }

    public static List<YmtDeclareNode> getTargetChain(int instrumentKey, String ymtAct) {
        return getLocalMap().computeIfAbsent(getNodeKey(instrumentKey, ymtAct), k -> new LinkedList<YmtDeclareNode>());
    }
}
