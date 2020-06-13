package com.njupt.gmall.manager.mapper;

import com.njupt.gmall.bean.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-22 19:10
 */
public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {

    //查询当前sku的spu的其他sku的集合的 hash表
    //以便当页面选择其他销售属性的时候可以直接定位到具体的某一个sku_id
    //然后再根据当前的sku_id号，去数据库中取出对应的具体信息
    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(String productId);

}
