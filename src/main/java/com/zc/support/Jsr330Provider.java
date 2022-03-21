package com.zc.support;

import com.zc.annotation.Provider;

/**
 * @author zhaochang.
 * @Date 2022/3/20.
 * @desc
 */
public class Jsr330Provider implements Provider {

    private BeanFactory beanFactory;

    @Override
    public Object get() {
        return null;
    }

    public Jsr330Provider(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
