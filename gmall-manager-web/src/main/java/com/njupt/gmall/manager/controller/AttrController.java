package com.njupt.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.bean.PmsBaseAttrInfo;
import com.njupt.gmall.bean.PmsBaseAttrValue;
import com.njupt.gmall.bean.PmsBaseSaleAttr;
import com.njupt.gmall.service.PmsAttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-21 13:24
 */
@Controller
@CrossOrigin
public class AttrController {

    @Reference
    PmsAttrService pmsAttrService;

    /**
     * 根据三级目录查询下属的属性名称列表
     * @param catalog3Id
     * @return
     */
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> getAttrInfoListByCatalog3Id(String catalog3Id){
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsAttrService.getAttrInfoListByCatalog3Id(catalog3Id);
        return pmsBaseAttrInfos;
    }

    /**
     * 当我点击"修改"的时候，需要先根据属性id查询属性值列表
     * @param attrId
     * @return
     */
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueListByAttrId(String attrId){
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsAttrService.getAttrValueListByAttrId(attrId);
        return pmsBaseAttrValues;
    }

    /**
     * 在平台属性中，添加属性名称和属性值或者修平台属性名称和属性值
     * 两者不同的是：添加属性名称和属性值的时候，是还没生成属性值id的
     *             修改属性名称和属性值的时候是在原有基础上修改的，此时已经产生了属性id
     *所以service层可以先判断参数中有没有属性值再进行相应的操作
     * @param pmsBaseAttrInfo
     * @return
     */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        String result = pmsAttrService.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    /**
     * 用来获取商家商品属性值(是后台商家录入自己仓库的商品众多属性值，以便于用户在购买页面可以看到)
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> getBaseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = pmsAttrService.getBaseSaleAttrList();
        return pmsBaseSaleAttrs;
    }


}
