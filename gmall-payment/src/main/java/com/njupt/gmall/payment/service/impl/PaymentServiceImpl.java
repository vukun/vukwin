package com.njupt.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.njupt.gmall.bean.PaymentInfo;
import com.njupt.gmall.mq.ActiveMQUtil;
import com.njupt.gmall.payment.mapper.PaymentInfoMapper;
import com.njupt.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhaokun
 * @create 2020-06-11 10:18
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    /**
     * 支付成功后，生成并保存用户的支付信息
     * @param paymentInfo
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    /**
     * 支付宝支付成功后的同步回调的接口方法:更新支付的信息
     * @param paymentInfo
     */
    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        //进行支付更新的幂等性检查，因为我们自己定时调的结果和支付宝返回的结果会产生两次相同的对本地的调用，
        //如果不进行幂等性的操作，同样的操作可能会修改数据的信息，比如创建的时间等等
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoResult = paymentInfoMapper.selectOne(paymentInfoParam);
        //进行幂等性的检查
        if(StringUtils.isNotBlank(paymentInfoResult.getPaymentStatus()) && paymentInfoResult.getPaymentStatus().equals("已支付")){
            //如果数据库该笔订单的结果状态已经是“已支付”，直接返回，不做任何操作
            return;
        }else{
            //如果不是，说明这是第一次入库，直接更新支付状态信息即可
            String orderSn = paymentInfo.getOrderSn();
            Example example = new Example(paymentInfo.getClass());
            example.createCriteria().andEqualTo("orderSn", orderSn);
            //获得从MQ连接池获取一个MQ队列的连接，并且创建会话
            Connection connection = null;
            Session session = null;
            try {
                connection = activeMQUtil.getConnectionFactory().createConnection();
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
            } catch (JMSException e) {
                e.printStackTrace();
            }
            try{
                paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
                //支付成功后，更新支付状态信息，引起的系统服务-->订单服务的更新-->库存服务-->物流服务
                //调用MQ发送支付成功的消息
                Queue payhment_success_queue = session.createQueue("PAYHMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payhment_success_queue);
                MapMessage mapMessage = new ActiveMQMapMessage();//基于hash结构的k/v消息
                mapMessage.setString("out_trade_no", paymentInfo.getOrderSn());
                producer.send(mapMessage);
                session.commit();
            }catch (Exception e){
                //执行失败后需要回滚消息
                try {
                    session.rollback();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }finally {
                //最后关闭连接
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendDelayPaymentResultCheck(String outTradeNo, int count) {
        //获得从MQ连接池获取一个MQ队列的连接，并且创建会话
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try{
            //提交支付页面时候，制作一个MQ定时器去自动检查支付的结果，然后使得后续的业务立即执行
            //调用延迟MQ发送信息
            Queue payment_check_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_check_queue);
            MapMessage mapMessage = new ActiveMQMapMessage();//基于hash结构的k/v消息
            mapMessage.setString("out_trade_no", outTradeNo);
            mapMessage.setInt("count",count);
            //设置延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000*30);
            producer.send(mapMessage);
            session.commit();
        }catch (Exception e){
            //执行失败后需要回滚消息
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            //最后关闭连接
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 调用支付宝的查询支付的接口，在支付宝还未返回支付结果之前查询支付的结果
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {
        Map<String,Object> resultMap = new HashMap<>();
        //调用支付宝的支付结果查询接口，获得支付结果的信息
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("有可能交易已创建，调用成功");
            resultMap.put("out_trade_no",response.getOutTradeNo());
            resultMap.put("trade_no",response.getTradeNo());
            resultMap.put("trade_status",response.getTradeStatus());
            resultMap.put("call_back_content",response.getMsg());
        } else {
            System.out.println("有可能交易未创建，调用失败");

        }
        return requestMap;
    }
}
