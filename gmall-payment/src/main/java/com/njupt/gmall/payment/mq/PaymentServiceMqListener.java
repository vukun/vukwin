package com.njupt.gmall.payment.mq;

import com.njupt.gmall.bean.PaymentInfo;
import com.njupt.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

/**
 * @author zhaokun
 * @create 2020-06-12 17:47
 */
//创建一个监听器，监听PAYMENT_CHECK_QUEUE消息队列中的消息
@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    //利用注解的方式，整合的mq。注解的第一个参数是：要监听的消息队列，第二个参数是：消息监听器连接工厂
    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentCheakResult(MapMessage mapMessage) throws JMSException {

        String out_trade_no = mapMessage.getString("out_trade_no");
        Integer count = 0;
        if(mapMessage.getString("count") != null){
            count = Integer.parseInt(""+mapMessage.getString("count"));
        }
        //调用paymentService的方法去查询支付宝支付结果的查询接口alipay.trade.query
        System.out.println("进行延迟检查，调用支付检查的借口服务");
        Map<String, Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);
        //判断返回的结果是否为空
        //不为空的时候才进行下一步的操作
        if(resultMap != null && !resultMap.isEmpty()){
            //得到返回结果中的支付状态
            String trade_status = (String) resultMap.get("trade_status");
            //根据查询的支付状态结果，判断是否进行下一次的延迟任务还是支付成功后的更新数据和后续任务
            if(StringUtils.isNotBlank(trade_status) && trade_status.equals("TRADE_SUCCESS")){
                //支付成功，更新支付发送支付队列
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String)resultMap.get("trade_no"));// 支付宝的交易凭证号
                paymentInfo.setCallbackContent((String)resultMap.get(("call_back_content")));//回调请求字符串
                paymentInfo.setCallbackTime(new Date());
                System.out.println("已经支付成功，调用支付服务，修改支付信息和发送支付成功的队列");
                paymentService.updatePayment(paymentInfo);
                return;
            }
            if(count > 0){
                //否则继续发送延迟检查任务，计算延迟时间等
                System.out.println("没有支付成功，检查剩余次数为"+count+",继续发送延迟检查任务");
                count--;
                paymentService.sendDelayPaymentResultCheck(out_trade_no, count);
            }else{
                System.out.println("检查剩余次数用尽，结束检查");
            }
        }

    }
}
