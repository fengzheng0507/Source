package com.sse.rcp.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class OrderBookCache {

    public static final String ORDER_BOOK_TOPIC_KEYWORD = "cnmf";
    public static final String ORDER_BOOK_TOPIC_PREFIX = "rcps-sse.mtp.ordbook.";
    public static final String ORDER_BOOK_TOPIC_POSTFIX = ".sorted";

    // 当前线程（所消费topic）的SET号
    private static ThreadLocal<Integer> CURRENT_SET_NO = new ThreadLocal<>();

    // 当前线程（消费数据后）要输出的topic
    private static ThreadLocal<String> TREAD_TARGET_TOPIC = new ThreadLocal<>();

    // 分别记录 当前线程（所消费topic）中 每个产品当前最新的订单编号
    private static ThreadLocal<Map<String, Long>> CURRENT_ORDER_NO = new ThreadLocal<>();

    // 分别记录 当前线程（所消费topic）中 每个产品当前最新的订单类型
    private static ThreadLocal<Map<String, Integer>> CURRENT_ORDER_TYPE = new ThreadLocal<>();

    // 分别记录 当前线程（所消费topic）中 每个产品当前最新的订单价格
    private static ThreadLocal<Map<String, BigDecimal>> CURRENT_PRICE = new ThreadLocal<>();

    // 分别记录 当前线程（所消费topic）中 每个产品当前最新的订单的行情档位
    private static ThreadLocal<Map<String, Integer>> CURRENT_PRICE_SORT = new ThreadLocal<>();

    public static void InitLocalCache() {
        CURRENT_ORDER_NO.set(new HashMap<>());
        CURRENT_ORDER_TYPE.set(new HashMap<>());
        CURRENT_PRICE.set(new HashMap<>());
        CURRENT_PRICE_SORT.set(new HashMap<>());
        TREAD_TARGET_TOPIC.set(getTargetTopic(getSetNo()));
    }

    public static Integer getCurrentPriceSort(String instrumentId) {
        return CURRENT_PRICE_SORT.get().get(instrumentId) == null ? 0 : CURRENT_PRICE_SORT.get().get(instrumentId);
    }

    public static void setCurrentOrderNo(String instrumentId, Long currentOrderNo) {
        CURRENT_ORDER_NO.get().put(instrumentId, currentOrderNo);
    }

    public static Long getCurrentOrderNo(String instrumentId) {
        return CURRENT_ORDER_NO.get().get(instrumentId) == null ? 0L : CURRENT_ORDER_NO.get().get(instrumentId);
    }

    public static Integer getCurrentOrderType(String instrumentId) {
        return CURRENT_ORDER_TYPE.get().get(instrumentId) == null ? 0 : CURRENT_ORDER_TYPE.get().get(instrumentId);
    }

    public static void setCurrentOrderType(String instrumentId, Integer currentOrderType) {
        CURRENT_ORDER_TYPE.get().put(instrumentId, currentOrderType);
    }

    public static void setCurrentPrice(String instrumentId, BigDecimal currentPrice) {
        CURRENT_PRICE.get().put(instrumentId, currentPrice);
    }

    public static BigDecimal getCurrentPrice(String instrumentId) {
        return CURRENT_PRICE.get().get(instrumentId) == null ? new BigDecimal(0) : CURRENT_PRICE.get().get(instrumentId);
    }

    public static String getTargetTopic() {
        return TREAD_TARGET_TOPIC.get();
    }

    public static void clearCurrentLocal() {
        CURRENT_ORDER_NO.remove();
        CURRENT_ORDER_TYPE.remove();
        CURRENT_PRICE_SORT.remove();
        CURRENT_PRICE.remove();
    }

    /**
     * 获取当前线程（kafka consumer）对应的SET号
     * 在第一次获取的时候，初始化SET号和kafka producer要输出的topic，并存放在当前线程threadLocal中
     */
    public static Integer getCurrentSetNo() {
        if (CURRENT_SET_NO.get() == null) {
            String setNo = getSetNo();
            CURRENT_SET_NO.set(Integer.parseInt(setNo));
        }
        return CURRENT_SET_NO.get();
    }

    // 从线程threadLocal中获取kafka consumer的 SET
    public static String getSetNo() {
        String treadName = Thread.currentThread().getName();
        int subStart = treadName.indexOf(ORDER_BOOK_TOPIC_KEYWORD) + 5;
        return treadName.substring(subStart, subStart + 4);
    }

    // 从线程threadLocal中获取kafka producer要输出的topic
    public static String getTargetTopic(String setNo) {
        return ORDER_BOOK_TOPIC_PREFIX + setNo + ORDER_BOOK_TOPIC_POSTFIX;
    }

}
