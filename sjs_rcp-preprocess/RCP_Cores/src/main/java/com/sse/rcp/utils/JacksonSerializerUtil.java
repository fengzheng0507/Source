package com.sse.rcp.utils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JacksonSerializerUtil {
    private static ObjectMapper genericMapper;

    static {
        genericMapper = new ObjectMapper();
        genericMapper.activateDefaultTyping(genericMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    public static String serializerWithType(Object data) throws JsonProcessingException {
        return genericMapper.writeValueAsString(data);
    }

    public static <T> T deserializeWithType(String dataStr, Class<T> objectClass) throws IOException {
        return (T) genericMapper.readValue(dataStr, objectClass);
    }

    public static <T> T deserializeWithType(byte[] value, Class<T> objectClass) throws IOException {
        return (T) genericMapper.readValue(value, objectClass);
    }
}
