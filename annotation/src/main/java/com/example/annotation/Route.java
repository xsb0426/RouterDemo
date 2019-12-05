package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xushibin
 * @date 2019-09-27
 * description：
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Route {
    String value();
}
