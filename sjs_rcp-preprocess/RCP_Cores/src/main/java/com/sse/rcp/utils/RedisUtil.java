package com.sse.rcp.utils;

import com.sse.rcp.domains.constant.RedisConstant;
import com.sse.rcp.domains.dimension.Instrument;
import com.sse.rcp.domains.dimension.YmtAccount;
import com.sse.rcp.domains.order.OrderNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@DependsOn("redisTemplate")
@Component
public class RedisUtil {

    private static volatile RedisTemplate redisTemplate;


    // ↓↓↓ 维度部分的缓存处理 ↓↓↓ ///////////////////

    /**
     * 从缓存中获取维度数据
     *
     * @param key Key
     * @return 维度数据
     */
    public static Object getDimensionData(String key) {
        return redisTemplate.opsForHash().get(getDimensionDataKey(key), key);
    }

    // ↓↓↓ 订单簿部分的缓存处理 ↓↓↓ ///////////////////

    /**
     * 获取订单簿的订单节点数据
     *
     * @param orderNo 订单编号
     * @return 订单节点数据
     */
    public static OrderNode getOrderNodeData(long orderNo) {
        return (OrderNode) redisTemplate.opsForHash().get(getOrderNodeDataKey(orderNo), orderNo);
    }

    /**
     * 放入订单簿的订单节点数据
     *
     * @param orderNo 订单编号
     * @param value   订单节点数据
     */
    public static void putOrderNodeData(long orderNo, OrderNode value) {
        redisTemplate.opsForHash().put(getOrderNodeDataKey(orderNo), orderNo, value);
    }

    /**
     * 移除订单簿的订单节点数据
     *
     * @param orderNo 订单编号
     */
    public static void removeOrderNodeData(long orderNo) {
        redisTemplate.opsForHash().delete(getOrderNodeDataKey(orderNo), orderNo);
    }


    // ↓↓↓ 获得打散后的redis key：把数据打散到不同的Hash中去，使得数据在redis节点间分布均匀一些 ↓↓↓ ///////////////////

    public static String getDimensionDataKey(String key) {
        String dimensionDataSet = "";
        int op;
        try {
            op = Integer.parseInt(key.substring(key.length() - 1));
        } catch (NumberFormatException e) {
            log.warn("seq:[{}] dimensionDataSet Error", key);
            return RedisConstant.DIMENSION_DATA_SET_10;
        }

        switch (op) {
            case 0:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_0;
                break;
            case 1:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_1;
                break;
            case 2:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_2;
                break;
            case 3:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_3;
                break;
            case 4:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_4;
                break;
            case 5:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_5;
                break;
            case 6:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_6;
                break;
            case 7:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_7;
                break;
            case 8:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_8;
                break;
            case 9:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_9;
                break;
            default:
                dimensionDataSet = RedisConstant.DIMENSION_DATA_SET_10;
                log.warn("seq:[{}] dimensionDataSet Error", key);
        }
        return dimensionDataSet;
    }

    public static String getOrderNodeDataKey(long orderNo) {
        String orderNodeSet = "";
        switch ((int) (orderNo ^ (orderNo >>> 3 << 3))) {
            case 0:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_0;
                break;
            case 1:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_1;
                break;
            case 2:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_2;
                break;
            case 3:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_3;
                break;
            case 4:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_4;
                break;
            case 5:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_5;
                break;
            case 6:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_6;
                break;
            case 7:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_7;
                break;
            default:
                orderNodeSet = RedisConstant.ORDER_DATA_SET_9;
                log.error("seq:[{}] getOrderNodeDataKey Error", orderNo);
        }
        return orderNodeSet;
    }

    public static RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    @Autowired(required = true)
    @Qualifier("redisTemplate")
    private void setRedisUtil(RedisTemplate contextRedisTemplate) {
        redisTemplate = contextRedisTemplate;
    }

    public static Map<Integer, Instrument> getInstDimensionDatas(List<Integer> keys) {
        if (keys == null || keys.isEmpty())
            return new HashMap<>();
        String redisKey = getDimensionDataKey(String.valueOf(keys.get(0)));
        List<Instrument> datas = redisTemplate.opsForHash().multiGet(redisKey, keys);
        if (datas.isEmpty())
            return new HashMap<>();
        return datas.parallelStream().filter(Objects::nonNull).collect(Collectors.toMap(Instrument::getIsix, p -> p));
    }

    public static Map<String, YmtAccount> getYmtDimensionDatas(List<String> keys) {
        if (keys == null || keys.isEmpty())
            return new HashMap<>();
        String redisKey = getDimensionDataKey(keys.get(0));
        List<YmtAccount> datas = redisTemplate.opsForHash().multiGet(redisKey, keys);
        if (datas.isEmpty())
            return new HashMap<>();
        return datas.parallelStream().filter(Objects::nonNull).collect(Collectors.toMap(YmtAccount::getInvestorAccountId, p -> p));
    }

    public static void saveInstListToRedis(List<Instrument> instrumentList) {
        redisTemplate.executePipelined(new RedisCallback<List<Instrument>>() {
            @Override
            public List<Instrument> doInRedis(RedisConnection connection) throws DataAccessException {
                for (Instrument instrument : instrumentList) {
                    connection.hashCommands().hSet(
                            getDimensionDataKey(String.valueOf(instrument.getIsix())).getBytes(),
                            redisTemplate.getHashKeySerializer().serialize(instrument.getIsix()),
                            redisTemplate.getHashValueSerializer().serialize(instrument));
                }
                return null;
            }
        });
    }

    public static void saveYmtListToRedis(List<YmtAccount> ymtAccountList) {
        redisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                for (YmtAccount ymtAccount : ymtAccountList) {
                    connection.hashCommands().hSet(
                            getDimensionDataKey(ymtAccount.getInvestorAccountId()).getBytes(),
                            redisTemplate.getHashKeySerializer().serialize(ymtAccount.getInvestorAccountId()),
                            redisTemplate.getHashValueSerializer().serialize(ymtAccount));
                }
                return null;
            }
        });
    }
}
