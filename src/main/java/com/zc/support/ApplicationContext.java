package com.zc.support;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.annotation.Singleton;
import com.zc.exception.BeanNotFoundException;
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
     * 目标类，如果有配置路径则写配置路径下的所有类，如果没有默认当前src目录
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
        // 初始化bean
        this.initBean();
    }

    private void initBean() {
        /**
         * 获取所有被管理的bean
         */
        List<BeanDefinition> beanDefinitions = this.getNamedAnnotationBeanDefinitions();
        /**
         * 初始化beanMap
         */
        this.initBeanMap(beanDefinitions);
        /**
         * 初始化bean中需要注入的属性
         */
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
            factory.registerBean(beanDefinition);
            Class beanClass = beanDefinition.getBeanClass();
            Field[] fields = beanClass.getDeclaredFields();
            Method[] methods = beanClass.getDeclaredMethods();
            for (Field field : fields) {
                factory.injectField(field, beanDefinition.getBean());
            }
        }
    }

    private void initBeanMap(List<BeanDefinition> beanDefinitions) {
        // 首先把找到的类转为BeanDefinition数据结构
        for (BeanDefinition beanDefinition : beanDefinitions) {
            factory.registerBean(beanDefinition);
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
            return beanDefinitions;
        }
        for (Class<?> clazz : cLasses) {
            if (clazz.isAnnotationPresent(Named.class)) {
                beanDefinitions.add(this.toBeanDefinition(clazz));
            }
        }
        return beanDefinitions;
    }

    private BeanDefinition toBeanDefinition(Class<?> clazz) {
        Named namedAnnotation = clazz.getAnnotation(Named.class);
        // 有该注解，查看是否有自定义的bean名称
        String beanName = namedAnnotation.value();
        if (StringUtils.isEmpty(beanName)) {
            // 没有自定义名称需要手动拼类名小写
            String simpleName = clazz.getSimpleName();
            beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        }
        BeanDefinition beanDefinition = new BeanDefinition(beanName, factory.getNewBean(clazz), clazz);
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
