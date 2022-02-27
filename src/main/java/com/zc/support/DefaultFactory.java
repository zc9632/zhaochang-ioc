package com.zc.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
public class DefaultFactory implements BeanFactory{

    Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);


    @Override
    public Object getBean(String name) {
        return null;
    }
}
