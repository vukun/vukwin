package com.njupt.gmall.user.service.impl;

//采用具有Dubbo的方式去扫描：意思是：不仅是一个spring的应用，也是具有dubbo协议的 RPC服务。
//所以在application.properties也要使用不同的扫描方式

import com.alibaba.dubbo.config.annotation.Service;
import com.njupt.gmall.bean.UmsMemberReceiveAddress;
import com.njupt.gmall.service.UmsMemberReceiveAddressService;
import com.njupt.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-18 10:45
 */
@Service
public class UmsMemberReceiveAddressServiceImpl implements UmsMemberReceiveAddressService {

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMemberReceiveAddress> getUmsMemberReceiveAddressById(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }
}
