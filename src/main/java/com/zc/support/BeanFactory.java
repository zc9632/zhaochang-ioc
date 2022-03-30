package com.zc.support;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
public interface BeanFactory {
    /**
     * 根据名称获取bean
     * @param name
     * @return
     */
    Object getBean(String name);

    /**
     * 根据类型获取bean
     * @param classType
     * @return
     */
    Object getBean(Class<?> classType);

    /**
     * 注册bean
     * @param beanDefinition
     */
    void registerBean(BeanDefinition beanDefinition);
}
