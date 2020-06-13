package com.njupt.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.annotations.LoginRequired;
import com.njupt.gmall.bean.*;
import com.njupt.gmall.service.PmsAttrService;
import com.njupt.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * @author zhaokun
 * @create 2020-05-30 12:50
 */
@Controller
public class SearchController {

    @Reference
    SearchService searchService;
    @Reference
    PmsAttrService pmsAttrService;


    //展示首页的信息
    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){
        return "index";
    }

    /**
     *  可以根据关键字、catalog3Id进行elasticSearch搜索查询数据
     *  单独创建一个PmsSearchParam实体类Bean，
     *  用来封装接收来自前端es查询的参数进行一系列的后台业务处理
     *  参数主要有：{catalog3Id、keyword、skuAttrValueList(平台属性值列表)}
     *  也就是前端可以根据catalog3Id进行搜索、可以根据keyword进行搜索、可以根据skuAttrValueList进行搜索
     * @param pmsSearchParam
     * @return
     */
    @RequestMapping("list")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){

        //第一步：调用搜索服务，返回整体搜索结果，并将搜索结果共同所包含的属性列表展示出来，目前还未进行第二步的搜索
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);
        //抽取检索结果包含的平台属性集合
        Set<String> valueSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueSet.add(valueId);
            }
        }
        //根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsAttrService.getAttrValueListByValueId(valueSet);
        modelMap.put("attrList", pmsBaseAttrInfos);


        // 第二步，当我们第二次进行平台属性搜索时候，此时需要将Url参数地址封装好，
        // 并且需要对平台属性集合进一步处理：
        //       当我们点击任一属性的时候，页面展示的时候需要去掉选中的当前条件中valueId所在的属性组的那一行
        //       而且相应的要生成一个面包屑，
        //所以删除平台属性组和生成面包屑可以合在一起写
        String[] delValueIds = pmsSearchParam.getValueId();
        //需要对平台属性列表判断，可能还没选择平台属性，此时列表集合为空，如果不加上非空判断会报异常
        if (delValueIds != null){
            // 面包屑
            // pmsSearchParam
            // delValueIds
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String delValueId : delValueIds){//平台属性集合有多少个就要删除多少个平台属性组和生成多少个面包屑，所以要在这放置循环
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();//平台属性集合
                //先生成一个面包屑对象，用来封装面包屑的参数
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                // 把面包屑的参数(delValueId)放到面包屑对象中
                pmsSearchCrumb.setValueId(delValueId);
                //重新生成面包屑的请求Url地址参数方法：getUrlParamForCrumb(pmsSearchParam, delValueId)
                //并把它放入到面包屑对象中(urlParam),此时面包屑对象还缺一个参数：属性值名称。
                // 而目前只有属性Id,属性名称只有在遍历属性值列表的时候才能匹配到，所需要把封装属性名称写在iterator的循环中
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam, delValueId));
                //对属性值列表进行遍历
                while (iterator.hasNext()){
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)){
                            // 当遍历到面包屑的属性值名称时，把他封装到面包屑对象中
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //并且删除该属性值所在的属性组   --->到现在删除平台属性的属性组和生成面包屑对象都已经完成了。
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }

        //因为前端传的是PmsSearchParam，前端把参数封装成了PmsSearchParam对象，我需要把参数从对象中取出来
        //查询结果后，将结果集和组装好的String类型参数一块返回给前端，这样，前端既可以取出对应结果，
        // 当我们点击其他的平台属性时候，还可以将此时的请求Url参数地址展示到前端页面的底部，方便分享给朋友
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }

        return "list";
    }

    //因为点击面包屑后，请求的地址是 = 当前的请求参数-面包屑的参数，如果直接操作字符串参数会很麻烦
    //所以我们可以直接重新封装请求的参数，
    // 如果当前参数是面包屑参数就不封装，反之就封装
    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam, String delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        //拼接点击面包屑后的URL地址参数
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                //如果当前的参数不是面包屑的参数就拼接，如果当前参数是面包屑参数就不拼接略过当前参数
                if (!pmsSkuAttrValue.equals(delValueId)) {
                    urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                }
            }
        }
        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        //pmsSearchParam中所包含的参数，但可能为空，要进行非空判断
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";
        //参数中可能包含keyword关键字
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        //参数中可能包含catalog3Id
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        //参数中可能包含skuAttrValueList平台属性列表
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
            }
        }
        return urlParam;
    }
}