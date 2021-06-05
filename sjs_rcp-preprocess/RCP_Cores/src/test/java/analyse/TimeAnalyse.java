package analyse;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

@Slf4j
public class TimeAnalyse {

    private static final String preProbe = "TotalTime[";
    private static final String postProbe = "]";
    private static final int padding = 10;

    private static long maxTime, minTime;
    private static BigInteger count = new BigInteger("0"), avgTimeBig, currentTime;

    private static final String targetTopic = "cnmf.0004";
    private static final String logPath =
            "D:\\project\\gitrepo\\rcp_preprocess\\RCP_Cores\\target\\2021-03-15\\rcp_cores.2021-03-15.0.log";

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logPath));
            String lineContext;
            while ((lineContext = reader.readLine()) != null) {
                if (StringUtils.isBlank(lineContext)) {
                    continue;
                }
                if (lineContext.contains("->END")) {
                    break;
                }
                // 解析文件行数据
                analyse(lineContext);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(Arrays.toString(e.getStackTrace()));
        }
        log.info("{}日志中，{}SET共推送{}条数据，最小耗时{}，最大耗时{}，平均耗时{}，总应耗时{}秒；整体速率：{}条/秒", logPath, targetTopic, count.toString()
                , minTime, maxTime, avgTimeBig.toString(), count.multiply(avgTimeBig).divide(new BigInteger("100000000")), count.divide(count.multiply(avgTimeBig).divide(new BigInteger("100000000"))));
    }

    private static void analyse(String lineContext) {
        currentTime = getTargetTime(lineContext);
        if (currentTime != null) {
            avgTimeBig = avgTimeBig == null ? currentTime : ((avgTimeBig.multiply(count).add(currentTime)).divide(count.add(BigInteger.ONE)));
            maxTime = Math.max(maxTime, currentTime.longValue());
            minTime = minTime == 0 ? currentTime.longValue() : Math.min(minTime, currentTime.longValue());
        }
    }

    private static BigInteger getTargetTime(String lineContext) {

        try {
            if (lineContext.contains(targetTopic)) {
                int start = lineContext.indexOf(preProbe);
                if (start >= 0) {
                    count = count.add(BigInteger.ONE);
                    int end = lineContext.indexOf(postProbe, start);
                    String target = lineContext.substring(start + padding, end);
                    return new BigInteger(target);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("------->", lineContext);
        }

        return null;
    }


}
