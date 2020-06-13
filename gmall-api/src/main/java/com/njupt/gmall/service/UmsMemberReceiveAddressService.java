package com.njupt.gmall.service;

import com.njupt.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-18 10:44
 */
public interface UmsMemberReceiveAddressService {

    /**
     * zhaokun
     * @param memberId 用户id
     * @return 该用户的收货地址
     */
    List<UmsMemberReceiveAddress> getUmsMemberReceiveAddressById(String memberId);

}

