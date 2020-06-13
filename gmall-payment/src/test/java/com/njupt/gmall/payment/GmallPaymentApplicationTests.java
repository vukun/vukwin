package com.njupt.gmall.payment;

import com.njupt.gmall.mq.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Test
    public void contextLoads(){
        ConnectionFactory connectionFactory = null;
        try {
            connectionFactory = activeMQUtil.getConnectionFactory();
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testQueue = session.createQueue("speaking");

            Topic topic = session.createTopic("HaHa,The weather is pretty good!");

            MessageProducer producer = session.createProducer(topic);
            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("哈哈，今天天气真好！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();
        }catch (JMSException e){
            e.printStackTrace();
        }
    }
}
