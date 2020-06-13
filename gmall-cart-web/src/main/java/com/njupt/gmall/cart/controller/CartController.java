package com.njupt.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.njupt.gmall.annotations.LoginRequired;
import com.njupt.gmall.bean.OmsCartItem;
import com.njupt.gmall.bean.PmsSkuInfo;
import com.njupt.gmall.service.CartService;
import com.njupt.gmall.service.PmsSkuService;
import com.njupt.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhaokun
 * @create 2020-06-05 11:24
 */
@Controller
public class CartController {

    @Reference
    PmsSkuService pmsSkuService;
    @Reference
    CartService cartService;


    /**
     * 把商品信息加入购物车
     * @param skuId
     * @param quantity
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response, HttpSession session){
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = pmsSkuService.getSkuInfo(skuId);
        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        //判断用户是否登录
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //判断用户是否登录
        if(StringUtils.isBlank(memberId)){
            //用户没有登陆的状态下,把数据放入到cookie中

            //此时需要判断cookie中是否有值：如果没值，则加入商品信息
            //                          如果有值，判断是否是当前商品：如果是，更新个数
            //                                                   如果不是，添加当前商品
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //判断cookie中是否有值
            if(StringUtils.isBlank(cartListCookie)){
                //cookie中没有值，把商品信息放入到商品集合中
                omsCartItems.add(omsCartItem);
            }else{
                //cookie中有值，判断cookie中的值是否含有当前商品
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean exist = if_cart_exist(omsCartItems, omsCartItem);
                if(exist){
                    //如果当前商品的cookie存在，则更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }
                }else{
                    //如果当前商品的cookie不存在，则添加当前商品信息到cookie中
                    omsCartItems.add(omsCartItem);
                }
            }
            //最后把商品信息放入到cookie中
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems),60*60*72, true);
        }else{
            //用户登录的状态下,放入到DB中，更新到缓存中
            //首先，需要根据memberId和skuId,去查询DB中是否存在该商品信息，若有，更新即可，若没有，添加到DB中
            OmsCartItem omsCartItemFromDb = cartService.getCartsByUser(memberId, skuId);
            //根据查询返回的结果判断数据库中没有该商品信息
            if(omsCartItemFromDb == null){
                //如果没有，放入数据库中
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname(nickname);
                omsCartItem.setQuantity(new BigDecimal(quantity));
                //将封装好的商品信息放入到数据库中
                cartService.addCart(omsCartItem);
            }else{
                //如果有，更新商品信息即可
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
            //并且同步到缓存中
            cartService.flushCartCache(memberId);
        }
        return "redirect:/success.html";
    }

    /**
     * 判断购物车cookies中有没有当前商品信息
     * @param omsCartItems
     * @param omsCartItem
     * @return
     */
    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean exist = false;
        for (OmsCartItem cartItem : omsCartItems) {
            if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                exist = true;
            }
        }
        return exist;
    }

    /**
     * 查询购物车信息，返回购物车商品列表
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        if(StringUtils.isNotBlank(memberId)){
            //已经登陆的情况下，查询缓存和Db
            omsCartItems = cartService.cartList(memberId);
        }else{
            //没有登录时，查询cookie的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        modelMap.put("cartList", omsCartItems);
        // 被勾选商品的总额
        BigDecimal totalAmount =getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartList";
    }

    /**
     * 计算购物车详情页面所选中的商品的总价格
     * @param omsCartItems
     * @return
     */
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if(omsCartItem.getIsChecked() != null){
                if(omsCartItem.getIsChecked().equals("1")){
                    totalAmount = totalAmount.add(totalPrice);
                }
            }
        }

        return totalAmount;
    }

    /**
     * 根据isChecked、skuId字段用ajax异步请求刷新内嵌页面
     * @param isChecked
     * @param skuId
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response,HttpSession session, ModelMap modelMap){

        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");

       //调用服务修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        cartService.checkCart(omsCartItem);

        //将最新的数据从缓存中取出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        // 被勾选商品的总额
        BigDecimal totalAmount =getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

}
