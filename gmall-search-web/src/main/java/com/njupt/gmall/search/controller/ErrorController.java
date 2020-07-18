package com.njupt.gmall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zhaokun
 * @create 2020-07-10 22:30
 */
@Controller
public class ErrorController {

    @RequestMapping("Error")
    public String error(){
        return "demo";
    }
}
