package com.njupt.gmall.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author zhaokun
 * @create 2020-05-26 2:03
 */
public class RedisUtil {
    private JedisPool jedisPool;
    //初始化一个连接池
    public void initPool(String host,int port ,int database){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //最大连接数
        poolConfig.setMaxTotal(200);
        //最大空闲连接数
        poolConfig.setMaxIdle(50);
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        poolConfig.setBlockWhenExhausted(true);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间）
        poolConfig.setMaxWaitMillis(10*1000);
        //在获取连接的时候检查有效性
        poolConfig.setTestOnBorrow(true);
        jedisPool=new JedisPool(poolConfig,host,port,20*1000);
    }
    //返回一个连接给调用者
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
