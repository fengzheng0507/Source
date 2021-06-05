package com.sse.rcp.push;

import com.sse.rcp.domains.ezei.mtp.FfTxtGrpT;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.domains.ezei.mtp.SimpTcT;
import com.sse.rcp.mock.MockData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Component
public class PersistMockData2Csv {


    private static AtomicLong seq = new AtomicLong(3590004000000001L);

    @Value("${fileName}")
    private String fileName;

    /**
     * 模拟 Json 数据
     */
    public void pushMockData2CSV() {
        mockDataWrite();
        log.info("mock  json data is finish !!!");
    }

    /**
     * 模拟 数据写入 CSV 文件 ，持久化至本地磁盘
     */
    private void mockDataWrite() {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName, true);
            while (true) {
                mockOCWrite(fileWriter);
                mockTCWrite(fileWriter);
                fileWriter.flush();


                if (seq.doubleValue() > 3590004000001200L) {
                    System.out.println("模拟造数程序完成----文件为CSV");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 模拟多条OC JSON 数据 ，塞入 fileWriter
     *
     * @param fileWriter
     * @throws IOException
     */
    private void mockOCWrite(FileWriter fileWriter) throws IOException {
        for (int i = 0; i < Math.random() * 10 + 10; i++) {

            fileWriter.append(getMtpOrdcnmf2String(MockData.generateOCData(seq)));
            seq.incrementAndGet();

        }
    }

    /**
     * 模拟单条TC JSON 数据 ，塞入 fileWriter
     *
     * @param fileWriter
     * @throws IOException
     */
    private void mockTCWrite(FileWriter fileWriter) throws IOException {
        fileWriter.append(getMtpTrdcnmf2String(MockData.generateTCData(seq)));
        seq.incrementAndGet();
    }

    public static String getMtpOrdcnmf2String(MtpOrdcnmf mtpOrdcnmf) {

        FfTxtGrpT ffTxtGrp = mtpOrdcnmf.getFfTxtGrp();

        return
                mtpOrdcnmf.getActnSeqNum() +
                        "," + mtpOrdcnmf.getIsix() +
                        "," + mtpOrdcnmf.getProdBuySell() +
                        "," + mtpOrdcnmf.getSellOut() +
                        "," + mtpOrdcnmf.getResTrade() +
                        "," + mtpOrdcnmf.getExecRestrict() +
                        "," + mtpOrdcnmf.getOrderType() +
                        "," + mtpOrdcnmf.getOrdrExePrc() +
                        "," + mtpOrdcnmf.getOrdrQty() +
                        "," + mtpOrdcnmf.getOrdrExeQty() +
                        "," + mtpOrdcnmf.getOrdrNum() +
                        "," + mtpOrdcnmf.getTranDatTim() +
                        "," + ffTxtGrp.getUserOrdNum() +
                        "," + ffTxtGrp.getText() +
                        "," + ffTxtGrp.getClgPrtpId() +
                        "," + ffTxtGrp.getPbuOsCod() +
                        "," + ffTxtGrp.getPbuBizCod() +
                        "," + mtpOrdcnmf.getBrnId() +
                        "," + mtpOrdcnmf.getInvAcctId() +
                        "," + mtpOrdcnmf.getTrnTypId() +
                        "," + mtpOrdcnmf.getAccountType() +
                        "," + mtpOrdcnmf.getCreditLabel() +
                        "," + mtpOrdcnmf.getFullTrade() +
                        "," + mtpOrdcnmf.getOrderStatus() +
                        "," + mtpOrdcnmf.getFiller2() + "\n";

    }

    public static String getMtpTrdcnmf2String(MtpTrdcnmf mtpTrdcnmf) {
        SimpTcT trdCfmBuy = mtpTrdcnmf.getTrdCfmBuy();
        SimpTcT trdCfmSell = mtpTrdcnmf.getTrdCfmSell();
        return
                trdCfmBuy.getActnSeqNum() +
                        "," + trdCfmBuy.getIsix() +
                        "," + trdCfmBuy.getProdBuySell() +
                        "," + trdCfmBuy.getSellOut() +
                        "," + trdCfmBuy.getResTrade() +
                        "," + trdCfmBuy.getExecRestrict() +
                        "," + trdCfmBuy.getOrderType() +
                        "," + trdCfmBuy.getTranIdNo() +
                        "," + trdCfmBuy.getTradMtchPrc() +
                        "," + trdCfmBuy.getTrdQty() +
                        "," + trdCfmBuy.getOrdrNo() +
                        "," + trdCfmBuy.getMktVal() +
                        "," + trdCfmBuy.getOrdrQty() +
                        "," + trdCfmBuy.getOrdrExeQty() +
                        "," + trdCfmBuy.getOrdrEntTim() +
                        "," + trdCfmBuy.getTotAucQty() +
                        "," + trdCfmBuy.getOrdrExePrc() +
                        "," + trdCfmBuy.getTranDatTim() +
                        "," + trdCfmBuy.getTrnTypId() +
                        "," + trdCfmBuy.getInvAcctId() +
                        "," + trdCfmBuy.getTrdTyp() +
                        "," + trdCfmBuy.getAccountType() +
                        "," + trdCfmBuy.getCreditLabel() +
                        "," + trdCfmBuy.getFullTrade() +
                        "," + trdCfmBuy.getOrderStatus() +
                        "," + trdCfmBuy.getFfTxtGrpT().getUserOrdNum() +
                        "," + trdCfmBuy.getFfTxtGrpT().getText() +
                        "," + trdCfmBuy.getFfTxtGrpT().getClgPrtpId() +
                        "," + trdCfmBuy.getFfTxtGrpT().getPbuOsCod() +
                        "," + trdCfmBuy.getFfTxtGrpT().getPbuBizCod() +
                        "," + trdCfmBuy.getCtpyPbuId() +
                        "," + trdCfmBuy.getBrnId() +
                        "," + trdCfmBuy.getTranTypCod() +
                        "," + trdCfmBuy.getPrcTypCod() +
                        "," + trdCfmSell.getActnSeqNum() +
                        "," + trdCfmSell.getIsix() +
                        "," + trdCfmSell.getProdBuySell() +
                        "," + trdCfmSell.getSellOut() +
                        "," + trdCfmSell.getResTrade() +
                        "," + trdCfmSell.getExecRestrict() +
                        "," + trdCfmSell.getOrderType() +
                        "," + trdCfmSell.getTranIdNo() +
                        "," + trdCfmSell.getTradMtchPrc() +
                        "," + trdCfmSell.getTrdQty() +
                        "," + trdCfmSell.getOrdrNo() +
                        "," + trdCfmSell.getMktVal() +
                        "," + trdCfmSell.getOrdrQty() +
                        "," + trdCfmSell.getOrdrExeQty() +
                        "," + trdCfmSell.getOrdrEntTim() +
                        "," + trdCfmSell.getTotAucQty() +
                        "," + trdCfmSell.getOrdrExePrc() +
                        "," + trdCfmSell.getTranDatTim() +
                        "," + trdCfmSell.getTrnTypId() +
                        "," + trdCfmSell.getInvAcctId() +
                        "," + trdCfmSell.getTrdTyp() +
                        "," + trdCfmSell.getAccountType() +
                        "," + trdCfmSell.getCreditLabel() +
                        "," + trdCfmSell.getFullTrade() +
                        "," + trdCfmSell.getOrderStatus() +
                        "," + trdCfmSell.getFfTxtGrpT().getUserOrdNum() +
                        "," + trdCfmSell.getFfTxtGrpT().getText() +
                        "," + trdCfmSell.getFfTxtGrpT().getClgPrtpId() +
                        "," + trdCfmSell.getFfTxtGrpT().getPbuOsCod() +
                        "," + trdCfmSell.getFfTxtGrpT().getPbuBizCod() +
                        "," + trdCfmSell.getCtpyPbuId() +
                        "," + trdCfmSell.getBrnId() +
                        "," + trdCfmSell.getTranTypCod() +
                        "," + trdCfmSell.getPrcTypCod() + "\n";
    }
}
