package com.njupt.gmall.order.mq;

import com.njupt.gmall.bean.OmsOrder;
import com.njupt.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author zhaokun
 * @create 2020-06-12 8:47
 */
//去消费支付完成后，支付业务发出的消息
//是一个普通的java类，本质上是继承了spring的监听器，用于监听队列中的消息
// 所以本类也是一个监听器
@Component
public class OrderServiceMqListener {

    @Autowired
    OrderService orderService;

    //利用注解的方式，整合的mq。注解的第一个参数是：要监听的消息队列，第二个参数是：消息监听器连接工厂
    @JmsListener(destination = "PAYHMENT_SUCCESS_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {

        String out_trade_no = mapMessage.getString("out_trade_no");
        //更新订单状态
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        //消费来自PAYHMENT_SUCCESS_QUEUE消息队列MQ中的消息，在支付完成后更新订单的相关信息
        orderService.updateOrder(omsOrder);
    }

}
