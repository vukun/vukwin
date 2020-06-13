package com.njupt.gmall.manager.mapper;

import com.njupt.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-21 13:53
 */
public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {

    //根据商品的productId和skuId查询商品销售属性列表
    List<PmsProductSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("productId") String productId,@Param("skuId") String skuId);
}
