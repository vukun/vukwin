package com.njupt.gmall.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//在方法上使用
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//判断该类是否需要注解来限制认证，不需要的话就给通过，需要的话就进行下面“是否一定通过”的验证
public @interface LoginRequired {

    //判定是否一定需要认证通过，true是，false不是
    boolean loginSuccess() default true;

}
