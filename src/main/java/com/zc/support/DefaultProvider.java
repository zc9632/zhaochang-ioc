package com.zc.support;

import com.zc.annotation.Provider;

import java.util.ArrayList;

/**
 * @author zhaochang.
 * @Date 2022/3/23.
 * @desc
 */
public class DefaultProvider<T> implements Provider<T> {

    private DefaultFactory factory;

    private String beanName;

    private boolean isNeedNewBean;

    private boolean isNeedFindChild;

    @Override
    public T get() {
        BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);
        if (null == beanDefinition){
            return null;
        }
        if (isNeedFindChild){
            BeanDefinition childBeanDefinition = factory.getChildBeanDefinition(beanDefinition.getBeanClass());
            if (null != childBeanDefinition){
                // 说明bean中有子类，加载子类的bean
                beanDefinition = childBeanDefinition;
            }
        }
        Object bean;
        if (isNeedNewBean){
            bean = factory.getNewBean(beanDefinition.getBeanClass());
        }else {
            bean = factory.getBean(beanName);
        }
        if (bean != null){
            return (T) bean;
        }else {
            return null;
        }
    }

    public DefaultProvider(DefaultFactory factory, String beanName) {
        this.factory = factory;
        this.beanName = beanName;
        isNeedNewBean = true;
        isNeedFindChild = true;
    }

    public void setNeedNewBean(boolean needNewBean) {
        isNeedNewBean = needNewBean;
    }

    public void setNeedFindChild(boolean needFindChild) {
        isNeedFindChild = needFindChild;
    }
}
