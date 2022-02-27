package com.zc.annotation;

/**
 * @author zhaochang.
 * @Date 2022/2/20.
 * @desc
 */
public interface Provider<T> {
    /**
     * 获取范型对象
     * @return
     */
    T get();
}
