package com.zc.support;

import com.zc.annotation.Named;
import com.zc.annotation.Qualifier;
import com.zc.annotation.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
     * 目标类，如果有配置路径则写配置路径下的所有类，如果没有默认当前src目录
     */
    private List<Class<?>> classes;


    public ApplicationContext() {
        init();
    }

    public ApplicationContext(String... packages) {
        init(packages);
    }

    private void init(String... packages) {
        fileScanner = new FileScanner();
        // 初始化需要扫描的包路径
        fileScanner.addPackages(packages);
        // 初始化当前需要扫描的包中的类
        classes = fileScanner.getClasses();
        // 初始化工厂
        factory = new DefaultFactory(this.initCustomizedAnnotations(classes));
        // 初始化bean(只初始化类上带有Named，Singleton和自定义注解的类)
        this.initBean();
    }

    private List<Class<?>> initCustomizedAnnotations(List<Class<?>> classes) {
        List<Class<?>> customizedAnnotations = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotation()) {
                continue;
            }
            if (clazz.isAnnotationPresent(Qualifier.class)) {
                customizedAnnotations.add(clazz);
            }
        }
        return customizedAnnotations;
    }

    private void initBean() {
        /**
         * 获取所有被管理的bean
         */
        List<BeanDefinition> beanDefinitions = this.getNamedAnnotationBeanDefinitions();
        /**
         * 初始化beanMap
         */
        this.initBeans(beanDefinitions);
    }

    private void initBeans(List<BeanDefinition> beanDefinitions) {
        // 首先把找到的类转为BeanDefinition数据结构
        for (BeanDefinition beanDefinition : beanDefinitions) {
            factory.registerBean(beanDefinition);
        }
        // 初始化bean属性
        List<BeanDefinition> hasInit = new ArrayList<>();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            initBean(beanDefinition, hasInit);
        }
    }

    private void initBean(BeanDefinition beanDefinition, List<BeanDefinition> hasInit) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Class<?> type = field.getType();
                if (factory.containsBean(type)) {
                    // 如果bean容器存在该类型
                    if (!hasInit.contains(factory.getBeanDefinition(type))) {
                        // 存在没有初始化
                        initBean(factory.getBeanDefinition(type), hasInit);
                    } else {
                        // 存在且已经初始化
                        field.set(beanDefinition.getBean(), factory.getBean(type));
                    }
                } else {
                    // 不存在，检查是否有可以被初始化的注解@Named或自定义注解
                    if (factory.shouldBeInjected(field.getAnnotations(), type)) {
                        field.set(beanDefinition.getBean(), factory.getNewBean(type));
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        hasInit.add(beanDefinition);
    }

    /**
     * 获取具有给定注解的BeanDefinition
     *
     * @param singleton
     * @return
     */
    private List<BeanDefinition> getNamedAnnotationBeanDefinitions() {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        if (CollectionUtils.isEmpty(classes)) {
            log.info("no classes can be found");
            return beanDefinitions;
        }
        for (Class<?> clazz : classes) {
            Annotation[] annotations = clazz.getDeclaredAnnotations();
            if (factory.shouldBeInjected(annotations, clazz)) {
                beanDefinitions.add(this.toBeanDefinition(clazz));
            }
        }
        return beanDefinitions;
    }

    private BeanDefinition toBeanDefinition(Class<?> clazz) {
        Named namedAnnotation = clazz.getAnnotation(Named.class);
        // 有该注解，查看是否有自定义的bean名称，没有使用类名首字母小写
        String simpleName = clazz.getSimpleName();
        String beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        if (null != namedAnnotation && !StringUtils.isEmpty(namedAnnotation.value())) {
            // 有自定义注解名称
            beanName = namedAnnotation.value();
        }
        BeanDefinition beanDefinition = null;
        try {
            beanDefinition = new BeanDefinition(beanName, clazz.newInstance(), clazz);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Singleton singleton = clazz.getAnnotation(Singleton.class);
        if (null != singleton) {
            // 设置作用域为单例
            beanDefinition.setScope(Scope.SCOPE_SINGLETON);
        }
        return beanDefinition;
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

    public void register(Class<?> beanClass) {
        BeanDefinition beanDefinition = this.toBeanDefinition(beanClass);
        if (factory.containsBean(beanDefinition.getBeanName())) {
            return;
        } else {
            factory.registerBean(beanDefinition);
        }
    }

    public Object getBean(String name) {
        return factory.getBean(name);
    }
}
