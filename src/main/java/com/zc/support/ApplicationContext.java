package com.zc.support;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.exception.BeanNotFoundException;
import com.zc.exception.BeanRepeatableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
@Slf4j
public class ApplicationContext {

    /**
     * 文件扫描
     */
    private FileScanner fileScanner;

    /**
     * bean工厂
     */
    private DefaultFactory factory;

    /**
     * 目标类
     */
    private List<Class<?>> cLasses;

    public ApplicationContext() {
        init();
    }

    public ApplicationContext(String... packages) {
        init(packages);
    }

    private void init(String... packages) {
        fileScanner = new FileScanner();
        // 初始化工厂
        factory = new DefaultFactory();
        // 初始化需要扫描的包路径
        fileScanner.addPackages(packages);
        // 初始化当前需要扫描的包中的类
        cLasses = fileScanner.getClasses();
        // 获取有@Named注解的bean
        List<BeanDefinition> beanDefinitions = this.getNamedAnnotationBeanDefinitions();
        // 初始化beanMap(@Named)
        this.initBeanMap(beanDefinitions);
        // 初始化注入的bean(@Inject)
        this.initInjectedBean(beanDefinitions);
    }

    /**
     * 此方法要求必须使用@Inject注解的类才会进行解析
     *
     * @param beanDefinitions
     */
    private void initInjectedBean(List<BeanDefinition> beanDefinitions) {
        // 初始化bean注入（1.set方式注入 2.变量方式注入）
        for (BeanDefinition beanDefinition : beanDefinitions) {
            // 通过反射获取属性变量
            Field[] fields = beanDefinition.getBeanClass().getDeclaredFields();
            for (Field field : fields) {
                this.injectObject(field, beanDefinition);
            }
        }
    }

    private void injectObject(Field field, BeanDefinition beanDefinition) {
        // 当前类
        Class aClass = beanDefinition.getBeanClass();
        // 当前类对应的bean
        Object classBean = factory.getBean(beanDefinition.getBeanName());
        // 当前属性
        String fieldName = field.getName();
        try {
            // 当前属性对应的set方法名称
            String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Inject inject = field.getAnnotation(Inject.class);
            Class<?> fieldType = field.getType();
            Method method = aClass.getMethod(setMethodName, fieldType);
            Inject methodInject = method.getAnnotation(Inject.class);
            String beanName;
            if (null != inject || null != methodInject) {
                // 该字段有注入的注解，需要判断注解里是否有指定的bean名称
                if (null != inject) {
                    beanName = inject.value();
                } else {
                    beanName = methodInject.value();
                }
                if (StringUtils.isEmpty(beanName)) {
                    // 没有手动指定bean名称，使用类型的小写名称
                    String fieldClassName = fieldType.getSimpleName();
                    beanName = fieldClassName.substring(0, 1).toLowerCase() + fieldClassName.substring(1);
                }
                if (factory.containsBean(beanName)) {
                    // 获取属性的set方法
                    Object bean = factory.getBean(beanName);
                    method.invoke(classBean, bean);
                } else {
                    throw new BeanNotFoundException("can not inject bean because can not found bean:" + beanName);
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error(e.getMessage());
        }
    }

    private void initBeanMap(List<BeanDefinition> beanDefinitions) {
        // 首先把找到的类转为BeanDefinition数据结构
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String beanName = beanDefinition.getBeanName();
            factory.registerBean(beanName, beanDefinition);
        }
    }

    /**
     * 获取具有给定注解的BeanDefinition
     *
     * @param singleton
     * @return
     */
    private List<BeanDefinition> getNamedAnnotationBeanDefinitions() {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        if (CollectionUtils.isEmpty(cLasses)) {
            log.info("no classes can be found");
            return Collections.emptyList();
        }
        for (Class<?> clazz : cLasses) {
            Named namedAnnotation = clazz.getAnnotation(Named.class);
            if (null != namedAnnotation) {
                // 有该注解，查看是否有自定义的bean名称
                String beanName = namedAnnotation.value();
                if (StringUtils.isEmpty(beanName)) {
                    // 没有自定义名称需要手动拼类名小写
                    String simpleName = clazz.getSimpleName();
                    beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                }
                try {
                    beanDefinitions.add(new BeanDefinition(beanName, clazz.newInstance(), clazz));
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("initBean error:{}", e.getMessage());
                }
            }
        }
        return beanDefinitions;
    }

    public void printBeans() {
        factory.listBean();
    }

    /**
     * 根据类型获取bean
     *
     * @param requiredType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        Object bean = factory.getBean(requiredType);
        return (T) bean;
    }

    public Object getBean(String name) {
        return factory.getBean(name);
    }
}
