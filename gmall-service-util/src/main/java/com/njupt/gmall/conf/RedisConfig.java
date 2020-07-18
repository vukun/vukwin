package com.njupt.gmall.conf;

import com.njupt.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhaokun
 * @create 2020-05-26 2:24
 */
//用于标注一个配置类，让spring在启动时，把redis的连接池初始化到spring容器中，方便后续直接获取连接使用
@Configuration
public class RedisConfig {
    //读取配置文件中的redis的ip地址
    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:0}")
    private int port;
    @Value("${spring.redis.database:0}")
    private int database;
    @Bean //可以让spring容器在启动的时候去把该方法返回的一个redisUtil类初始化到spring容器中
    public RedisUtil getRedisUtil(){
        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil=new RedisUtil();
        redisUtil.initPool(host,port,database);
        return redisUtil;
    }
}
