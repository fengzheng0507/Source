package RedisTest;

import config.LettuceRedisTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LettuceRedisTestConfig.class)
public class IOTest {
    private static final Logger log = LoggerFactory.getLogger(IOTest.class);
    @Autowired
    private RedisTemplate redisTemplate;

    private static BoundHashOperations ops;

    private final static String testKey = "testKey-666";

    private final static String testHashKey = "testHashKey-666";

    private final static String testHashValue = "testHashValue-66666666666666666666666";

    private final static String zsetKey = "zset-9";
    private final static String zsetValue = "zset-VVVVVV";

    private final static long times = 1_0000L;

    @Test
    public void zsetTest() {
        for (int i = 0; i < 1000; i++) {
            redisTemplate.opsForZSet().add(zsetKey, zsetValue + i, new Random(i).nextDouble());
        }
    }


    @Test
    public void speedContrast() {
        init();
        long start2 = System.nanoTime();
        for (int i = 0; i < times; i++) {
            redisTemplate.opsForHash().put(testKey, testHashKey + i * 100, testHashValue);
        }
        long end2 = System.nanoTime();
        log.warn("--->>>redisTemplate1 time cost:[{}]", (end2 - start2) / 1000_000_000.00);


        long start1 = System.nanoTime();
        for (int i = 0; i < times; i++) {
            ops.put(testHashKey + i * 10, testHashValue);
        }
        long end1 = System.nanoTime();
        log.warn("--->>>ops1 time cost:[{}]", (end1 - start1) / 1000_000_000.00);


        long start4 = System.nanoTime();
        for (int i = 0; i < times; i++) {
            redisTemplate.opsForHash().put(testKey, testHashKey + i * 10000, testHashValue);
        }
        long end4 = System.nanoTime();
        log.warn("--->>>redisTemplate1 time cost:[{}]", (end4 - start4) / 1000_000_000.00);

        long start3 = System.nanoTime();
        for (int i = 0; i < times; i++) {
            ops.put(testHashKey + i * 1000, testHashValue);
        }
        long end3 = System.nanoTime();
        log.warn("--->>>ops1 time cost:[{}]", (end3 - start3) / 1000_000_000.00);


    }


    private void init() {
        ops = redisTemplate.boundHashOps(testKey);
        ops.put(testHashKey, 666);
        ops.put(testHashKey, 999);
    }
}
