package com.njupt.gmall.service;

import com.njupt.gmall.bean.*;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-20 22:44
 */
public interface PmsCatalogService {

    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2ByCatalog1Id(String catalog1Id);

    List<PmsBaseCatalog3> getCatalog3ByCatalog2Id(String catalog2Id);


}
