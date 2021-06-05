package com.sse.rcp.domains.constant;

public class RedisConstant {
    /**
     * 缺省状态默认的缓存超时时间（单位：小时）
     */
    public static final long TIMEOUT_HOURS_DEFAULT = 8;

    public static final Integer SET_QUERY_LIMIT_INSTRUMENT = 5000;
    public static final Integer SET_QUERY_LIMIT_YMTACCOUNT = 500;

    public static final String RANDOM_DATA_SET_0 = "randomSeq-0";
    public static final String RANDOM_DATA_SET_1 = "randomSeq-1";
    public static final String RANDOM_DATA_SET_2 = "randomSeq-2";
    public static final String RANDOM_DATA_SET_3 = "randomSeq-3";
    public static final String RANDOM_DATA_SET_4 = "randomSeq-4";
    public static final String RANDOM_DATA_SET_5 = "randomSeq-5";
    public static final String RANDOM_DATA_SET_6 = "randomSeq-6";
    public static final String RANDOM_DATA_SET_7 = "randomSeq-7";
    public static final String RANDOM_DATA_SET_8 = "randomSeq-8";
    public static final String RANDOM_DATA_SET_9 = "randomSeq-9";

    public static final String DIMENSION_DATA_SET_0 = "dimension-0";
    public static final String DIMENSION_DATA_SET_1 = "dimension-1";
    public static final String DIMENSION_DATA_SET_2 = "dimension-2";
    public static final String DIMENSION_DATA_SET_3 = "dimension-3";
    public static final String DIMENSION_DATA_SET_4 = "dimension-4";
    public static final String DIMENSION_DATA_SET_5 = "dimension-5";
    public static final String DIMENSION_DATA_SET_6 = "dimension-6";
    public static final String DIMENSION_DATA_SET_7 = "dimension-7";
    public static final String DIMENSION_DATA_SET_8 = "dimension-8";
    public static final String DIMENSION_DATA_SET_9 = "dimension-9";
    public static final String DIMENSION_DATA_SET_10 = "dimension-10";

    public static final String ORDER_DATA_SET_0 = "order-0";
    public static final String ORDER_DATA_SET_1 = "order-1";
    public static final String ORDER_DATA_SET_2 = "order-2";
    public static final String ORDER_DATA_SET_3 = "order-3";
    public static final String ORDER_DATA_SET_4 = "order-4";
    public static final String ORDER_DATA_SET_5 = "order-5";
    public static final String ORDER_DATA_SET_6 = "order-6";
    public static final String ORDER_DATA_SET_7 = "order-7";
    public static final String ORDER_DATA_SET_8 = "order-8";
    public static final String ORDER_DATA_SET_9 = "order-9";


    /**
     * 产品isix编号
     */
    public static final String REDIS_ISIX = "isix";
    /**
     * 证券12位国际代码
     */
    public static final String REDIS_ISIN = "isin";
    /**
     * 证券6位国内代码
     */
    public static final String REDIS_INSTRUMENT_ID = "instrumentId";
    /**
     * 投资者账户
     */
    public static final String REDIS_INV_ACT_ID = "invActId";
    /**
     * 一码通账户
     */
    public static final String REDIS_YMT_ACT_ID = "ymtActId";
    /**
     * 一码通户名
     */
    public static final String REDIS_YMT_ACT_NAME = "ymtActName";
    /**
     * 订单编号
     */
    public static final String REDIS_ORDER_NUMBER = "orderNum";
    /**
     * 订单价格（申报价格）
     */
    public static final String REDIS_ORDER_EXE_PRC = "orderExePrc";
    /**
     * 剩余订单有效数量
     */
    public static final String REDIS_ORDER_QTY = "orderQty";

}
