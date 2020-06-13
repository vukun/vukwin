package com.njupt.gmall.seckill.controller;

import com.njupt.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-06-13 19:29
 */
@Controller
public class SeckillController {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    //用redis来实现秒杀。 随机凭运气式秒杀
    @RequestMapping("kill")
    @ResponseBody
    public String kill(){
        Jedis jedis = redisUtil.getJedis();
        //开启商品的监控
        jedis.watch("130");
        Integer stock = Integer.parseInt(jedis.get("130"));
        if(stock > 0){
            Transaction multi = jedis.multi();
            multi.incrBy("130", -1);
            List<Object> exec = multi.exec();
            if(exec != null && exec.size() > 0){
                System.out.println("当前库存剩余数量" + stock + ",某用户抢购成功，当前抢购人数：" + (1000 - stock));
                //用消息队列发出订单消息
            }else{
                System.out.println("当前库存剩余数量" + stock + ",某用户抢购失败");
            }
        }
        jedis.close();
        return "1";
    }

    //用Redission来实现秒杀。 先到先得式秒杀
    @RequestMapping("secKill")
    @ResponseBody
    public String secKill(){
        Jedis jedis = redisUtil.getJedis();

        RSemaphore semaphore = redissonClient.getSemaphore("130");
        boolean b = semaphore.tryAcquire();
        int stock = Integer.parseInt(jedis.get("130"));
        if(b){
            System.out.println("当前库存剩余数量" + stock + ",某用户抢购成功，当前抢购人数：" + (1000 - stock));
            System.out.println("发出订单的消息队列，由订单系统对当前抢购生成订单");
        }else{
            System.out.println("当前库存剩余数量" + stock + ",某用户抢购失败");
        }

        jedis.close();
        return "1";
    }

}
