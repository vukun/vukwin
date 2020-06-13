package com.njupt.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.njupt.gmall.bean.PmsSkuAttrValue;
import com.njupt.gmall.bean.PmsSkuImage;
import com.njupt.gmall.bean.PmsSkuInfo;
import com.njupt.gmall.bean.PmsSkuSaleAttrValue;
import com.njupt.gmall.manager.mapper.PmsSkuAttrValueMapper;
import com.njupt.gmall.manager.mapper.PmsSkuImageMapper;
import com.njupt.gmall.manager.mapper.PmsSkuInfoMapper;
import com.njupt.gmall.manager.mapper.PmsSkuSaleAttrValueMapper;
import com.njupt.gmall.service.PmsSkuService;
import com.njupt.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * @author zhaokun
 * @create 2020-05-22 19:14
 */
@Service
public class PmsSkuServiceImpl implements PmsSkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 保存商品属性和商品属性值以及商品的图片信息
     * @param pmsSkuInfo
     * @return
     */
    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //插入sku信息,并且根据主键返回策略得到当前插入的主键id -> pmsSkuInfoId
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String pmsSkuInfoId = pmsSkuInfo.getId();

        //插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfoId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //插入平台属性关联的属性值
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfoId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        //插入平台属性关联的图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(pmsSkuInfoId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        return "success";
    }

    /**
     * 根据商品的skuId从数据库中查询商品详细信息 ->商品的详情页
     * @param skuId
     * @return
     */

    public PmsSkuInfo getSkuInfoFromDb(String skuId) {
        //获取sku商品信息
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //将该sku商品的图片信息也获取到并且封装到skuInfo中
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    /**
     * 根据商品的skuId查询商品详细信息 ->商品的详情页
     * @param skuId
     * @return
     */
    @Override
    public PmsSkuInfo getSkuInfo(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        Jedis jedis = redisUtil.getJedis();
        String skuKey = "sku:" + skuId + ":info";
        //根据skuKey从缓存中拿数据
        String skuJson = jedis.get(skuKey);
        //判断从缓存中拿到的数据存不存在
        if(StringUtils.isNotBlank(skuJson)){
            //若存在，直接把从缓存中拿到的数据转成对应的对象数据返回即可
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        }else{
            //如果缓存中没有，此时可能产生了缓存击穿或者缓存穿透第一阶段的缓存崩掉的情况，此时大量请求要去访问数据库
            //所以我们要在访问数据库之前加上锁，防止缓存击穿的情况发生

            //我们首先要设置分布式锁，并加上过期时间，就是为了防止该线程如果临时死掉，锁不会被释放掉，造成请求阻塞
            //同时为了确保让jedis删掉自己的锁，我们可以生成一个全局唯一 token值，作为value值存入，在删除的时候加个判断即可
            String uniqeToken = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", uniqeToken, "nx", "px", 10*1000);
            //如果返回的结果是“OK”，说明上锁成功
            if(StringUtils.isNotBlank(OK) && OK.equals("OK")){
                //上锁成功后，然后再去访问数据库
                pmsSkuInfo = getSkuInfoFromDb(skuId);
                //判断数据库中是否存在该数据
                if(pmsSkuInfo != null){
                    //若存在，就把数据放入到缓存中
                    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                }else{
                    //若不存在，就在缓存中放入该key，并把该对应的 v设置为null,并利用setex()方法设置过期时间
                    jedis.setex("sku:" + skuId + ":info",60 * 3, JSON.toJSONString(""));
                }
                //在访问 mysql后，需要释放掉锁,方便后续请求能获取到锁进行访问
                //先判断是不是自己的锁再去删除
                String resultToken = jedis.get("sku:" + skuId + ":lock");
                //当取出的token是自己的 token时候，再去删除锁，为了避免删除别人的锁
                if(StringUtils.isNotBlank(resultToken) && resultToken.equals(uniqeToken)){
                    jedis.del("sku" + skuId + ":lock");
                }
            }else{
                //代表上锁失败，让该jedis自旋一段时间再重新去访问该方法
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //再重新去访问该方法,注意 return不能丢
                return getSkuInfo(skuId);
            }
        }
        jedis.close();
        return pmsSkuInfo;
    }

    /**
     * 查询当前sku的spu的其他sku的集合的 hash表
     * 以便当页面选择其他销售属性的时候可以直接定位到具体的某一个sku_id
     * 然后再根据当前的sku_id号，去数据库中取出对应的具体信息
     * @param productId
     * @return
     */
    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    /**
     * 目的：查出数据导入到 es中
     * 根据catalog3Id查询所有的数据
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {

        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        BigDecimal price = pmsSkuInfo1.getPrice();

        if(price.compareTo(productPrice)==0){
            b = true;
        }

        return b;
    }

}
