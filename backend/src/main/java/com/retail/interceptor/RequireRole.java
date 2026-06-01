package com.retail.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解 - 标记在 Controller 方法上，指定允许访问的角色
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 允许的角色列表：admin, manager, analyst
     */
    String[] value() default {};
}
