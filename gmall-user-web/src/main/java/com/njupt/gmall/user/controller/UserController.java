package com.njupt.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.bean.UmsMember;
import com.njupt.gmall.bean.UmsMemberReceiveAddress;
import com.njupt.gmall.service.UmsMemberReceiveAddressService;
import com.njupt.gmall.service.UserService;
import com.njupt.gmall.util.Md5Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author zhaokun
 * @create 2020-05-11 20:35
 */
@Controller
public class UserController {

    @Reference
    UserService userService;

    @Reference
    UmsMemberReceiveAddressService umsMemberReceiveAddressService;

    @RequestMapping("checkUsername")
    @ResponseBody
    public String checkUsername(HttpServletRequest request) {
        String username = request.getParameter("username");
        String usernameResult = userService.checkUsername(username);
        return usernameResult;
    }


    @RequestMapping("checkPhone")
    @ResponseBody
    public String checkPhone(HttpServletRequest request) {
        String phone = request.getParameter("phone");
        String phoneResult = userService.checkPhone(phone);
        return phoneResult;
    }

    /**
     * 用户注册的方法，
     *
     * @param username 用户名
     * @param password 用户密码
     * @param phone    注册手机号
     * @return
     */
    @RequestMapping("index")
    public ModelAndView index(String username, String password, String phone) {
        // MD5加密工具类
        Md5Utils md5Utils = new Md5Utils();
        ModelAndView mv = null;
        if (username != null) {
            UmsMember umsMember = new UmsMember();
            umsMember.setUsername(username);
            //采用 MD5加密方法对前端传过来的 MD5加密后的密码 再次加密，称为 后端 MD5加密
            String passwordOfEncode = md5Utils.getMd5ofStr(password);
            umsMember.setPassword(passwordOfEncode);
            umsMember.setPhone(phone);
            userService.register(umsMember);
            String url = "redirect:http://localhost:8085/index?ReturnUrl=http://localhost:8083/index";
            mv = new ModelAndView(url);
        } else {
            mv = new ModelAndView("index");
        }
        return mv;
    }

    @RequestMapping("getUmsMemberAddress")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getUmsMemberReceiveAddressById(String memberId) {
        return umsMemberReceiveAddressService.getUmsMemberReceiveAddressById(memberId);
    }

}
