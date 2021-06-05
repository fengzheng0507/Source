package com.sse.rcp.dimension;


import com.sse.rcp.dbsink.DimensionService;
import com.sse.rcp.domains.constant.AppConst;
import com.sse.rcp.domains.constant.RedisConstant;
import com.sse.rcp.domains.dimension.Instrument;
import com.sse.rcp.domains.dimension.YmtAccount;
import com.sse.rcp.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;


/**
 * 初始化维度数据，并放入缓存
 */
@Slf4j
@Component
public class InitializeDimensionData {

    /**
     * 产品代码、ISIX、ISIN 关系维度
     */
    public static Map<Integer, Instrument> instMap = new HashMap<>();

    /**
     * 投资者账户 - 一码通 关系维度
     */
    public static Map<String, YmtAccount> ymtActMap = new HashMap<>();
    @Autowired
    private DimensionService dimensionService;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Value("${dimension.source}")
    private String initialSource = "db";
//    /**
//     * 限制内存维度的最大条数，默认500w，后续改至redis则去掉该限制
//     */
//    @Value("${dimension.maxcounts}")
//    private int maxCounts = 5000000;

    @PostConstruct
    private void initSharedMemory() {
        // 初始化：预热维度数据的两种加载方式
        switch (initialSource) {
            case "db":
                initFromPostgre();
                break;
            case "file":
                initFromCsvFile();
                break;
        }
    }

    /**
     * 预热维度数据从 postgre 数据库加载
     */
    private void initFromPostgre() {
        getInstFromPg2Redis(RedisConstant.SET_QUERY_LIMIT_INSTRUMENT);
        getYmtFromPg2Redis(RedisConstant.SET_QUERY_LIMIT_YMTACCOUNT);
    }

    private void getInstFromPg2Redis(int perBatchSize) {
        Long counts = dimensionService.getInstrumentCounts();
        int tpCount = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i <= counts / perBatchSize; i++, tpCount += perBatchSize) {
            List<Instrument> instrumentList = dimensionService.findAllInstrument(perBatchSize, perBatchSize * i);
            RedisUtil.saveInstListToRedis(instrumentList);
            instrumentList.clear();
        }
        log.info("本次初始化：从postgre加载[产品信息表]写入 redis ：{} 条 ,耗时：{}ms ", tpCount, (System.currentTimeMillis() - startTime));
    }

    private void getYmtFromPg2Redis(int perBatchSize) {
        Long counts = dimensionService.getYmtCounts();
        int tpCount = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i <= counts / perBatchSize; i++, tpCount += perBatchSize) {
            List<YmtAccount> ymtAccountList = dimensionService.findAllYmt(perBatchSize, perBatchSize * i);
            RedisUtil.saveYmtListToRedis(ymtAccountList);
            ymtAccountList.clear();
        }
        log.info("本次初始化：从postgre数据库加载[一码通表]写入 redis ：{} 条 ,耗时：{}ms ", tpCount, (System.currentTimeMillis() - startTime));
    }


    /**
     * 预热维度数据从  CSV 文件中加载
     */
    private void initFromCsvFile() {
        log.info("--------维度数据预热开始！！！");
        // 解析classpath:DimensionData路径下的维度数据文件
        parseResource("classpath:dimensionData");
        // 解析完成后维度数据存在于 instMap ，ymtActMap 中， 调用数据库方法加载进postgre 数据库
        insertYmt();
        insertInst();

        log.info("---------维度数据预热完成！");

    }

    // 产品代码、ISIX、ISIN对应关系
    private static void parseInst(List<Instrument> instrumentList) {
        for (Instrument instrument : instrumentList) {
            instMap.put(instrument.getIsix(), instrument);
        }
    }

    // 投资者账户 - 一码通对应关系
    private static void parseYmt(List<YmtAccount> ymtRecList) {
        for (YmtAccount ymtAccount : ymtRecList) {
            ymtActMap.put(ymtAccount.getInvestorAccountId().trim(), ymtAccount);
        }
    }

    /**
     * 加载 CSV 文件中的维度数据到 postgre数据库 一码通表
     */
    private void insertYmt() {
        Iterator<String> iterators = ymtActMap.keySet().iterator();
        while (iterators.hasNext()) {
            YmtAccount ymtAccount = ymtActMap.get(iterators.next());
            dimensionService.insertYmt(ymtAccount);
        }
    }

    /**
     * 加载 CSV 文件中的维度数据到 postgre数据库 产品信息表
     */
    private void insertInst() {
        Iterator<Integer> iterators = instMap.keySet().iterator();
        while (iterators.hasNext()) {
            Instrument instrument = instMap.get(iterators.next());
            dimensionService.insertInstrument(instrument);
        }
    }

    /**
     * 解析指定路径下的所有数据文件，并放入缓存
     *
     * @param filePath 目标路径
     */
    private void parseResource(String filePath) {
        try {
            URI uri = resourceLoader.getResource(filePath).getURI();
            File[] files = new File(uri).listFiles();
            for (File f : files) {
                // 解析一个维度数据文件
                doParseResource(f.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 逐行解析目标文件的数据
     *
     * @param fileName 指定文件名
     */
    private void doParseResource(String fileName) {
        try {
            String url = "classpath:DimensionData/" + fileName;
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resourceLoader.getResource(url).getInputStream()));
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                if (StringUtils.isBlank(tmp)) {
                    continue;
                }
                // 解析文件行数据
                parse(fileName, tmp);
            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    // 根据不同文件类型，选择解析逻辑
    private static void parse(String fileName, String context) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String methodName = "parse" + fileName.substring(0, 1).toUpperCase() + fileName.replace(".csv", "").substring(1);
        Class<?> clazz = Class.forName("com.sse.rcp.dimension.InitializeDimensionData");
        Method method = clazz.getDeclaredMethod(methodName, String.class);
        method.invoke(null, context);
    }

    // 产品代码、ISIX、ISIN对应关系
    private static void parseInstruments(String context) {
//        String[] arr = context.split(AppConst.SPLIT_COMMA);
//        Instrument instrument = new Instrument();
//        instrument.setIsin(arr[0]);
//        instrument.setInstrumentId(arr[1]);
//        instrument.setIsix(arr[2]);
//
//        instMap.put(arr[2], instrument);
    }

    // 投资者账户 - 一码通对应关系
    private static void parseYmtaccount(String context) {
        String[] arr = context.split(AppConst.SPLIT_COMMA);
        YmtAccount ymtAccount = new YmtAccount();
        ymtAccount.setYmtAccountId(arr[0]);
        ymtAccount.setYmtAccountName(arr[1]);
        ymtAccount.setInvestorAccountId(arr[2]);

        ymtActMap.put(arr[2], ymtAccount);
    }
}
