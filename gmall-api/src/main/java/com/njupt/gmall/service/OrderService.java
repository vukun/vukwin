package com.njupt.gmall.service;

import com.njupt.gmall.bean.OmsOrder;

/**
 * @author zhaokun
 * @create 2020-06-10 9:38
 */
public interface OrderService {

    String genTradeCode(String memberId);

    String checkTradeCode(String memberId, String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);
}
