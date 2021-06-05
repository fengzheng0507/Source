package com.sse.rcp.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private Class<T> clazz;

    /**
     * Serialize the given object to binary data.
     *
     * @param t object to serialize. Can be {@literal null}.
     * @return the equivalent binary data. Can be {@literal null}.
     */
    @Override
    public byte[] serialize(T t) throws SerializationException {
        return Optional.ofNullable(t)
                .map(r -> JSON.toJSONString(r, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET))
                .orElseGet(() -> new byte[0]);
    }

    /**
     * Deserialize an object from the given binary data.
     *
     * @param bytes object binary representation. Can be {@literal null}.
     * @return the equivalent object instance. Can be {@literal null}.
     */
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return Optional.ofNullable(bytes)
                .map(t -> JSON.parseObject(new String(t, DEFAULT_CHARSET), clazz))
                .orElseGet(() -> null);
    }
}
