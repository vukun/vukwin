package com.njupt.gmall.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//指定该注解的使用范围：在方法上使用
@Target(ElementType.METHOD)
//注解@Retention可以用来修饰注解，是注解的注解，称为元注解。RetentionPolicy表示注解的生命周期。
// .RUNTIME：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
@Retention(RetentionPolicy.RUNTIME)
//判断该类是否需要注解来限制认证，不需要的话就给通过，需要的话就进行下面“是否一定通过”的验证
public @interface LoginRequired {

    //判定是否一定需要认证通过，true是，false不是
    boolean loginSuccess() default true;

}
