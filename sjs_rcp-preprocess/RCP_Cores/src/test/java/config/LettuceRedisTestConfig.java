package config;

import com.sse.rcp.domains.constant.AppConst;
import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Configuration
public class LettuceRedisTestConfig {
    private final String nodes = "10.112.103.5:7001,10.112.103.6:7001,10.112.103.7:7001,10.112.103.5:7002,10.112.103.6:7002,10.112.103.7:7002";
    private final String password = "sf@sf12dds";
    private final Integer maxIdle = 30;
    private final Integer minIdle = 1;
    private final Integer maxTotal = 30;
    private final Long maxWaitMillis = 500L;
    private final Integer maxRedirects = 3;

    @Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        LettucePoolingClientConfiguration lettucePoolingClientConfiguration
                = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();

        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        Set<RedisNode> nodeSet = new HashSet<>();
        String[] hosts = nodes.split(AppConst.SPLIT_COMMA);
        for (String h : hosts) {
            h = h.replaceAll(AppConst.REGULAR_BLANK, AppConst.EMPTY).replaceAll(AppConst.REGULAR_ENTER, AppConst.EMPTY);
            if (!StringUtils.isEmpty(h)) {
                String[] hs = h.split(AppConst.SPLIT_COLON);
                String host = hs[0];
                int port = Integer.parseInt(hs[1]);
                nodeSet.add(new RedisNode(host, port));
            }
        }
        redisClusterConfiguration.setClusterNodes(nodeSet);
        redisClusterConfiguration.setMaxRedirects(maxRedirects);
        redisClusterConfiguration.setPassword(password);
        return new LettuceConnectionFactory(redisClusterConfiguration, lettucePoolingClientConfiguration);
    }

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
//
//    @Bean
//    public DefaultRedisScript<Object> redisScript4RandomData() {
//        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>();
////        redisScript.setResultType();zx
//        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redisScripts/randomScript.lua")));
//        return redisScript;
//    }
}
