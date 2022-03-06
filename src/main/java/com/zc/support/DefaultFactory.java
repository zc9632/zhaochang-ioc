package com.zc.support;

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

    @Override
    public Object getBean(String name) {
        if (beanDefinitionMap.containsKey(name)){
            return beanDefinitionMap.get(name).getBean();
        }
        throw new IllegalArgumentException("未找到bean:" + name);
    }

    public void registerBean(String beanName, BeanDefinition beanDefinition){
        if (this.containsBean(beanName)){
            throw new BeanRepeatableException("you have created two beans defined by same name:" + beanName);
        }
        beanDefinitionMap.put(beanName, beanDefinition);
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
