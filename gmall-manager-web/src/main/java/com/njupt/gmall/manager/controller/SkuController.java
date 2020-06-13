package com.njupt.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.bean.PmsSkuInfo;
import com.njupt.gmall.service.PmsSkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhaokun
 * @create 2020-05-22 19:06
 */
@Controller
@CrossOrigin
public class SkuController {

    @Reference
    PmsSkuService pmsSkuService;

    /**
     * 保存商品属性和商品属性值以及商品的图片信息
     * @param pmsSkuInfo
     * @return
     */
    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        //如果前端没有设置图片默认值，后端默认设置第一张为默认值
        if(StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
        String result = pmsSkuService.saveSkuInfo(pmsSkuInfo);
        return "success";
    }

}
