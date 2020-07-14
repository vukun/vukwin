package com.njupt.gmall.service;

import com.njupt.gmall.bean.UmsMember;
import com.njupt.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-11 20:36
 */
public interface UserService {

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String memberId);

    UmsMember addOauthUser(UmsMember umsMember);

    UmsMember checkOauthUser(UmsMember umsCheck);

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);

    void register(UmsMember umsMember);

    String checkUsername(String username);

    String checkPhone(String phone);

    void addAddress(UmsMemberReceiveAddress umsMemberReceiveAddress);
}
