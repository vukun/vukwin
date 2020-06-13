package com.njupt.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.njupt.gmall.bean.UmsMember;
import com.njupt.gmall.service.UserService;
import com.njupt.gmall.util.HttpclientUtil;
import com.njupt.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhaokun
 * @create 2020-06-06 16:32
 */
@Controller
public class PassportController {

    @Reference
    UserService userService;


    /**
     * 请求的时候，需要带着原始请求的url，待认证结束后，让请求直接回到原始url去
     * @param ReturnUrl
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){
        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";
    }

    /**
     * 登录页面,验证用户输入的账户和密码是否正确
     * @param umsMember
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        String token = "";
        // 调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if(umsMemberLogin != null){
            //登录成功，需要用jwt制作token
            Map<String, Object> userMap = new HashMap<>();
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            userMap.put("memberId",memberId);
            userMap.put("nickname",nickname);
            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
            //加密生成token
            token = JwtUtil.encode("2020gmall0511", userMap, ip);
            //还需要将token存入redis一份
            userService.addUserToken(token, memberId);
        }else{
            //登陆失败
            token = "fail";
        }
        return token;
    }

    /**
     * 验证中心通过jwt校验token的真假
     * @param token
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp){

        // 通过jwt校验token真假
        Map<String, String> map = new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "2020gmall0511", currentIp);
        if(decode != null){
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
        }else{
            map.put("status", "fail");
        }
        return JSON.toJSONString(map);
    }

    @RequestMapping("vlogin")
    public String vlogin(String code, HttpServletRequest request){

        // 授权码换取access_token
        // client_secret=a79777bba04ac70d973ee002d27ed58c
        // client_id=1478025309
        String s3 = "https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","1478025309");
        paramMap.put("client_secret","e132eecc220d641a340575de6ab4ffe9");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://192.168.1.185:8085/vlogin");
        paramMap.put("code",code);// 授权有效期内可以使用，没新生成一次授权码，说明用户对第三方数据进行重启授权，之前的access_token和授权码全部过期
        String access_token_json = HttpclientUtil.doPost(s3, paramMap);

        Map<String,Object> access_map = JSON.parseObject(access_token_json,Map.class);
        //access_token换取用户信息
        String uid = (String) access_map.get("uid");
        String access_token = (String) access_map.get("access_token");
        String show_user_url = "https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String user_json = HttpclientUtil.doGet(show_user_url);
        Map<String,Object> user_map = JSON.parseObject(user_json,Map.class);
        //将用户信息保存到数据库，并且将用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String) user_map.get("idstr"));
        umsMember.setCity((String) user_map.get("location"));
        umsMember.setNickname((String) user_map.get("screen_name"));
        String g = "0";
        String gender = (String)user_map.get("gender");
        if(gender.equals("m")){
            g = "1";
        }
        umsMember.setGender(g);
        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        //先查询之前有没有登陆过，如果登陆过，查询出来即可，如果没有就把信息插入数据库
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck);
        if(umsMemberCheck==null){
            umsMember = userService.addOauthUser(umsMember);
        }else{
            umsMember = umsMemberCheck;
        }
        //生成jwt的token，并且重定向到首页，携带token
        String token = null;
        String memberId = umsMember.getId();//rpc的主键返回策略失效，因为无法跨越两层，他只能在dao层有用
        String nickname = umsMember.getNickname();
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId);//是保存数据库后主键返回策略生成的id
        userMap.put("nickname",nickname);

        String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();// 从request中获取ip
            if(StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }
        // 按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2020gmall0511", userMap, ip);
        // 将token存入redis一份
        userService.addUserToken(token,memberId);
        return "redirect:http://localhost:8083/index?token=" + token;
    }
}
