package com.njupt.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.njupt.gmall.bean.OmsCartItem;
import com.njupt.gmall.cart.mapper.OmsCartItemMapper;
import com.njupt.gmall.service.CartService;
import com.njupt.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaokun
 * @create 2020-06-05 14:12
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 根据memberId和skuId,去查询DB中是否存在该商品信息
     * @param memberId
     * @param skuId
     * @return
     */
    @Override
    public OmsCartItem getCartsByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    /**
     * 如果数据库中没有该商品信息，将封装好的商品信息放入到数据库中
     * @param omsCartItem
     */
    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if(StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);//避免添加空值
        }
    }

    /**
     * 如果数据库中有该商品信息，更新商品信息即可
     * @param omsCartItemFromDb
     */
    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, e);
    }

    /**
     * 最后将购物车的信息同步到缓存中
     * @param memberId
     */
    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        //同步到缓存中
        Jedis jedis = redisUtil.getJedis();
        Map<String, String> map = new HashMap<>();
        for (OmsCartItem cartItem : omsCartItems) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        jedis.del("user:" + memberId + ":cart");
        //把更新后的数据放入到对应的用户缓存中
        jedis.hmset("user:" + memberId + ":cart", map);
        jedis.close();
    }

    /**
     * 查询购物车信息，返回购物车商品列表
     * @param memberId
     * @return
     */
    @Override
    public List<OmsCartItem> cartList(String memberId) {
        //从缓存中查询购物车商品信息
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            jedis.close();
        }
        return omsCartItems;
    }

    /**
     * 根据isChecked、skuId字段用ajax异步请求刷新内嵌页面
     * @param omsCartItem
     */
    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem, e);
        //缓存同步
        flushCartCache(omsCartItem.getMemberId());
    }

    @Override
    public void delCart(String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(skuId);
        omsCartItemMapper.delete(omsCartItem);
    }

}
