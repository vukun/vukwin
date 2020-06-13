package com.njupt.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.api.AlipayClient;
import com.njupt.gmall.annotations.LoginRequired;
import com.njupt.gmall.bean.OmsOrder;
import com.njupt.gmall.bean.PaymentInfo;
import com.njupt.gmall.service.OrderService;
import com.njupt.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhaokun
 * @create 2020-06-10 21:24
 */
@Controller
public class PaymentController {

    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;

    /**
     * 提交订单后，进入选择支付方式所展示的index页面
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickname");
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("nickName",nickName);
        return "index";
    }

    /**
     * 选择微信支付的方法
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        return null;
    }

    //完整的代码

//    /**
//     * 选择支付宝支付的方法
//     * @param outTradeNo
//     * @param totalAmount
//     * @param request
//     * @param modelMap
//     * @return
//     */
//    @RequestMapping("alipay/submit")
//    @LoginRequired(loginSuccess = true)
//    @ResponseBody
//    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
//        //1、获得一个支付宝请求的客户端（它并不是一个链接，而是一个封装好的http的表单请求）
//        String form = null;
//        //封装参数
//        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
//        //同步和异步回调函数的地址
//        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
//        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
//        //先用集合封装信息，然后再将集合转成json字符串
//        Map<String, Object> map = new HashMap<>();
//        map.put("out_trade_no", outTradeNo);
//        map.put("product_code","FAST_INSTANT_TRADE_PAY");
//        map.put("total_amount", totalAmount);
//        map.put("subject","宇宙无敌感光徕卡曲面折叠屏7Plus瞎命名系列");
//        String param = JSON.toJSONString(map);
//        alipayRequest.setBizContent(param);
//        try {
//            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        //生成并保存用户的支付信息
//        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
//        PaymentInfo paymentInfo = new PaymentInfo();
//        paymentInfo.setCreateTime(new Date());
//        paymentInfo.setOrderId(omsOrder.getId());
//        paymentInfo.setOrderSn(outTradeNo);
//        paymentInfo.setPaymentStatus("未付款");
//        paymentInfo.setSubject("谷粒商城商品一件");
//        paymentInfo.setTotalAmount(totalAmount);
//        paymentService.savePaymentInfo(paymentInfo);
//        向消息中间件发送一个检查支付状态（由支付服务去消费）的延迟消息队列，
//        这么做是主动去定时查询支付的结果，避免长时间的等待支付宝操作结束后才给我们返回结果的延迟时间
//        paymentService.sendDelayPaymentResultCheck(outTradeNo, 5);
//        return form;
//    }

//    /**
//     * 支付宝支付成功后的同步回调的接口方法:更新支付的信息
//     * 异步回调接口的话是在另外一台商家的服务器，而不是我们编写代码的服务器
//     * @param request
//     * @param modelMap
//     * @return
//     */
//    @RequestMapping("alipay/callback/return")
//    @LoginRequired(loginSuccess = true)
//    public String alipayCallbackReturn(HttpServletRequest request, ModelMap modelMap){
//        //回调请求中获取支付宝的参数
//        //获取请求中的公共参数
//        String sign = request.getParameter("sign");
//        //获取请求中的业务参数
//        String trade_no = request.getParameter("trade_no");
//        String out_trade_no = request.getParameter("out_trade_no");
//        String trade_status = request.getParameter("trade_status");
//        String total_amount = request.getParameter("total_amount");
//        String subject = request.getParameter("subject");
//        String call_back_content = request.getQueryString();
//
//        //通过支付宝的paramMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
//        if(StringUtils.isNotBlank(sign)){
//            PaymentInfo paymentInfo = new PaymentInfo();
//            paymentInfo.setOrderSn(out_trade_no);
//            paymentInfo.setPaymentStatus("已支付");
//            paymentInfo.setAlipayTradeNo(trade_no);
//            paymentInfo.setCallbackContent(call_back_content);
//            paymentInfo.setCallbackTime(new Date());
//            //更新用户的支付状态
//            paymentService.updatePayment(paymentInfo);
//        }
//        return "finish";
//    }


    //自己改的代码，因为支付宝未走通

    /**
     * 选择支付宝支付的方法
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public ModelAndView alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
//        //1、获得一个支付宝请求的客户端（它并不是一个链接，而是一个封装好的http的表单请求）
//        String form = null;
//        //封装参数
//        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
//        //同步和异步回调函数的地址
//        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
//        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
//        //先用集合封装信息，然后再将集合转成json字符串
//        Map<String, Object> map = new HashMap<>();
//        map.put("out_trade_no", outTradeNo);
//        map.put("product_code","FAST_INSTANT_TRADE_PAY");
//        map.put("total_amount", totalAmount);
//        map.put("subject","宇宙无敌感光徕卡曲面折叠屏7Plus瞎命名系列");
//        String param = JSON.toJSONString(map);
//        alipayRequest.setBizContent(param);
//        try {
//            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
        //生成并保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("谷粒商城商品一件");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo);

        //向消息中间件发送一个检查支付状态（由支付服务去消费）的延迟消息队列，
        //这么做是主动去定时查询支付的结果，避免长时间的等待支付宝操作结束后才给我们返回结果的延迟时间
//        paymentService.sendDelayPaymentResultCheck(outTradeNo, 5);

        ModelAndView mv = new ModelAndView("redirect:http://localhost:8087/alipay/callback/return");
        mv.addObject("outTradeNo",outTradeNo);
        mv.addObject("totalAmount",totalAmount);
        return mv;
    }


    /**
     * 支付宝支付成功后的同步回调的接口方法:更新支付的信息
     * 异步回调接口的话是在另外一台商家的服务器，而不是我们编写代码的服务器
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String alipayCallbackReturn(HttpServletRequest request, ModelMap modelMap){
        //回调请求中获取支付宝的参数
        //获取请求中的公共参数
        String sign = "MyselfDownItItIsFake";
        //获取请求中的业务参数
        String trade_no = "202005201314";
        String out_trade_no = request.getParameter("outTradeNo");
//        String trade_status = request.getParameter("trade_status");
//        String total_amount = request.getParameter("total_amount");
//        String subject = request.getParameter("subject");
        String call_back_content = "自己写的不作数";

        //通过支付宝的paramMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if(StringUtils.isNotBlank(sign)){
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(call_back_content);
            paymentInfo.setCallbackTime(new Date());
            //更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
        }
        //分布式事务体现在：支付成功后，对支付信息的更新后并立即发送MQ消息给其他服务，
        // 也就是更新支付信息和发送消息要同时成功，或者同时失败回滚
        // 所以MQ就要写在更新支付信息的服务中。即MQ消息写在PaymentServiceImpl的updatePayment()方法内

        return "finish";
    }
}
