package com.sse.rcp.sort;


import com.sse.rcp.domains.dimension.Instrument;
import com.sse.rcp.domains.dimension.YmtAccount;
import com.sse.rcp.domains.ezei.TopicData;
import com.sse.rcp.domains.ezei.mtp.MtpOrdcnmf;
import com.sse.rcp.domains.ezei.mtp.MtpTrdcnmf;
import com.sse.rcp.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DimensionDataHelper {

    /**
     * 维度注入
     *
     * @param datas 接入的ezei原始数据  k->seq,v->data
     * @return 维度join后的数据
     */
    public static Map<Long, TopicData> fillDimension(Map<Long, TopicData> datas) {
        if (datas == null || datas.isEmpty())
            return null;

        long startDimeTime = System.currentTimeMillis();
        Map<Long, TopicData> filledData;
        boolean isOc = datas.values().iterator().next() instanceof MtpOrdcnmf;
        List<Integer> instKeys;
        List<String> ymtKeys;
        if (isOc) {
            instKeys = datas.entrySet().parallelStream().map(e -> {
                TopicData data = e.getValue();
                return ((MtpOrdcnmf) data).getIsix();
            }).filter(p -> p > 0).distinct().collect(Collectors.toList());

            ymtKeys = datas.entrySet().parallelStream().map(e -> {
                TopicData data = e.getValue();
                String[] invActIds = null;
                String id = ((MtpOrdcnmf) data).getInvAcctId();
                if (!org.springframework.util.StringUtils.isEmpty(id)) {
                    invActIds = new String[1];
                    invActIds[0] = id;
                }
                return invActIds;
            }).filter(Objects::nonNull).flatMap(Arrays::stream).distinct().collect(Collectors.toList());
        } else {
            instKeys = datas.entrySet().parallelStream().map(e -> {
                TopicData data = e.getValue();
                return ((MtpTrdcnmf) data).getTrdCfmBuy().getIsix();
            }).filter(p -> p > 0).distinct().collect(Collectors.toList());

            ymtKeys = datas.entrySet().parallelStream().map(e -> {
                TopicData data = e.getValue();
                String[] invActIds = null;
                String idB = ((MtpTrdcnmf) data).getTrdCfmBuy().getInvAcctId();
                String idS = ((MtpTrdcnmf) data).getTrdCfmSell().getInvAcctId();
                if (!org.springframework.util.StringUtils.isEmpty(idB) && !org.springframework.util.StringUtils.isEmpty(idS)) {
                    invActIds = new String[2];
                    invActIds[0] = idB;
                    invActIds[1] = idS;
                }
                return invActIds;
            }).filter(Objects::nonNull).flatMap(Arrays::stream).distinct().collect(Collectors.toList());
        }

        Map<Integer, Instrument> instMap = new HashMap(2048);
        Map<String, YmtAccount> ymtMap = new HashMap(2048);
        for (int i = 0; i < 10; i++) {
            int currentIdx = i;
            List<Integer> instKs = instKeys.parallelStream().filter(p -> (p % 10) == currentIdx).collect(Collectors.toList());
            if (instKs.size() > 0 && instKs.get(0) > 0) {
                instMap.putAll(RedisUtil.getInstDimensionDatas(instKs));
            }

            List<String> ymtKs = ymtKeys.parallelStream().filter(p -> Integer.parseInt(p.substring(p.length() - 1)) % 10 == currentIdx).collect(Collectors.toList());
            if (ymtKs.size() > 0 && !org.springframework.util.StringUtils.isEmpty(ymtKs.get(0))) {
                ymtMap.putAll(RedisUtil.getYmtDimensionDatas(ymtKs));
            }
        }

        if (isOc) {
            filledData = datas.entrySet().parallelStream().map(en -> {
                MtpOrdcnmf data = (MtpOrdcnmf) en.getValue();
                Instrument instData = instMap.get(data.getIsix());
                YmtAccount ymtData = ymtMap.get(data.getInvAcctId());
                //////////  TODO
                if (instData == null || ymtData == null) {
                    // log.warn("该OC信息维度注入失败！-->[{}]", mtpOrdcnmf.toString());  TODO 现在日志太多了，先关闭
                    // TODO 硬代码做数据
                    data.setIsin(String.valueOf(data.getIsix()));
                    data.setInstrumentId(String.valueOf(data.getIsix()));
                    data.setYmtAccountId("6" + ((data.getInvAcctId().length() > 2) ? data.getInvAcctId().substring(2) : "66"));
                    data.setYmtAccountName("ymtName");
                } else {
                    data.setIsin(instData.getIsin());
                    data.setInstrumentId(instData.getInstrumentId());
                    data.setYmtAccountId(ymtData.getYmtAccountId());
                    data.setYmtAccountName(ymtData.getYmtAccountName());
                }
                return en;
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            filledData = datas.entrySet().parallelStream().map(en -> {
                MtpTrdcnmf data = (MtpTrdcnmf) en.getValue();
                Instrument instData = instMap.get(data.getTrdCfmBuy().getIsix());
                YmtAccount ymtDataBuy = ymtMap.get(data.getTrdCfmBuy().getInvAcctId());
                YmtAccount ymtDataSell = ymtMap.get(data.getTrdCfmSell().getInvAcctId());

                //////////  TODO
                if (instData == null || ymtDataBuy == null || ymtDataSell == null) {
                    // log.warn("该OC信息维度注入失败！-->[{}]", mtpOrdcnmf.toString());  TODO 现在日志太多了，先关闭
                    // TODO 硬代码做数据
                    data.setSellIsin(String.valueOf(data.getTrdCfmBuy().getIsix()));
                    data.setSellInstrumentId(String.valueOf(data.getTrdCfmBuy().getIsix()));
                    data.setSellYmtAccountId("6" + ((data.getTrdCfmSell().getInvAcctId().length() > 2) ? data.getTrdCfmSell().getInvAcctId().substring(2) : "66"));
                    data.setSellYmtAccountName("ymtNameS");

                    data.setBuyIsin(String.valueOf(data.getTrdCfmBuy().getIsix()));
                    data.setBuyInstrumentId(String.valueOf(data.getTrdCfmBuy().getIsix()));
                    data.setBuyYmtAccountId("6" + ((data.getTrdCfmBuy().getInvAcctId().length() > 2) ? data.getTrdCfmBuy().getInvAcctId().substring(2) : "66"));
                    data.setBuyYmtAccountName("ymtNameB");
                } else {
                    data.setSellIsin(instData.getIsin());
                    data.setSellInstrumentId(instData.getInstrumentId());
                    data.setSellYmtAccountId(ymtDataSell.getYmtAccountId());
                    data.setSellYmtAccountName(ymtDataSell.getYmtAccountName());
                    data.setBuyIsin(instData.getIsin());
                    data.setBuyInstrumentId(instData.getInstrumentId());
                    data.setBuyYmtAccountId(ymtDataBuy.getYmtAccountId());
                    data.setBuyYmtAccountName(ymtDataBuy.getYmtAccountName());
                }
                return en;
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        if (!datas.isEmpty())
            log.info("monitor-排序和维度注入模块->{}线程本次给{}条数据进行了维度注入，耗时{}秒", Thread.currentThread().getName(), datas.size(), (System.currentTimeMillis() - startDimeTime) / 1000.0);
        return filledData;
    }

//    /**
//     * 不同主题的数据调用对应的方法
//     *
//     * @param ed 需要注入维度的 ed数据
//     */
//    public static long injectDimension(TopicData ed) {
//        if (ed instanceof MtpOrdcnmf) {
//            return DimensionDataHelper.putMtpOcDimension(ed);
//        } else if (ed instanceof MtpTrdcnmf) {
//            return DimensionDataHelper.putMtpTcDimension(ed);
//        }
////        else if (ed instanceof MtpPn) {
////            DimensionDataHelper.putMtpPnDimension(ed);
////        } else if (ed instanceof MtpNc) {
////            DimensionDataHelper.putMtpNcDimension(ed);
////        }
//        else if (ed instanceof AtpTt) {
//            return DimensionDataHelper.putAtpTtDimension(ed);
//        } else if (ed instanceof AtpTo) {
//            return DimensionDataHelper.putAtpToDimension(ed);
//        }
//        log.warn("线程{}维度注入时数据类型异常->{}", Thread.currentThread().getName(), ed.toString());
//        return 0L;
//    }
//
//
//    /**
//     * 维度注入 MtpOrdcnmf 对象
//     *
//     * @param ed
//     */
//    public static long putMtpOcDimension(TopicData ed) {
//        MtpOrdcnmf mtpOrdcnmf = (MtpOrdcnmf) ed;
//
//        int isix = mtpOrdcnmf.getIsix();
//        if (isix == 0) {
//            return -1L; // isix==0  ->  eos信息
//        }
//        String invAcctId = mtpOrdcnmf.getInvAcctId();
//
//        Instrument instrument = QueryDimensionData.queryInstrumentByIsix(isix);
//        YmtAccount YmtAccount = QueryDimensionData.queryYmtAccountByInvActId(invAcctId);
//
//        if (instrument == null || YmtAccount == null) {
////            log.warn("该OC信息维度注入失败！-->[{}]", mtpOrdcnmf.toString());  TODO 现在日志太多了，先关闭
//            // TODO 硬代码做数据
//            mtpOrdcnmf.setIsin(String.valueOf(isix));
//            mtpOrdcnmf.setInstrumentId(String.valueOf(isix));
//            mtpOrdcnmf.setYmtAccountId("6" + ((invAcctId.length() > 2) ? invAcctId.substring(2) : "66"));
//            mtpOrdcnmf.setYmtAccountName("ymtName");
//            return mtpOrdcnmf.getActnSeqNum();
//        }
//        mtpOrdcnmf.setIsin(instrument.getIsin());
//        mtpOrdcnmf.setInstrumentId(instrument.getInstrumentId());
//        mtpOrdcnmf.setYmtAccountId(YmtAccount.getYmtAccountId());
//        mtpOrdcnmf.setYmtAccountName(YmtAccount.getYmtAccountName());
//        return mtpOrdcnmf.getActnSeqNum();
//    }
//
//    /**
//     * 维度注入 MtpTrdcnmf 对象
//     *
//     * @param ed
//     */
//    public static long putMtpTcDimension(TopicData ed) {
//        MtpTrdcnmf mtpTrdcnmf = (MtpTrdcnmf) ed;
//
//        int isix = mtpTrdcnmf.getTrdCfmBuy().getIsix();
//        Instrument instrument = QueryDimensionData.queryInstrumentByIsix(isix);
//
//        // 买方维度
//        String sellInvAcctId = mtpTrdcnmf.getTrdCfmSell().getInvAcctId();
//        YmtAccount YmtAccountSell = QueryDimensionData.queryYmtAccountByInvActId(sellInvAcctId);
//
//        // 卖方维度
//        String buyInvAcctId = mtpTrdcnmf.getTrdCfmBuy().getInvAcctId();
//        YmtAccount YmtAccountBuy = QueryDimensionData.queryYmtAccountByInvActId(buyInvAcctId);
//
//        if (instrument == null || YmtAccountSell == null || YmtAccountBuy == null) {
////            log.warn("该TC信息维度注入失败！-->[{}]", mtpTrdcnmf.toString());  TODO 现在日志太多了，先关闭
//
//
//            // TODO 硬代码做数据
//            mtpTrdcnmf.setSellIsin(String.valueOf(isix));
//            mtpTrdcnmf.setSellInstrumentId(String.valueOf(isix));
//            mtpTrdcnmf.setSellYmtAccountId("6" + ((sellInvAcctId.length() > 2) ? sellInvAcctId.substring(2) : "66"));
//            mtpTrdcnmf.setSellYmtAccountName("ymtNameS");
//
//            mtpTrdcnmf.setBuyIsin(String.valueOf(isix));
//            mtpTrdcnmf.setBuyInstrumentId(String.valueOf(isix));
//            mtpTrdcnmf.setBuyYmtAccountId("6" + ((buyInvAcctId.length() > 2) ? sellInvAcctId.substring(2) : "66"));
//            mtpTrdcnmf.setBuyYmtAccountName("ymtNameB");
//
//            return Math.min(mtpTrdcnmf.getTrdCfmBuy().getActnSeqNum(), mtpTrdcnmf.getTrdCfmSell().getActnSeqNum());
//        }
//
//        mtpTrdcnmf.setSellIsin(instrument.getIsin());
//        mtpTrdcnmf.setSellInstrumentId(instrument.getInstrumentId());
//        mtpTrdcnmf.setSellYmtAccountId(YmtAccountSell.getYmtAccountId());
//        mtpTrdcnmf.setSellYmtAccountName(YmtAccountSell.getYmtAccountName());
//        mtpTrdcnmf.setBuyIsin(instrument.getIsin());
//        mtpTrdcnmf.setBuyInstrumentId(instrument.getInstrumentId());
//        mtpTrdcnmf.setBuyYmtAccountId(YmtAccountBuy.getYmtAccountId());
//        mtpTrdcnmf.setBuyYmtAccountName(YmtAccountBuy.getYmtAccountName());
//        return Math.min(mtpTrdcnmf.getTrdCfmBuy().getActnSeqNum(), mtpTrdcnmf.getTrdCfmSell().getActnSeqNum());
//    }
//
//    /**
//     * 维度注入 AtpTo 对象
//     *
//     * @param ed
//     */
//    public static long putAtpToDimension(TopicData ed) {
//
//        AtpTo atpTo = (AtpTo) ed;
//        String instId = atpTo.getInstId();
//        String invAcctId = atpTo.getInvAcctId();
//
//        //补全国际证券代码
//        atpTo.setIsinCod(QueryDimensionData.queryIsinCodeByInstId(String.valueOf(instId)));
//
//        if (!StringUtils.isEmpty(invAcctId)) {
//            atpTo.setYmtAccountId(QueryDimensionData.queryYmtCodeByInvActId(invAcctId));
//            atpTo.setYmtAccountName(QueryDimensionData.queryYmtNameByInvActId(invAcctId));
//        }
//
//        return atpTo.getActionSeqNum();
//
//    }
//
//
//    /**
//     * 维度注入 AtpTt 对象
//     *
//     * @param ed
//     */
//    public static long putAtpTtDimension(TopicData ed) {
//
//        AtpTt atpTt = (AtpTt) ed;
//
//        String buyInvAcctId = atpTt.getBuyInvAcctId();
//        atpTt.setBuyYmtAccountId(QueryDimensionData.queryYmtCodeByInvActId(buyInvAcctId));
//        atpTt.setBuyYmtAccountName(QueryDimensionData.queryYmtNameByInvActId(buyInvAcctId));
//
//        String sellInvAcctId = atpTt.getSellInvAcctId();
//        atpTt.setSellYmtAccountId(QueryDimensionData.queryYmtCodeByInvActId(sellInvAcctId));
//        atpTt.setSellYmtAccountName(QueryDimensionData.queryYmtNameByInvActId(sellInvAcctId));
//
//        String instId = atpTt.getInstId();
//        //补全国际证券代码
//        atpTt.setIsinCod(QueryDimensionData.queryIsinCodeByInstId(String.valueOf(instId)));
//
//        return Math.min(atpTt.getBuyActionSeqNum(), atpTt.getSellActionSeqNum());
//    }
//
//    /**
//     * 维度注入 MtpNc 对象
//     *
//     * @param ed
//     */
//    public static void putMtpNcDimension(TopicData ed) {
//
//        MtpNc mtpNc = (MtpNc) ed;
//
//        String invAcctId = mtpNc.getInvAcctId();
//        mtpNc.setYmtAccountId(QueryDimensionData.queryYmtCodeByInvActId(invAcctId));
//        mtpNc.setYmtAccountName(QueryDimensionData.queryYmtNameByInvActId(invAcctId));
//
//
//        int isix = mtpNc.getIsix();
//        mtpNc.setInstrumentId(QueryDimensionData.querySecCodeByIsix(String.valueOf(isix).trim()));
//        mtpNc.setIsin(QueryDimensionData.queryIsinCodeByIsix(String.valueOf(isix).trim()));
//
//
//    }
//
//    /**
//     * 维度注入 MtpPn 对象
//     *
//     * @param ed
//     */
//    public static void putMtpPnDimension(TopicData ed) {
//        MtpPn mtpPn = (MtpPn) ed;
//        int isix = mtpPn.getIsix();
//        mtpPn.setIsin(QueryDimensionData.queryIsinCodeByIsix(String.valueOf(isix).trim()));
//        mtpPn.setInstrumentId(QueryDimensionData.querySecCodeByIsix(String.valueOf(isix).trim()));
//
//    }

}
