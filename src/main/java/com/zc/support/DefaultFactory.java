package com.zc.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
public class DefaultFactory implements BeanFactory{

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

    @Override
    public Object getBean(String name) {
        if (beanDefinitionMap.containsKey(name)){
            return beanDefinitionMap.get(name).getBean();
        }
        throw new IllegalArgumentException("未找到bean:" + name);
    }

    public void registerBean(String beanName, BeanDefinition beanDefinition){
        beanDefinitionMap.put(beanName, beanDefinition);
    }
}
