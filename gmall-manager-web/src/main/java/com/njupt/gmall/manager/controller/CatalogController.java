package com.njupt.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.bean.PmsBaseCatalog1;
import com.njupt.gmall.bean.PmsBaseCatalog2;
import com.njupt.gmall.bean.PmsBaseCatalog3;
import com.njupt.gmall.service.PmsCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-20 22:07
 */
@Controller
//解决来自前端不同网域的跨域请求
@CrossOrigin
public class CatalogController {

    @Reference
    PmsCatalogService pmsCatalogService;


    /**
     * 直接去库中查询一级目录列表
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> pmsBaseCatalog1s = pmsCatalogService.getCatalog1();
        return pmsBaseCatalog1s;
    }

    /**
     * 根据一级目录的id，查询二级目录列表
     * @param catalog1Id
     * @return
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2ByCatalog1Id( String catalog1Id){
        List<PmsBaseCatalog2> pmsBaseCatalog2s = pmsCatalogService.getCatalog2ByCatalog1Id(catalog1Id);
        return pmsBaseCatalog2s;
    }

    /**
     * 根据二级目录的id查询三级目录列表
     * @param catalog2Id
     * @return
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3ByCatalog2Id( String catalog2Id){
        List<PmsBaseCatalog3> pmsBaseCatalog3s = pmsCatalogService.getCatalog3ByCatalog2Id(catalog2Id);
        return pmsBaseCatalog3s;
    }


}
