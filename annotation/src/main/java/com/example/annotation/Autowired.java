package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xushibin
 * @date 2019-11-19
 * description：
 */
@Retention(RetentionPolicy.CLASS) //表示注解保留到class文件，运行时会被丢弃
@Target(ElementType.FIELD)
public @interface Autowired {
    String value() default "";
}
