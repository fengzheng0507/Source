package analyse;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class SortAppLogAnalyse {

    private static final String preProbe = "rdcnmf.0004";
    private static final String postProbe = "]";
    private static final int padding = 23;
    private static final String targetFlag = "[com.sse.rcp.sort.operator.SortOperatorImpl] ThreadName:[SortThread-rcpi";

    private static long lastSeq, currentSeq, count;
    private static final String logPath =
            "D:\\project\\gitrepo\\rcp_preprocess\\RCP_Cores\\target\\2021-03-15\\rcp_cores.2021-03-15.0.log";

    private static String[] contents = new String[3];

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
                if (!lineContext.contains(preProbe)) {
                    continue;
                }
                // 解析文件行数据
                analyse(lineContext);
            }
            log.info("检查完毕，没有大小逻辑错误！");
        } catch (IOException e) {
            e.printStackTrace();
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private static void analyse(String lineContext) {
        currentSeq = getTargetTime(lineContext);
        if (currentSeq > 0 && lastSeq > currentSeq) {
            log.error("\r\n");
            log.error("=================================================");

            for (String str : Arrays.asList(contents)) {
                log.error(str);
                log.error("-----------");
            }
            log.error("从第{}行开始出现异常！-----↓↓↓↓↓↓↓", count);
            log.error(lineContext);
            log.error("\r\n");
//            throw new RuntimeException();
        }
        lastSeq = currentSeq;
        for (int i = 0; i < contents.length - 1; i++) {
            contents[i] = contents[i + 1];
        }
        contents[contents.length - 1] = lineContext;
        count++;
    }

    private static long getTargetTime(String lineContext) {
        if (lineContext.contains(targetFlag) && lineContext.contains(preProbe)) {
            try {
                int start = lineContext.indexOf(preProbe);
                if (start >= 0) {
                    int end = lineContext.indexOf(postProbe, start + padding);
                    return Long.parseLong(lineContext.substring(start + padding, end));
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("------->", lineContext);
            }
        }
        return -1;
    }


}
