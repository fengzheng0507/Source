package com.sse.rcp.parse;


import com.sse.rcp.domains.constant.AppConst;

import com.sse.rcp.domains.ezei.mtp.FfTxtGrpT;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ParseMockDataFromCsv {
    public static String ocKey = "mtpOrdcnmf";
    public static String tcKey = "mtpTrdcnmf";

    @Autowired
    ResourceLoader resourceLoader;

    /**
     * MockMapOC 申报确认模拟数据散列结构
     */
    public static Map<Long, Object> MockDataMap = new HashMap<Long, Object>();

    public MtpOrdcnmf mtpOrdcnmf = null;
    public MtpTrdcnmf mtpTrdcnmf = null;

    @PostConstruct
    private void loadCSVData() {
//        log.info("读取CSV模拟数据开始");
        parseResource("classpath:mockData");
//        log.info("读取CSV模拟数据结束！！！");

    }

    private void parseResource(String filePath) {
        try {
            URI uri = resourceLoader.getResource(filePath).getURI();
            File[] files = new File(uri).listFiles();

            //
            for (File f : files) {
                doParseResource(f.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
//            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

    /**
     * 逐行解析目标文件的数据
     *
     * @param fileName 指定文件名
     */
    private synchronized void doParseResource(String fileName) {
        try {
            String url = "classpath:mockData/" + fileName;
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resourceLoader.getResource(url).getInputStream()));
            String context;
            while ((context = reader.readLine()) != null) {
                if (StringUtils.isBlank(context)) {
                    continue;
                }
                // 解析文件行数据
                parseMockData(context);
            }
        } catch (IOException e) {
            e.printStackTrace();
//            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

    // 产品代码、ISIX、ISIN对应关系
    private void parseMockData(String context) {
        String[] arr = context.split(AppConst.SPLIT_COMMA);
        if (arr.length == 25) {
            mtpOrdcnmf = getMtpOrdcnmf(arr);
            MockDataMap.put(mtpOrdcnmf.getActnSeqNum(), mtpOrdcnmf);
        } else if (arr.length == 68) {
            mtpTrdcnmf = getMtpTrdcnmf(arr);
            long min = Math.min(mtpTrdcnmf.getTrdCfmBuy().getActnSeqNum(), mtpTrdcnmf.getTrdCfmSell().getActnSeqNum());
            MockDataMap.put(min, getMtpTrdcnmf(arr));
        }
    }


    private static MtpOrdcnmf getMtpOrdcnmf(String[] arr) {
        MtpOrdcnmf mtpOrdcnmf = new MtpOrdcnmf();
        mtpOrdcnmf.setActnSeqNum(Long.valueOf(arr[0]));
        mtpOrdcnmf.setIsix(Integer.valueOf(arr[1]));
        mtpOrdcnmf.setProdBuySell(Integer.valueOf(arr[2]));
        mtpOrdcnmf.setSellOut(Integer.valueOf(arr[3]));
        mtpOrdcnmf.setResTrade(Integer.valueOf(arr[4]));
        mtpOrdcnmf.setExecRestrict(Integer.valueOf(arr[5]));
        mtpOrdcnmf.setOrderType(Integer.valueOf(arr[6]));
        mtpOrdcnmf.setOrdrExePrc(new BigDecimal(arr[7]));
        mtpOrdcnmf.setOrdrQty(new BigDecimal(arr[8]));
        mtpOrdcnmf.setOrdrExeQty(new BigDecimal(arr[9]));
        mtpOrdcnmf.setOrdrNum(Long.valueOf(arr[10]));
        mtpOrdcnmf.setTranDatTim(Long.valueOf(arr[11]));

        FfTxtGrpT ffTxtGrp = new FfTxtGrpT();
        ffTxtGrp.setUserOrdNum(arr[12]);
        ffTxtGrp.setText(arr[13]);
        ffTxtGrp.setClgPrtpId(arr[14]);
        ffTxtGrp.setPbuOsCod(arr[15]);
        ffTxtGrp.setPbuBizCod(arr[16]);

        mtpOrdcnmf.setFfTxtGrp(ffTxtGrp);
        mtpOrdcnmf.setBrnId(arr[17]);
        mtpOrdcnmf.setInvAcctId(arr[18]);
        mtpOrdcnmf.setTrnTypId(arr[19].charAt(0));
        mtpOrdcnmf.setAccountType(Integer.valueOf(arr[20]));
        mtpOrdcnmf.setCreditLabel(Integer.valueOf(arr[21]));
        mtpOrdcnmf.setFullTrade(Integer.valueOf(arr[22]));
        mtpOrdcnmf.setOrderStatus(Integer.valueOf(arr[23]));
        mtpOrdcnmf.setFiller2(arr[24]);

        return mtpOrdcnmf;
    }


    private static MtpTrdcnmf getMtpTrdcnmf(String[] arr) {
        MtpTrdcnmf mtpTrdcnmf = new MtpTrdcnmf();
        SimpTcT trdCfmBuy = new SimpTcT();
        SimpTcT trdCfmSell = new SimpTcT();
        /******************trdCfmSell***********************/
        trdCfmBuy.setActnSeqNum(Long.valueOf(arr[0]));
        trdCfmBuy.setIsix(Integer.valueOf(arr[1]));
        trdCfmBuy.setProdBuySell(Integer.valueOf(arr[2]));
        trdCfmBuy.setSellOut(Integer.valueOf(arr[3]));
        trdCfmBuy.setResTrade(Integer.valueOf(arr[4]));
        trdCfmBuy.setExecRestrict(Integer.valueOf(arr[5]));
        trdCfmBuy.setOrderType(Integer.valueOf(arr[6]));
        trdCfmBuy.setTranIdNo(Long.valueOf(arr[7]));
        trdCfmBuy.setTradMtchPrc(new BigDecimal(arr[8]));
        trdCfmBuy.setTrdQty(new BigDecimal(arr[9]));
        trdCfmBuy.setOrdrNo(Long.valueOf(arr[10]));
        trdCfmBuy.setMktVal(Long.valueOf(arr[11]));
        trdCfmBuy.setOrdrQty(new BigDecimal(arr[12]));
        trdCfmBuy.setOrdrExeQty(new BigDecimal(arr[13]));
        trdCfmBuy.setOrdrEntTim(Long.valueOf(arr[14]));
        trdCfmBuy.setTotAucQty(Long.valueOf(arr[15]));
        trdCfmBuy.setOrdrExePrc(new BigDecimal(arr[16]));
        trdCfmBuy.setTranDatTim(Long.valueOf(arr[17]));
        trdCfmBuy.setTrnTypId(arr[18].charAt(0));
        trdCfmBuy.setInvAcctId(arr[19]);
        trdCfmBuy.setTrdTyp(arr[20].charAt(0));
        trdCfmBuy.setAccountType(Integer.valueOf(arr[21]));
        trdCfmBuy.setCreditLabel(Integer.valueOf(arr[22]));
        trdCfmBuy.setFullTrade(Integer.valueOf(arr[23]));
        trdCfmBuy.setOrderStatus(Integer.valueOf(arr[24]));


        FfTxtGrpT ffTxtGrpBuy = new FfTxtGrpT();
        ffTxtGrpBuy.setUserOrdNum(arr[25]);
        ffTxtGrpBuy.setText(arr[26]);
        ffTxtGrpBuy.setClgPrtpId(arr[27]);
        ffTxtGrpBuy.setPbuOsCod(arr[28]);
        ffTxtGrpBuy.setPbuBizCod(arr[29]);

        trdCfmBuy.setFfTxtGrpT(ffTxtGrpBuy);

        trdCfmBuy.setCtpyPbuId(arr[30]);
        trdCfmBuy.setBrnId(arr[31]);
        trdCfmBuy.setTranTypCod(arr[32]);
        trdCfmBuy.setPrcTypCod(arr[33].charAt(0));
        /******************trdCfmSell***********************/
        trdCfmSell.setActnSeqNum(Long.valueOf(arr[34]));
        trdCfmSell.setIsix(Integer.valueOf(arr[35]));
        trdCfmSell.setProdBuySell(Integer.valueOf(arr[36]));
        trdCfmSell.setSellOut(Integer.valueOf(arr[37]));
        trdCfmSell.setResTrade(Integer.valueOf(arr[38]));
        trdCfmSell.setExecRestrict(Integer.valueOf(arr[39]));
        trdCfmSell.setOrderType(Integer.valueOf(arr[40]));
        trdCfmSell.setTranIdNo(Long.valueOf(arr[41]));
        trdCfmSell.setTradMtchPrc(new BigDecimal(arr[42]));
        trdCfmSell.setTrdQty(new BigDecimal(arr[43]));
        trdCfmSell.setOrdrNo(Long.valueOf(arr[44]));
        trdCfmSell.setMktVal(Long.valueOf(arr[45]));
        trdCfmSell.setOrdrQty(new BigDecimal(arr[46]));
        trdCfmSell.setOrdrExeQty(new BigDecimal(arr[47]));
        trdCfmSell.setOrdrEntTim(Long.valueOf(arr[48]));
        trdCfmSell.setTotAucQty(Long.valueOf(arr[49]));
        trdCfmSell.setOrdrExePrc(new BigDecimal(arr[50]));
        trdCfmSell.setTranDatTim(Long.valueOf(arr[51]));
        trdCfmSell.setTrnTypId(arr[52].charAt(0));
        trdCfmSell.setInvAcctId(arr[53]);
        trdCfmSell.setTrdTyp(arr[54].charAt(0));
        trdCfmSell.setAccountType(Integer.valueOf(arr[55]));
        trdCfmSell.setCreditLabel(Integer.valueOf(arr[56]));
        trdCfmSell.setFullTrade(Integer.valueOf(arr[57]));
        trdCfmSell.setOrderStatus(Integer.valueOf(arr[58]));


        FfTxtGrpT ffTxtGrpSell = new FfTxtGrpT();
        ffTxtGrpSell.setUserOrdNum(arr[59]);
        ffTxtGrpSell.setText(arr[60]);
        ffTxtGrpSell.setClgPrtpId(arr[61]);
        ffTxtGrpSell.setPbuOsCod(arr[62]);
        ffTxtGrpSell.setPbuBizCod(arr[63]);

        trdCfmSell.setFfTxtGrpT(ffTxtGrpBuy);

        trdCfmSell.setCtpyPbuId(arr[64]);
        trdCfmSell.setBrnId(arr[65]);
        trdCfmSell.setTranTypCod(arr[66]);
        trdCfmSell.setPrcTypCod(arr[67].charAt(0));

        mtpTrdcnmf.setTrdCfmBuy(trdCfmBuy);
        mtpTrdcnmf.setTrdCfmSell(trdCfmSell);

        return mtpTrdcnmf;
    }
}




