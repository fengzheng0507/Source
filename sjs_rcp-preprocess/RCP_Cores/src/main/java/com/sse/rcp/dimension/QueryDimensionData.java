package com.sse.rcp.dimension;

import com.sse.rcp.domains.dimension.Instrument;
import com.sse.rcp.domains.dimension.YmtAccount;
import com.sse.rcp.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QueryDimensionData {

    /**
     * 根据isix查询Instrument
     */
    public static Instrument queryInstrumentByIsix(int isix) {
        return (Instrument) RedisUtil.getDimensionData(String.valueOf(isix));
    }

    public static YmtAccount queryYmtAccountByInvActId(String invActId) {
        return (YmtAccount) RedisUtil.getDimensionData(invActId);
    }


    /**
     * 根据isix查询6位产品代码
     */
    public static String querySecCodeByIsix(String isix) {
        Object accept = RedisUtil.getDimensionData(isix);
        return accept == null ? null : ((Instrument) accept).getInstrumentId();
    }

    /**
     * 根据isix查询isin
     */
    public static String queryIsinCodeByIsix(String isix) {
        Object accept = RedisUtil.getDimensionData(isix);
        return accept == null ? null : ((Instrument) accept).getIsin();
    }

    /**
     * 根据【投资者账户】查询【一户通账号】
     *
     * @param invActId 投资者账户 - inverstor_account_id
     * @return 一户通账号
     */
    public static String queryYmtCodeByInvActId(String invActId) {
        Object accept = RedisUtil.getDimensionData(invActId);
        return accept == null ? null : ((YmtAccount) accept).getYmtAccountId();
    }

    /**
     * 根据【投资者账户】查询【一户通户名】
     *
     * @param invActId 投资者账户 - inverstor_account_id
     * @return 一户通户名
     */
    public static String queryYmtNameByInvActId(String invActId) {
        Object accept = RedisUtil.getDimensionData(invActId);
        return accept == null ? null : ((YmtAccount) accept).getYmtAccountName();
    }

    /**
     * 根据INSTRUMENT_ID 查询 ISINCOD
     */
    public static String queryIsinCodeByInstId(String instId) {
        Object accept = RedisUtil.getDimensionData(instId);
        return accept == null ? null : ((Instrument) accept).getIsin();
    }
}
