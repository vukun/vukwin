package com.njupt.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.njupt.gmall.util.HttpclientUtil;
import com.njupt.gmall.annotations.LoginRequired;
import com.njupt.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截代码

        // 判断被拦截请求的访问的方法是否有注解(是否需要拦截)，有，拦截，没有就放行
        //根据请求的方法名，利用反射的技术，得到该方法名的注解 methodAnnotation
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        // 根据methodAnnotation是否为空，判断该方法是否需要被拦截
        //如果等于null，不需要拦截
        if (methodAnnotation == null) {
            return true;
        }
        //不等于空，进入拦截器的拦截方法，判断是否需要“必须登录”才能通过
        String token = "";

        //从cookie中获得的老的Token
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        //从地址栏获取新的Token
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        // 是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();// 获得该请求是否必登录成功

        // 可以先调用认证中心进行验证，然后再判断是否 "必须登录" 才能访问
        String success = "fail";
        Map<String, String> successMap = new HashMap<>();
        if(StringUtils.isNotBlank(token)){
            String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();// 从request中获取ip
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
            String successJson  = HttpclientUtil.doGet("http://localhost:8085/verify?token=" + token + "&currentIp=" + ip);//currentIp是原本请求的ip，此时需要把原始请求的ip传过去
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }

        if (loginSuccess) {
            // 必须登录成功才能使用
            //如果验证没通过，重定向去passport登录
            if (!success.equals("success")) {
                //重定向会passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://localhost:8085/index?ReturnUrl="+requestURL);
                return false;
            }
            // 验证通过
            // 需要将token携带的用户信息写入
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));
            //验证通过，并且需要覆盖cookie中的token，
            // 因为原始的cookie中的Token是有过期时间的，当刷新一次的时候会使得过期时间延长
            if(StringUtils.isNotBlank(token)){
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
            }
        } else {
            // 不是“必须登录”也可以访问，但是必须验证，它会影响购物车所走的分支（cookie或redis）
            if (success.equals("success")) {
                // 需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                //验证通过，覆盖cookie中的token
                if(StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
        }
        return true;
    }
}
