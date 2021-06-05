package com.sse.rcp.dao;

import com.sse.rcp.domains.ezei.atp.AtpTo;
import com.sse.rcp.domains.ezei.atp.AtpTt;
import com.sse.rcp.domains.order.*;

import java.util.List;

public interface StorageDao {
    // OrderBookRecord
    int insertOrdBook(OrderBookRecord orderBookRecord ) ;
    // OrderDetail
    int insertOrderDetail(OrderDetail orderDetail) ;
    // OrderWithdraw
    int insertOrderWithdraw(OrderWithdraw orderWithdraw);
    // TradeDetail
    int insertTradeDetail(TradeDetail tradeDetail) ;

    // BatchUpdateOrderBookRecord
    int[] batchInsertOrdBook(List<OrderBookRecord> orderBookRecords) ;
    // BatchUpdateOrderDetail
    int[] batchInsertOrderDetail(List<OrderDetail> orderDetails) ;
    // BatchUpdateOrderWithdraw
    int[] batchInsertOrderWithdraw(List<OrderWithdraw> orderWithdraws);
    // BatchUpdateTradeDetail
    int[] batchInsertTradeDetail(List<TradeDetail> tradeDetails) ;
    //批量插入竞价持仓数据
    int[] batchInsertPositionData(List<PositionDetail> positionDetails) ;
    //竞价持仓数据更新
    int updatePositionData();
    //清除竞价持仓表中临时数据
    int truncateTempPositionData();
    //综业盘后固价申报表
    int[] batchInsertPfpOrderDetail(List<AtpTo> ocDataTrads_pfp);
    //综业盘后固价撤单表
    int[] batchInsertPfpOrderWithCancel(List<AtpTo> ocDataCancels_pfp);
    //综业盘后固价成交表
    int[] batchInsertPfpTradeDetail(List<AtpTt> tcDatas_pfp);
}
