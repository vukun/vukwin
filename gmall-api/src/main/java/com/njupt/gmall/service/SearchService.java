package com.njupt.gmall.service;

import com.njupt.gmall.bean.PmsSearchParam;
import com.njupt.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-30 22:04
 */
public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);

}
