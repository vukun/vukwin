package com.njupt.gmall.service;

import com.njupt.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * @author zhaokun
 * @create 2020-06-11 10:18
 */
public interface PaymentService {

    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheck(String outTradeNo, int count);

    Map<String, Object> checkAlipayPayment(String out_trade_no);
}
