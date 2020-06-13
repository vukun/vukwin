package com.njupt.gmall.service;

import com.njupt.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-06-05 13:46
 */
public interface CartService {
    OmsCartItem getCartsByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);
    
    void updateCart(OmsCartItem omsCartItemFromDb);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);

    void delCart(String skuId);
}
