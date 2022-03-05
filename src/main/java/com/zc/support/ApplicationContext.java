package com.zc.support;

import com.zc.annotation.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

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

    public ApplicationContext(){
        init();
    }

    public ApplicationContext(String... packages){
        init(packages);
    }

    private void init(String... packages) {
        fileScanner = new FileScanner();
        // 初始化工厂
        factory = new DefaultFactory();
        // 初始化需要扫描的包路径
        FileScanner.addPackages(packages);
        // 初始化当前需要扫描的包中的类
        cLasses = fileScanner.getClasses();
        // 初始化beanMap(@Named)
        initBeanMap();
        // 初始化注入的bean(@Inject)
        initInjectedBean();
    }

    private void initInjectedBean() {

    }

    private void initBeanMap() {
        // 首先把找到的类转为BeanDefinition数据结构
        List<BeanDefinition> beanDefinitions = getNamedAnnotationBeanDefinitions();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            factory.registerBean(beanDefinition.getBeanName(), beanDefinition);
        }
    }

    /**
     * 获取具有给定注解的BeanDefinition
     * @param singleton
     * @return
     */
    private List<BeanDefinition> getNamedAnnotationBeanDefinitions() {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        if (CollectionUtils.isEmpty(cLasses)){
            log.info("未在包路径下找到类");
            return Collections.emptyList();
        }
        for (Class<?> clazz : cLasses) {
            Named namedAnnotation = clazz.getAnnotation(Named.class);
            if (null != namedAnnotation){
                // 有该注解，查看是否有自定义的bean名称
                String beanName = namedAnnotation.value();
                if ("".equals(beanName)){
                    // 没有自定义名称需要手动拼类名小写
                    String simpleName = clazz.getSimpleName();
                    beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                }
                try {
                    beanDefinitions.add(new BeanDefinition(beanName, clazz.newInstance(), clazz));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return beanDefinitions;
    }

}
