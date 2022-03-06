package com.zc.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
@AllArgsConstructor
@NoArgsConstructor
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

}
