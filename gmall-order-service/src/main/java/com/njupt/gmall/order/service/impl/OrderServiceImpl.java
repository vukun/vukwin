package com.njupt.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.njupt.gmall.bean.OmsOrder;
import com.njupt.gmall.bean.OmsOrderItem;
import com.njupt.gmall.mq.ActiveMQUtil;
import com.njupt.gmall.order.mapper.OmsOrderItemMapper;
import com.njupt.gmall.order.mapper.OmsOrderMapper;
import com.njupt.gmall.service.CartService;
import com.njupt.gmall.service.OrderService;
import com.njupt.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author zhaokun
 * @create 2020-06-10 9:39
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    RedisUtil redisUtil;
    @Reference
    CartService cartService;
    @Autowired
    ActiveMQUtil activeMQUtil;

    /**
     * 在”购物车详情“页面根据memberId生成交易码，用于提交订单时候的校验
     * @param memberId
     * @return
     */
    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
            String tradeCode = UUID.randomUUID().toString();
            jedis.setex(tradeKey, 60*15, tradeCode);
            return tradeCode;
        }finally {
            jedis.close();
        }
    }

    /**
     * 在提交订单时，校验交易码，防止用户重复性提交订单
     * @param memberId
     * @return
     */
    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
//            String tradeCodeFromCache = jedis.get(tradeKey);
            //使用lua脚本，在发现key的同时将key删除，防止并发订单攻击
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));

            if (eval!=null&&eval!=0) {
                // jedis.del(tradeKey);
                return "success";
            } else {
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }


    @Override
    public void saveOrder(OmsOrder omsOrder) {
        // 保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        // 保存订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            // 删除购物车数据
            cartService.delCart(omsOrderItem.getProductSkuId());
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    /**
     * 在支付完成后更新订单的相关信息，并且发送MQ给库存服务
     * @param omsOrder
     */
    @Override
    public void updateOrder(OmsOrder omsOrder) {
        //将订单状态，0待付款-->1待发货
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());
        OmsOrder omsOrderUpdate = new OmsOrder();
        omsOrderUpdate.setStatus("1");
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
            //更新订单的支付状态的信息
            omsOrderMapper.updateByExampleSelective(omsOrderUpdate, example);
            //更新支付状态信息后，引起的系统服务是：订单服务的更新-->MQ-->库存服务
            //调用MQ发送库存服务的更新信息
            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);
            TextMessage textMessage = new ActiveMQTextMessage();//基于hash结构的k/v消息
            //查询订单的对象转化成json字符串存入ORDER_PAY_QUEUE的消息队列中，供库存服务消费
            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResult = omsOrderMapper.selectOne(omsOrderParam);
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrderParam.getOrderSn());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);
            omsOrderResult.setOmsOrderItems(omsOrderItems);
            textMessage.setText(JSON.toJSONString(omsOrderResult));
            producer.send(textMessage);
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

    @Override
    public List<OmsOrder> getMyOrderListByMemberId(String memberId) {
        List<OmsOrder> omsOrders = omsOrderMapper.getMyOrderListByMemberId(memberId);
        if(omsOrders.size() > 0){
            for (OmsOrder omsOrder : omsOrders) {
                List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.getMyOrderItemListByOrderSn(omsOrder.getOrderSn());
                omsOrder.setOmsOrderItems(omsOrderItems);
                if(omsOrder.getStatus().equals("0")){
                    omsOrder.setStatus("待付款");
                }
                if(omsOrder.getStatus().equals("1")){
                    omsOrder.setStatus("待发货");
                }
                if(omsOrder.getStatus().equals("2")){
                    omsOrder.setStatus("已发货");
                }
                if(omsOrder.getStatus().equals("3")){
                    omsOrder.setStatus("已完成");
                }
                if(omsOrder.getStatus().equals("4")){
                    omsOrder.setStatus("已关闭");
                }
                if(omsOrder.getStatus().equals("5")){
                    omsOrder.setStatus("无效订单");
                }
                SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
                String format = formatter.format(omsOrder.getCreateTime());
                omsOrder.setCreatetime(format);
            }
        }
        return omsOrders;
    }

}
