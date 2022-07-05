package com.zc.support;

import com.zc.annotation.Provider;

/**
 * @author zhaochang.
 * @Date 2022/3/23.
 * @desc
 */
public class DefaultProvider<T> implements Provider<T> {

    private final DefaultFactory factory;

    private final String beanName;

    private boolean isNeedNewBean;

    private boolean isNeedFindChild;

    /**
     * JSR330标准要求每次get的值不能相同
     *
     * @return
     */
//    private Object bean;
    @Override
    public T get() {
        BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);
        if (null == beanDefinition) {
            return null;
        }
        Object bean;
        if (isNeedNewBean && isNeedFindChild) {
            BeanDefinition childBeanDefinition = factory.getChildBeanDefinition(beanDefinition.getBeanClass());
            if (null != childBeanDefinition) {
                // 说明bean中有子类，加载子类的bean
                beanDefinition = childBeanDefinition;
            }
            bean = factory.getNewBean(beanDefinition.getBeanClass());
        } else {
            bean = factory.getBean(beanName);
        }
        return (T) bean;
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
