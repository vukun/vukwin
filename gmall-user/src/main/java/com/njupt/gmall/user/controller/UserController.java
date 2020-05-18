package com.njupt.gmall.user.controller;

import com.njupt.gmall.user.bean.UmsMemberReceiveAddress;
import com.njupt.gmall.user.service.UmsMemberReceiveAddressService;
import com.njupt.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Autowired
    UserService userService;

    @Autowired
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
