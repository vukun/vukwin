package com.njupt.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.njupt.gmall.bean.PmsBaseCatalog1;
import com.njupt.gmall.bean.PmsBaseCatalog2;
import com.njupt.gmall.bean.PmsBaseCatalog3;
import com.njupt.gmall.manager.mapper.*;
import com.njupt.gmall.service.PmsCatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-20 22:50
 */
@Service
public class PmsCatalogServiceImpl implements PmsCatalogService {

    @Autowired
    PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;
    @Autowired
    PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;
    @Autowired
    PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;


    /**
     * 直接去库中查询一级目录列表
     *
     * @return
     */
    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return pmsBaseCatalog1Mapper.selectAll();
    }

    /**
     * 根据一级目录的id，查询二级目录列表
     *
     * @param catalog1Id
     * @return
     */
    @Override
    public List<PmsBaseCatalog2> getCatalog2ByCatalog1Id(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        List<PmsBaseCatalog2> pmsBaseCatalog2s = pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);
        return pmsBaseCatalog2s;
    }

    /**
     * 根据二级目录的id查询三级目录列表
     *
     * @param catalog2Id
     * @return
     */
    @Override
    public List<PmsBaseCatalog3> getCatalog3ByCatalog2Id(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        List<PmsBaseCatalog3> pmsBaseCatalog3s = pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
        return pmsBaseCatalog3s;
    }
}