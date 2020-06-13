package com.njupt.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.njupt.gmall.bean.PmsBaseAttrInfo;
import com.njupt.gmall.bean.PmsBaseAttrValue;
import com.njupt.gmall.bean.PmsBaseSaleAttr;
import com.njupt.gmall.manager.mapper.PmsBaseAttrInfoMapper;
import com.njupt.gmall.manager.mapper.PmsBaseAttrValueMapper;
import com.njupt.gmall.manager.mapper.PmsBaseSaleAttrMapper;
import com.njupt.gmall.service.PmsAttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author zhaokun
 * @create 2020-05-21 13:26
 */
@Service
public class PmsAttrServiceImpl implements PmsAttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    /**
     * 根据三级目录查询下属的属性名称列表
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsBaseAttrInfo> getAttrInfoListByCatalog3Id(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        /**
         * 因为 ”商品属性信息管理“ 中的 ”增加SKU“ 功能 在展示选择属性和属性值的时候是调用了这里的方法，获取平台属性和平台属性值
         * 并不是新写的方法
         *不同的是：在“平台属性列表”功能时，只是要求获取属性列表，并没有获取属性值列表
         *        而在“商品属性信息管理”功能中，是需要同时获取属性列表和属性值列表的(双重集合)
         * 所以，要在原有的基础上增加获取属性值列表，并且封装到pmsBaseAttrInfo对象中，返回给前端
         */
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            List<PmsBaseAttrValue> pmsBaseAttrValues = new ArrayList<>();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }
        return pmsBaseAttrInfos;
    }

    /**
     * 当我点击"修改"的时候，需要先根据属性id查询属性值列表
     * @param attrId
     * @return
     */
    @Override
    public List<PmsBaseAttrValue> getAttrValueListByAttrId(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }

    /**
     * 在平台属性中，添加属性名称和属性值或者修平台属性名称和属性值
     * 两者不同的是：添加属性名称和属性值的时候，是还没生成属性名称id的
     *             修改属性名称和属性值的时候是在原有基础上修改的，此时已经产生了属性名称id
     *所以service层可以先判断参数中有没有属性名称id再进行相应的操作
     *对于修改属性名称是直接在原有的基础上直接修改的，而对于修改属性值的时候是采用先删除原有的属性值再把新的属性值插入
     * @param pmsBaseAttrInfo
     * @return
     */
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();
        //当属性名称id为空时，说明是新增属性名称和属性值的操作
        if(StringUtils.isBlank(id)){
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }else{
            //当属性名称id不为空时，说明是修改属性名称和属性值的操作

            //根据属性名称表的id修改属性名称
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo, example);

            //根据属性名称id删除属性值
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);

            //先从参数中获取要修改的属性值集合，再根据属性名称id插入属性值
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue baseAttrValue : pmsBaseAttrValues) {
                pmsBaseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
        return "success";
    }

    @Override
    public List<PmsBaseSaleAttr> getBaseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    /**
     * 在ES搜索功能搜出来结果集时候，展示页面结果的平台属性，可根据valueId将属性列表查询出来
     * @param valueSet
     * @return
     */
    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueSet) {
        String valueIdStr = StringUtils.join(valueSet, ",");//(41,45,46)
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);
        return pmsBaseAttrInfos;
    }
}
