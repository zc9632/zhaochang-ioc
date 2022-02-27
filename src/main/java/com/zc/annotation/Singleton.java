package com.zc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author zhaochang.
 * @Date 2022/2/20.
 * @desc
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@Documented()
public @interface Singleton {
}
