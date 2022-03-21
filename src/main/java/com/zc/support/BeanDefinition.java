package com.zc.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
@Data
public class BeanDefinition {
    /**
     * bean的名字
     */
    private String beanName;
    /**
     * bean对象
     */
    private Object bean;
    /**
     * 每个类对应的class对象
     */
    private Class beanClass;

    /**
     * 作用域
     */
    private Scope scope;

    public BeanDefinition(String beanName, Object bean, Class beanClass, Scope scope) {
        this.beanName = beanName;
        this.bean = bean;
        this.beanClass = beanClass;
        this.scope = scope;
    }

    public BeanDefinition() {
        this.scope = Scope.SCOPE_PROTOTYPE;
    }

    public BeanDefinition(String beanName, Object bean, Class beanClass) {
        this.beanName = beanName;
        this.bean = bean;
        this.beanClass = beanClass;
        this.scope = Scope.SCOPE_PROTOTYPE;
    }
}
