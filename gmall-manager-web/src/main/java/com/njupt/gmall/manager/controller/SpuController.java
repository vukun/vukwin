package com.njupt.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.bean.PmsProductImage;
import com.njupt.gmall.bean.PmsProductInfo;
import com.njupt.gmall.bean.PmsProductSaleAttr;
import com.njupt.gmall.service.PmsProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-21 13:23
 */
@Controller
@CrossOrigin
public class SpuController {

    @Reference
    PmsProductService pmsProductService;

    /**
     * 根据catalog3Id查询商品列表
     * @param catalog3Id
     * @return
     */
    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> getSpuList(String catalog3Id){
        List<PmsProductInfo> pmsProductInfos = pmsProductService.getSpuList(catalog3Id);
        return pmsProductInfos;
    }

    /**
     * 根据商家属性添加或修改对应属性值
     * @param pmsProductInfo
     * @return
     */
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        pmsProductService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    /**
     * 将商品属性中的图片上传到图片服务器，因为图片传输是采用“file”形式传输，所以对于controller层
     * 需要用 MultipartFile类型对象接收，同时要告诉 MultipartFile，请求传来的是file类型的数据
     * 即需要在参数列表家加上@RequestParam("file")
     *
     * @param multipartFile
     * @return
     */
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file")MultipartFile multipartFile){
        //将图片或者音视频上传到分布式文件存储系统



        //将图片的存储路径返回给页面
        String imgUrl = "https://m.360buyimg.com/babel/jfs/t5137/20/1794970752/352145/d56e4e94/591417dcN4fe5ef33.jpg";
        return imgUrl;
    }

    /**
     * 获取商品属性列表
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductService.getSpuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

    /**
     * 获取商品图片列表
     * @param spuId
     * @return
     */
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> getSpuImageList(String spuId){
        List<PmsProductImage> pmsProductImageList = pmsProductService.getSpuImageList(spuId);
        return pmsProductImageList;
    }


}
