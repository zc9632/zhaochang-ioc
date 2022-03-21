package com.zc.support;

/**
 * @author zhaochang.
 * @Date 2022/3/19.
 * @desc
 */
public enum Scope {
    /**
     * 单例
     */
    SCOPE_SINGLETON("singleton"),
    /**
     * 多例
     */
    SCOPE_PROTOTYPE("prototype");

    Scope(String value) {
    }

}
