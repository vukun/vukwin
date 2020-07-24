package com.njupt.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.njupt.gmall.bean.UmsMember;
import com.njupt.gmall.bean.UmsMemberReceiveAddress;
import com.njupt.gmall.service.UserService;
import com.njupt.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.njupt.gmall.user.mapper.UserMapper;
import com.njupt.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-11 20:36
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;


    /**
     * 登录页面,验证用户输入的账户和密码是否正确
     * 为了缓解高并发的压力，网站会先把用户信息放入缓存中，方便快速查询到
     * 所以需要先从缓存中取数据,如果缓存中没有，就去mysql去取
     * @param umsMember
     * @return
     */
    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            //如果jedis不等于空，说明可以从缓存中取数据
            if(jedis != null){
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + umsMember.getUsername() + ":info");
                if(StringUtils.isNotBlank(umsMemberStr)){
                    //说明缓存中有用户数据信息
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //否则jedis为空，连接redis失败，可能redis宕机等，就需要从数据库DB中取数据
            //或者要么是密码错误，要么是缓存中没有用户信息数据,如果是缓存中没有数据，需要从DB中获取
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if(umsMemberFromDb != null){
                jedis.setex("user:" + umsMember.getPassword() + umsMember.getUsername() + ":info", 60*60*24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;
        }finally {
            jedis.close();
        }
    }

    /**
     * 将生成的token放入到redis中
     * @param token
     * @param memberId
     */
    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + memberId + ":token", 60*60*5, token);
        jedis.close();
    }

    /**
     * 把其他网站的用户登录的信息放入到数据库中
     * @param umsMember
     */
    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
        UmsMember umsMember = userMapper.selectOne(umsCheck);
        return umsMember;
    }

    /**
     * //根据用户的memberId查询用户的收货地址
     * @param memberId
     * @return
     */
    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }

    @Override
    public void register(UmsMember umsMember) {
        umsMember.setMemberLevelId("1");
        umsMember.setCreateTime(new Date());
        umsMember.setSourceType("1");
        umsMember.setIntegration(0);
        umsMember.setGrowth(1);
        umsMember.setHistoryIntegration(0);
        umsMember.setNickname(umsMember.getUsername());
        userMapper.insertSelective(umsMember);
    }

    @Override
    public String checkUsername(String username) {
        int count = userMapper.checkUsername(username);
        if(count == 0){
            return "SUCCESS";
        }else{
            return "FAIL";
        }
    }

    @Override
    public String checkPhone(String phone) {
        int count = userMapper.checkPhone(phone);
        if(count == 0){
            return "SUCCESS";
        }else{
            return "FAIL";
        }
    }

    @Override
    public void addAddress(UmsMemberReceiveAddress umsMemberReceiveAddress) {
        umsMemberReceiveAddress.setDefaultStatus(0);
        umsMemberReceiveAddressMapper.insertSelective(umsMemberReceiveAddress);
    }

    /**
     * 退出登录
     * @param nickName
     */
    @Override
    public void exit(String nickName) {
        UmsMember umsMember = new UmsMember();
        umsMember.setNickname(nickName);
        UmsMember umsMember1 = userMapper.selectOne(umsMember);
        String password = umsMember1.getPassword();
        if(StringUtils.isNotBlank(password)){
            Jedis jedis = redisUtil.getJedis();
            String key = "user:" + password + nickName + ":info";
            if(StringUtils.isNoneBlank(jedis.get(key))){
                jedis.del(key);
            }
        }
    }


    /**
     * 缓存中如果没有，就需要从数据库中取数据
     * @param umsMember
     * @return
     */
    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if(umsMembers.size() != 0){
            return umsMembers.get(0);
        }
        return null;
    }
}
