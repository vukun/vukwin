package com.njupt.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.bean.UmsMemberReceiveAddress;
import com.njupt.gmall.service.UmsMemberReceiveAddressService;
import com.njupt.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-11 20:35
 */
@Controller
@RequestMapping("user")
public class UserController {

    @Reference
    UserService userService;

    @Reference
    UmsMemberReceiveAddressService umsMemberReceiveAddressService;

    @RequestMapping("index")
    @ResponseBody
    public String test(){
        return "Hello GMall!";
    }

    @RequestMapping("getUmsMemberAddress")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getUmsMemberReceiveAddressById(String memberId){
       return umsMemberReceiveAddressService.getUmsMemberReceiveAddressById(memberId);
    }

}
