package com.njupt.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.njupt.gmall.annotations.LoginRequired;
import com.njupt.gmall.bean.PmsProductSaleAttr;
import com.njupt.gmall.bean.PmsSkuInfo;
import com.njupt.gmall.bean.PmsSkuSaleAttrValue;
import com.njupt.gmall.service.PmsProductService;
import com.njupt.gmall.service.PmsSkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-23 0:33
 */
@Controller
public class ItemController {

    @Reference
    PmsSkuService pmsSkuService;
    @Reference
    PmsProductService pmsProductService;

    /**
     * 根据skuId查询具体的商品详情消息封装到PmsSkuInfo对象中
     * 渲染到item页面上
     * @param skuId
     * @param modelMap
     * @return
     */
    @RequestMapping("{skuId}.html")
    @LoginRequired(loginSuccess = false)
    public String getSkuInfo(@PathVariable String skuId, ModelMap modelMap){
        //查询商品详细信息
        PmsSkuInfo pmsSkuInfo = pmsSkuService.getSkuInfo(skuId);
        modelMap.put("skuInfo", pmsSkuInfo);

        //根据商品的productId和skuId查询商品销售属性列表.  难点难点难点
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        //查询当前sku的spu的其他sku的集合的 hash表
        //以便当页面选择其他销售属性的时候可以直接定位到具体的某一个sku_id
        //然后再根据当前的sku_id号，去数据库中取出对应的具体信息
        HashMap<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            String v = skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";
            }
            skuSaleAttrHash.put(k, v);
        }

        //将sku的销售属性hash表放到页面
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashJsonStr", skuSaleAttrHashJsonStr);

        return "item";
    }
}
