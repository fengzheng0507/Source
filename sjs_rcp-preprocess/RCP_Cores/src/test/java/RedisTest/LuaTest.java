package RedisTest;

import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.utils.FastJsonRedisSerializer;
import config.LettuceRedisTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LettuceRedisTestConfig.class)
public class LuaTest {
    private static final Logger log = LoggerFactory.getLogger(IOTest.class);
    private static final String RANDOM_DISCARD = "discardSeq";
    private static final int RANDOM_LIMIT = 10;
    private static final String REDIS_SLOT_DEFAULT = "{}";

    private static DefaultRedisScript<List> getRandomsScript;

    private static RedisTemplate<String, Object> redisTemplate;

    @Test
    public void executeScript() {
        LuaTest.initScript();
        List datas = LuaTest.getRandomList(1);
        log.info(datas.toString());
    }

    public static List<? extends TopicData> getRandomList(long startSeq) {
        List<String> keys = new ArrayList<>();
        keys.add(padHashTag("test"));

        List<? extends TopicData> datas = null;
        try {
            datas = redisTemplate.execute(getRandomsScript, keys, padHashTag("order")
                    , padHashTag("123123"), padHashTag("666"), padHashTag("996")
                    , padHashTag("111.12345"), padHashTag("100.123")
                    , padHashTag("101.123")
                    , padHashTag("102.123"));
//                    redisTemplate.execute(getRandomsScript, keys, padHashTag("1")
//                    , padHashTag("3"), padHashTag("discardSeq"));
        } catch (Exception e) {
            log.error("线程{}执行lua脚本异常:{}->{}", Thread.currentThread().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        return datas;
    }

    public static String padHashTag(String raw) {
        return raw + "{" + "t1" + "}";
    }

    private static void initScript() {
        getRandomsScript = new DefaultRedisScript<List>();
        getRandomsScript.setResultType(List.class);
        getRandomsScript.setScriptSource(new ResourceScriptSource(
//                new ClassPathResource("luaScript/test.lua")));
        new ClassPathResource("luaScript/test.lua")));
    }

    @Autowired(required = true)
    @Qualifier("redisTemplate")
    private void setTemplate(RedisTemplate contextRedisTemplate) {
        redisTemplate = contextRedisTemplate;
//        redisTemplate.setValueSerializer(new FastJsonRedisSerializer());
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new FastJsonRedisSerializer<Map>());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashValueSerializer(new FastJsonRedisSerializer<Map>());
    }
}
