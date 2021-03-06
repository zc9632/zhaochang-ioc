package com.zc.support;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.annotation.Provider;
import com.zc.annotation.Singleton;
import com.zc.exception.CircularDependencyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    /**
     * bean名称
     */
    private final Set<String> beanNames = new HashSet<>(64);

    /**
     * 自定义注解
     */
    private List<Class<?>> customizedAnnotations;

    /**
     * bean缓存，处理循环依赖
     */
    private List<Class<?>> cashedBean = new ArrayList<>(32);

    public DefaultFactory(List<Class<?>> customizedAnnotations) {
        this.customizedAnnotations = customizedAnnotations;
    }

    @Override
    public Object getBean(String name) {
        return getBeanByName(name);
    }

    /**
     * 根据类型获取bean
     * @param requiredType
     * @return
     */
    @Override
    public Object getBean(Class<?> requiredType) {
        String beanName = checkInterface(requiredType);
        if (beanTypeMap.containsKey(requiredType)) {
            beanName = beanTypeMap.get(requiredType);
        }
        return getBeanByName(beanName);
    }

    /**
     * 校验是否是接口并返回对应的bean名称
     * @param requiredType
     * @return
     */
    private String checkInterface(Class<?> requiredType) {
        if (requiredType.isInterface()) {
            // 需要获取他的实现类
            for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
                Class<?>[] interfaces = beanDefinition.getBeanClass().getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    if (anInterface.equals(requiredType)) {
                        return beanDefinition.getBeanName();
                    }
                }
            }
        }
        return "";
    }

    public BeanDefinition getBeanDefinition(String name) {
        return beanDefinitionMap.get(name);
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
                bean = constructBean(beanDefinition.getBeanClass());
            }
        }
        return bean;
    }

    public Object constructBean(Class<?> clazz) {
        Object instance = null;
        // 需要返回的对象实例,不是单例直接返回新实例
        Object bean = this.containsSingletonBean(clazz);
        if (null != bean) {
            // 由于是递归所以需要考虑深度递归后可能会有单例bean
            return bean;
        } else {
            instance = getNewBean(clazz);
        }
        return instance;
    }

    public Object getNewBean(Class<?> clazz) {
        if (cashedBean.contains(clazz)){
            throw new CircularDependencyException("Please use singleton annotation or Provider interface to resolve circularDependency, clazz:" + clazz.getName());
        }
        cashedBean.add(clazz);
        Object instance = null;
        // 不包含需要创建并注入对应的属性，注入顺序：1.构造方法 2.字段属性 3.方法注入
        // 先构造方法注入
        instance = this.constructsInject(clazz);
        if (null == instance) {
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 先注入父类属性
        this.parentFieldsInject(clazz, instance);
        // 先执行父类的方法
        this.parentMethodsInject(clazz, instance);
        // 属性注入
        this.fieldsInject(clazz, instance);
        // 普通方法注入
        this.methodInject(clazz, instance);
        cashedBean.remove(clazz);
        return instance;
    }

    private void methodInject(Class<?> clazz, Object instance) {
//        List<Method> overrideMethods = getParentOverrideMethods(clazz);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            this.injectMethod(method, instance);
        }
    }

    private void parentMethodsInject(Class<?> clazz, Object instance) {
        Class<?> superclass = clazz.getSuperclass();
        if (null == superclass || superclass.equals(Object.class)) {
            return;
        }
        // 暂时不确实是否只执行子类
        List<Method> overrideMethods = getParentOverrideMethods(clazz);
        Method[] methods = superclass.getDeclaredMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (overrideMethods.contains(method) && Modifier.isPublic(modifiers)) {
                // 子类重写的方法子类优先，其余方法父类优先
                continue;
            }
            if (!Modifier.isProtected(modifiers)) {
                // 受保护的方法不会执行，单测中有案例
                this.injectMethod(method, instance);
            }
        }
    }

    /**
     * 获取被重写的方法
     *
     * @param clazz
     * @return
     */
    private List<Method> getParentOverrideMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Method[] declaredMethods = clazz.getDeclaredMethods();
        Method[] parentDeclaredMethods = clazz.getSuperclass().getDeclaredMethods();
        for (Method pMethod : parentDeclaredMethods) {
            for (Method cMethod : declaredMethods) {
                if (pMethod.getName().equals(cMethod.getName()) && pMethod.getReturnType().equals(cMethod.getReturnType())) {
                    Class<?>[] pTypes = pMethod.getParameterTypes();
                    Class<?>[] cTypes = cMethod.getParameterTypes();
                    if (pTypes.length == cTypes.length) {
                        boolean isEqual = true;
                        for (int i = 0; i < pTypes.length; i++) {
                            if (!pTypes[i].equals(cTypes[i])) {
                                isEqual = false;
                                break;
                            }
                        }
                        if (isEqual) {
                            methods.add(pMethod);
                            break;
                        }
                    }
                }
            }
        }
        return methods;
    }

    private void injectMethod(Method method, Object instance) {
        if (!method.isAnnotationPresent(Inject.class)) {
            return;
        }
        try {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            Class<?>[] parameterTypes = method.getParameterTypes();
            method.setAccessible(true);
            method.invoke(instance, getMethodParameters(parameterTypes, genericParameterTypes, parameterAnnotations, ClassEnum.METHOD));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object[] getMethodParameters(Class<?>[] parameterTypes, Type[] genericParameterTypes, Annotation[][] parameterAnnotations, ClassEnum classEnum) {
        Object[] objects = new Object[parameterTypes.length];
        int index = 0;
        for (Class<?> parameterType : parameterTypes) {
            boolean shouldInject = shouldBeInjected(parameterAnnotations[index], parameterType);
            DefaultProvider<Object> provider = checkProvider(parameterType, genericParameterTypes[index], classEnum, parameterAnnotations[index]);
            if (null == provider) {
                if (!shouldInject) {
                    objects[index] = null;
                } else if (this.containsBean(parameterType)) {
                    BeanDefinition childBeanDefinition = this.getChildBeanDefinition(parameterType);
                    if (null != childBeanDefinition) {
                        objects[index] = this.getBean(childBeanDefinition.getBeanClass());
                    } else {
                        objects[index] = this.getBean(parameterType);
                    }
                } else {
                    objects[index] = constructBean(parameterType);
                }
            } else {
                objects[index] = provider;
            }
            index++;
        }
        return objects;
    }

    private DefaultProvider<Object> checkProvider(Class<?> parameterType, Type genericParameterType, ClassEnum classEnum, Annotation[] annotations) {
        if (parameterType.equals(Provider.class)) {
            if (genericParameterType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                Class<?> genericClazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                DefaultProvider<Object> provider = new DefaultProvider<>(this, getProviderTypeName(genericClazz, annotations));
                // 实例化的时候是否需要创建新实例
                boolean isProviderNeedNewBean = this.isProviderNeedNewBean(genericClazz, classEnum, annotations);
                // 是否有自定义注解
                boolean isProviderNeedFindChild = this.isHasCustomizedAnnotation(annotations);
                provider.setNeedNewBean(isProviderNeedNewBean);
                provider.setNeedNewBean(isProviderNeedFindChild);
                return provider;
            }
        }
        return null;
    }

    private boolean isHasCustomizedAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type.equals(Named.class) || customizedAnnotations.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean isProviderNeedNewBean(Class<?> genericClazz, ClassEnum classEnum, Annotation[] annotations) {
        // 先判断是否有注入的注解，有的话不考虑范型是否是单例
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.equals(Named.class) || customizedAnnotations.contains(annotationType)) {
                return true;
            }
        }
        if (genericClazz.isAnnotationPresent(Singleton.class)) {
            return false;
        }
        switch (classEnum) {
            case METHOD:
                if (checkCircularlyDependentSingletons(genericClazz)) {
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 检查provider中的类型是否有单例循环依赖
     *
     * @param genericClazz
     * @return
     */
    private boolean checkCircularlyDependentSingletons(Class<?> genericClazz) {
        if (!genericClazz.isAnnotationPresent(Singleton.class)) {
            return false;
        }
        Field[] declaredFields = genericClazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Class<?> type = declaredField.getType();
            if (!type.isAnnotationPresent(Singleton.class)) {
                continue;
            }
            Field[] typeDeclaredFields = type.getDeclaredFields();
            for (Field typeDeclaredField : typeDeclaredFields) {
                Class<?> parameterType = typeDeclaredField.getType();
                Type genericType = typeDeclaredField.getGenericType();
                if (parameterType.equals(Provider.class)) {
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericType;
                        Class<?> genericClazz1 = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        if (genericClazz1.equals(genericClazz)) {
                            return true;
                        }
                    }
                } else {
                    if (parameterType.equals(genericClazz)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 构造方法注入
     *
     * @param clazz
     */
    private Object constructsInject(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                try {
                    Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
                    Type[] genericParameterTypes = constructor.getGenericParameterTypes();
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if (genericParameterTypes.length == 0) {
                        return constructor.newInstance();
                    }
                    constructor.setAccessible(true);
                    return constructor.newInstance(getMethodParameters(parameterTypes, genericParameterTypes, parameterAnnotations, ClassEnum.CONSTRUCTOR));
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
            this.injectField(field, instance);
        }
    }

    private void parentFieldsInject(Class<?> clazz, Object instance) {
        Class<?> superclass = clazz.getSuperclass();
        if (null == superclass) {
            return;
        }
        Field[] fields = superclass.getDeclaredFields();
        for (Field field : fields) {
            this.injectField(field, instance);
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
            return beanDefinition.getBean();
        }
        return null;
    }

    /**
     * 处理属性上的注解
     *
     * @param field  属性
     * @param instance 该属性的实例
     */
    public void injectField(Field field, Object instance) {
        if (!field.isAnnotationPresent(Inject.class)) {
            return;
        }
        if (checkProvider(field, instance)) {
            return;
        }
        // 如果不为null说明已经通过其它方式注入了
        try {
            field.setAccessible(true);
            if (this.shouldBeInjected(field.getAnnotations(), field.getType())) {
                Named named = field.getAnnotation(Named.class);
                if (null != named && StringUtils.isEmpty(named.value())) {
                    // @Named注解中指定了bean名称
                    String fieldBeanName = named.value();
                    if (this.containsBean(fieldBeanName)) {
                        field.set(instance, this.getBean(fieldBeanName));
                    }
                } else {
                    // 没有@Named注解或未指定名称
                    if (this.isHasCustomizedAnnotation(field.getAnnotations())) {
                        BeanDefinition childBeanDefinition = this.getChildBeanDefinition(field.getType());
                        if (null != childBeanDefinition) {
                            field.set(instance, this.constructBean(childBeanDefinition.getBeanClass()));
                        }
                    } else {
                        field.set(instance, this.constructBean(field.getType()));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void registerBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.getBeanName();
        if (this.containsBean(beanName)) {
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
        this.printBeans(beanDefinitionMap);
    }

    public boolean shouldBeInjected(Annotation[] annotations, Class<?> clazz) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Named.class)
                    || annotation.annotationType().equals(Singleton.class)
                    || customizedAnnotations.contains(annotation.annotationType())
                    || this.containsBean(clazz)) {
                return true;
            }
        }
        return false;
    }

    private void printBeans(Map<String, BeanDefinition>... beanDefinitionMaps) {
        int index = 1;
        for (Map<String, BeanDefinition> item : beanDefinitionMaps) {
            for (Map.Entry<String, BeanDefinition> entry : item.entrySet()) {
                log.info(index++ + ".{}:{}", entry.getKey(), entry.getValue());
            }
        }
    }

    public BeanDefinition getBeanDefinition(Class<?> type) {
        return beanDefinitionMap.get(beanTypeMap.get(type));
    }

    public String getBeanNameByClass(Class clazz) {
        return beanTypeMap.get(clazz);
    }

    public boolean checkProvider(Field field, Object instance) {
        if (field.getType().equals(Provider.class)) {
            initProvider(field, instance);
            return true;
        }
        return false;
    }

    private void initProvider(Field field, Object instance) {
        Type genericType = field.getGenericType();
        try {
            field.setAccessible(true);
//          // 获取泛型里的类型
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Class<?> genericClazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                field.setAccessible(true);
                DefaultProvider<Object> provider = new DefaultProvider<>(this, getProviderTypeName(genericClazz, null));
                if (genericClazz.isAnnotationPresent(Singleton.class)) {
                    provider.setNeedNewBean(false);
                }
                if (isHasCustomizedAnnotation(field.getAnnotations())) {
                    provider.setNeedNewBean(true);
                    provider.setNeedFindChild(true);
                } else {
                    provider.setNeedNewBean(false);
                    provider.setNeedFindChild(false);
                }
                field.set(instance, provider);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private String getProviderTypeName(Class<?> genericClazz, Annotation[] annotations) {
        String beanName = "";
        if (null != annotations) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Named.class)) {
                    String name = ((Named) annotation).value();
                    if (!StringUtils.isEmpty(name)) {
                        return name;
                    }
                }
            }
        }
        // 首先检查是否有provider里的类型的bean
        if (this.containsBean(genericClazz)) {
            return this.getBeanNameByClass(genericClazz);
        }
        // 如果没有再检查目前map中是否有类是否有是该类型子类的bean
        Set<Class<?>> classes = beanTypeMap.keySet();
        for (Class<?> clazz : classes) {
            if (isChild(clazz, genericClazz)) {
                return getProviderTypeName(clazz, null);
            }
        }
        String simpleName = genericClazz.getSimpleName();
        beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        return beanName;
    }

    private boolean isChild(Class<?> clazz, Class<?> genericClazz) {
        Class<?> clazzCopy = clazz;
        boolean isChild = false;
        while (true) {
            Class<?> superclass = clazzCopy.getSuperclass();
            if (null == superclass) {
                // 父类是null
                break;
            } else if (superclass.equals(genericClazz)) {
                // 父类不是是null且是指定类型的子类
                isChild = true;
                break;
            } else {
                // 不是子类
                clazzCopy = superclass;
            }
        }
        return isChild;
    }

    /**
     * 获取bean容器管理的子类
     * @param beanClass 父类等于这种类型
     * @return
     */
    public BeanDefinition getChildBeanDefinition(Class<?> beanClass) {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            Class<?> superclass = beanDefinition.getBeanClass().getSuperclass();
            while (true) {
                if (null == superclass) {
                    break;
                }
                if (superclass.equals(beanClass)) {
                    return beanDefinition;
                }
                superclass = superclass.getSuperclass();
            }
        }
        return null;
    }
}
