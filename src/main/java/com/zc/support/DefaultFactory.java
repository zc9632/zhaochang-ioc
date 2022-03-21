package com.zc.support;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.exception.BeanNotFoundException;
import com.zc.exception.BeanRepeatableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
@Slf4j
public class DefaultFactory implements BeanFactory {

    /**
     * bean map
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);
    /**
     * key：bean类型 value：bean名称
     */
    private final Map<Class<?>, String> beanTypeMap = new ConcurrentHashMap<>(64);

    private final Set<String> beanNames = new HashSet<>(64);

    @Override
    public Object getBean(String name) {
        return getBeanByName(name);
    }

    public Object getBean(Class<?> requiredType) {
        String beanName = "";
        if (beanTypeMap.containsKey(requiredType)) {
            beanName = beanTypeMap.get(requiredType);
        }
        return getBeanByName(beanName);
    }

    private Object getBeanByName(String beanName) {
        Object bean = null;
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals(Scope.SCOPE_SINGLETON)) {
                // 单例直接从map中获取
                bean = beanDefinition.getBean();
            } else if (beanDefinition.getScope().equals(Scope.SCOPE_PROTOTYPE)) {
                // 这里由于是多例所以需要递归出所有的属性并创建对象
                bean = getNewBean(beanDefinition.getBeanClass());
            }
        }
        if (null == bean) {
            log.warn("bean not found, beanName:" + beanName);
        }
        return bean;
    }

    public Object getNewBean(Class<?> clazz) {
        Object instance = null;
        // 需要返回的对象实例,不是单例直接返回新实例
        Object bean = this.containsSingletonBean(clazz);
        if (null != bean) {
            // 由于是递归所以需要考虑深度递归后可能会有单例bean
            return bean;
        } else {
            // 不包含需要创建并注入对应的属性，注入顺序：1.构造方法 2.字段属性 3.方法注入
            // 先构造方法注入
            instance = this.constructsInject(clazz);
            if (null == instance){
                try {
                    instance = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            // 普通方法注入(放在前面，后面属性注入如果重复会直接覆盖因为后面重复注入会覆盖)
            methodInject(clazz, instance);
            // 属性注入
            fieldsInject(clazz, instance);
            return instance;
        }
    }

    private void methodInject(Class<?> clazz, Object instance) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            // 如果不为null说明已经通过其它方式注入了
            this.injectMethod(method, instance);
        }
    }

    private void injectMethod(Method method, Object instance) {
        if (!method.isAnnotationPresent(Inject.class)) {
            return;
        }
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                return;
            }
            method.setAccessible(true);
            method.invoke(instance, getMethodParameters(parameterTypes));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private List<Object> getMethodParameters(Class<?>[] parameterTypes) {
        List<Object> parameters = new ArrayList<>();
        for (Class<?> parameterType : parameterTypes) {
            Named named = parameterType.getAnnotation(Named.class);
            if (null == named) {
                parameters.add(null);
            } else {
                if (!StringUtils.isEmpty(named.value())) {
                    parameters.add(this.getBean(named.value()));
                } else if (this.containsBean(parameterType)) {
                    parameters.add(this.getBean(parameterType));
                } else {
                    parameters.add(getNewBean(parameterType));
                }
            }
        }
        return parameters;
    }

    /**
     * 构造方法注入
     *
     * @param clazz
     * @param instance
     */
    private Object constructsInject(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                try {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        return constructor.newInstance();
                    }
                    constructor.setAccessible(true);
                    return constructor.newInstance(getMethodParameters(parameterTypes));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 字段属性注入
     *
     * @param clazz
     * @param instance
     */
    private void fieldsInject(Class<?> clazz, Object instance) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 如果不为null说明已经通过其它方式注入了
            try {
                field.setAccessible(true);
                if (field.get(instance) == null) {
                    this.injectField(field, instance);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 包含该类的单例bean 则返回对应的bean，否则返回null
     *
     * @param clazz
     * @return
     */
    private Object containsSingletonBean(Class<?> clazz) {
        if (!beanTypeMap.containsKey(clazz)) {
            return null;
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanTypeMap.get(clazz));
        if (beanDefinition.getScope().equals(Scope.SCOPE_SINGLETON)) {
            return beanDefinition;
        }
        return null;
    }

    /**
     * 处理属性上的注解
     *
     * @param field  属性
     * @param target 该属性的实例
     */
    public void injectField(Field field, Object target) {
        if (!field.isAnnotationPresent(Inject.class)) {
            return;
        }
        field.setAccessible(true);
        Named named = field.getAnnotation(Named.class);
        try {
            if (null != named && StringUtils.isEmpty(named.value())) {
                // @Named注解中指定了bean名称
                String fieldBeanName = named.value();
                if (this.containsBean(fieldBeanName)) {
                    field.set(target, this.getBean(fieldBeanName));
                } else {
                    throw new BeanNotFoundException("specified bean not found:" + fieldBeanName);
                }
            } else {
                // 没有@Named注解或未指定名称
                field.set(target, this.getNewBean(field.getType()));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.getBeanName();
        if (this.containsBean(beanName)) {
            log.warn("you have created same beans defined by same name:{}", beanName);
            return;
        }
        beanDefinitionMap.put(beanName, beanDefinition);
        beanNames.add(beanName);
        beanTypeMap.put(beanDefinition.getBeanClass(), beanName);
    }

    public boolean containsBean(String beanName) {
        return beanNames.contains(beanName);
    }

    public boolean containsBean(Class<?> clazz) {
        return beanTypeMap.containsKey(clazz);
    }

    public void listBean() {
        if (beanDefinitionMap.isEmpty()) {
            log.info("DefaultFactory: no bean can be found");
            return;
        }
        log.info("Jsr330 all beans:");
        printBeans(beanDefinitionMap);
    }

    private void printBeans(Map<String, BeanDefinition>... beanDefinitionMaps) {
        int index = 1;
        for (Map<String, BeanDefinition> item : beanDefinitionMaps) {
            for (Map.Entry<String, BeanDefinition> entry : item.entrySet()) {
                log.info(index++ + ".{}:{}", entry.getKey(), entry.getValue());
            }
        }
    }
}
