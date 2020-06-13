package com.njupt.gmall.service;

import com.njupt.gmall.bean.PmsBaseAttrInfo;
import com.njupt.gmall.bean.PmsBaseAttrValue;
import com.njupt.gmall.bean.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

/**
 * @author zhaokun
 * @create 2020-05-21 13:27
 */
public interface PmsAttrService {

    List<PmsBaseAttrInfo> getAttrInfoListByCatalog3Id(String catalog3Id);

    List<PmsBaseAttrValue> getAttrValueListByAttrId(String attrId);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseSaleAttr> getBaseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueSet);

}
