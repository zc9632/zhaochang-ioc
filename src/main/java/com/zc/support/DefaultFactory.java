package com.zc.support;

import com.zc.exception.BeanNotFoundException;
import com.zc.exception.BeanRepeatableException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
@Slf4j
public class DefaultFactory implements BeanFactory{

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);
    private final Map<Class, String> beanTypeMap = new ConcurrentHashMap<>(64);

    @Override
    public Object getBean(String name) {
        Object bean = null;
        if (beanDefinitionMap.containsKey(name)){
            bean = beanDefinitionMap.getOrDefault(name, new BeanDefinition()).getBean();
        }
        if (null == bean) {
            log.warn("bean not found, beanName:" + name);
        }
        return bean;
    }

    public Object getBean(Class requiredType){
        Object bean = null;
        if (beanTypeMap.containsKey(requiredType)){
            bean = beanDefinitionMap.getOrDefault(beanTypeMap.get(requiredType), new BeanDefinition()).getBean();
        }
        if (null == bean) {
            log.warn("bean not found, type of bean:" + requiredType);
        }
        return bean;
    }

    public void registerBean(String beanName, BeanDefinition beanDefinition){
        if (this.containsBean(beanName)){
            throw new BeanRepeatableException("you have created two beans defined by same name:" + beanName);
        }
        beanDefinitionMap.put(beanName, beanDefinition);
        beanTypeMap.put(beanDefinition.getBeanClass(), beanName);
    }

    public boolean containsBean(String beanName){
        return beanDefinitionMap.containsKey(beanName);
    }

    public void listBean(){
        if (beanDefinitionMap.isEmpty()){
            log.info("DefaultFactory: no bean can be found");
            return;
        }
        log.info("Jsr330 all beans:");
        int index = 1;
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            log.info(index++ + ".{}:{}", entry.getKey(), entry.getValue());
        }
    }
}
