package com.sse.rcp.sort;

import com.sse.rcp.domains.constant.AppConst;
import com.sse.rcp.domains.ezei.TopicData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@DependsOn("redisTemplate")
@Component
public class SortCacheTool {
    private static final String RANDOM_DATA_PREFIX = "random-";
    private static final String RANDOM_DISCARD = "discardSeq";

    private static final String REDIS_SLOT_DEFAULT = "{}";

    private static RedisTemplate redisTemplate;

    private static RedisTemplate<String, Object> luaTemplate;

    private static DefaultRedisScript<List> getRandomsScript;

    /**
     * 调用lua脚本，获得一串seq连续递增的数据
     *
     * @param startSeq 起始的seq
     * @param limit    最多返回条数
     * @return TopicData列表
     */
    public static List<? extends TopicData> getRandomList(long startSeq, String limit) {
        List<String> keys = new ArrayList<>();
        keys.add(SortCacheTool.getRandomKey(startSeq));

        List<? extends TopicData> datas = new ArrayList<>();
        try {
            List currentDatas = luaTemplate.execute(getRandomsScript, keys, padHashTag(String.valueOf(startSeq), startSeq)
                    , padHashTag(limit, startSeq), padHashTag(RANDOM_DISCARD, startSeq));
            datas.addAll(currentDatas);
        } catch (Exception e) {
            log.error("线程{}执行lua脚本异常:{}->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        return datas;
    }

    /**
     * 通过seq获取数据
     *
     * @param seq actnSeqNum
     * @return TopicData
     */
    public static TopicData getRandomData(long seq) {
        return (TopicData) redisTemplate.opsForHash().get(SortCacheTool.getRandomKey(seq), seq);
    }

    /**
     * 将数据拉取到缓存中
     *
     * @param seq  actnSeqNum
     * @param data TopicData
     * @param <D>  TopicData
     */
    public static <D extends TopicData> void putRandomData(long seq, D data) {
        redisTemplate.opsForHash().put(SortCacheTool.getRandomKey(seq), seq, data);
    }

    public static void saveRandomDatas(Map<Long, TopicData> datas) {
        Map<Long, TopicData> fillDatas = DimensionDataHelper.fillDimension(datas);
        if (fillDatas != null && !fillDatas.isEmpty()) {
            redisTemplate.executePipelined(new RedisCallback<String>() {
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    datas.forEach((k, v) -> {
                        connection.hashCommands().hSet(
                                getRandomKey(k).getBytes(),
                                redisTemplate.getHashKeySerializer().serialize(k),
                                redisTemplate.getHashValueSerializer().serialize(v));
                    });
                    return null;
                }
            });
        }
    }

    /**
     * 将废弃的seq标记出来
     *
     * @param seq actnSeqNum
     * @param <D> TopicData
     */
    public static <D extends TopicData> void discardSeq(long seq) {
        redisTemplate.opsForSet().add(padHashTag(RANDOM_DISCARD, seq), seq);
    }

    /**
     * 按SET划分TopicData的hash
     *
     * @param seq actnSeqNum
     * @return 当前seq对应的hash key
     */
    public static String getRandomKey(long seq) {
        return padHashTag(RANDOM_DATA_PREFIX, seq);
    }

    /**
     * 给redis kay指定hash tag
     *
     * @param raw 要添加tag的key
     * @param seq 根据数据的seq决定tag的内容
     * @return 带hash tag的redis key
     */
    public static String padHashTag(String raw, long seq) {
        return raw + "{" + (seq / AppConst.SEQ_TAG_SCALE) + "}";
    }

    @PostConstruct
    public void init() {
        getRandomsScript = new DefaultRedisScript<List>();
        getRandomsScript.setResultType(List.class);
        getRandomsScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("luaScript/getRandoms.lua")));
    }

    @Autowired(required = true)
    @Qualifier("redisTemplate")
    private void setTemplate(RedisTemplate contextRedisTemplate) {
        redisTemplate = contextRedisTemplate;
    }

    @Autowired(required = true)
    @Qualifier("luaTemplate")
    private void setLuaTemplate(RedisTemplate contextRedisTemplate) {
        luaTemplate = contextRedisTemplate;
    }

}
