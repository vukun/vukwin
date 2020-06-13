package com.njupt.gmall.service;

import com.njupt.gmall.bean.PmsProductImage;
import com.njupt.gmall.bean.PmsProductInfo;
import com.njupt.gmall.bean.PmsProductSaleAttr;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-21 13:59
 */
public interface PmsProductService {

    List<PmsProductInfo> getSpuList(String catalog3Id);

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId);

    List<PmsProductImage> getSpuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId);

}
