package com.zc.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
@Slf4j
public class FileScanner {
    /**
     * 当前项目路径
     */
    private static final String DEFAULT_PACKAGE = "com.zc";

    /**
     * 包的扫描路径
     */
    private static List<PackageDefinition> packages;

    static {
        initPackages();
    }

    static void initPackages() {
        // 需要先扫描是否有配置的包路径
        setConfigurePackages();
        // 如果没有则使用默认路径
        if (CollectionUtils.isEmpty(packages)){
            packages.add(new PackageDefinition(DEFAULT_PACKAGE, true));
        }
    }

    protected static void addPackages(String[] newPackages) {
        if (null == newPackages || newPackages.length == 0) {
            return;
        }
        for (String newPackage : newPackages) {
            if (checkPackage(newPackage)) {
                packages.add(new PackageDefinition(newPackage, true));
            }
        }
    }

    private static boolean checkPackage(String newPackage) {
        for (int index = 0; index < packages.size(); index++) {
            PackageDefinition packageDefinition = packages.get(index);
            String packageName = packageDefinition.getPackageName();
            if (newPackage.startsWith(packageName)) {
                // 说明已经有路径包含当前路径,直接不做处理
                return false;
            }
            if (packageName.startsWith(newPackage)) {
                // 说明新路径包含当前老路径，删除当前老路径,继续遍历
                packageDefinition.invalid();
            }
        }
        return true;
    }

    private static void setConfigurePackages() {
        // todo 需要初始化配置的包路径
        packages = new ArrayList<>();
    }

    /**
     * 获取当前路径{@link path} 下的所有类，如果不存在则获取{@link DEFAULT_PATH}下的所有类
     */
    public List<Class<?>> getClasses() {
        List<Class<?>> classes = new ArrayList<>();
        if (CollectionUtils.isEmpty(packages)) {
            log.info("未设置需要扫描的路径，使用默认路径");
        }
        for (PackageDefinition packageDefinition : packages) {
            if (packageDefinition.isValid()){
                classes.addAll(this.getSpecifiedPackageClasses(packageDefinition.getPackageName()));
            }
        }
        return classes;
    }

    /**
     * 获取某个包路径中的类
     * @param packageName
     * @return
     */
    private List<Class<?>> getSpecifiedPackageClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String packageDir = packageName.replace('.', '/');
        Enumeration<URL> urls;
        try {
            urls = Thread.currentThread().getContextClassLoader().getResources(packageDir);
            while (urls.hasMoreElements()) {
                // 获取下一个元素
                URL url = urls.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String fileDir = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    this.findFileClasses(packageName, fileDir, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    System.out.println("jar类型的扫描");
                    JarFile jar;
                    // 获取jar
                    jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    // 从此jar包 得到一个枚举类
                    Enumeration<JarEntry> entries = jar.entries();
                    this.findJarClasses(packageName, packageDir, entries, classes);
                }
            }
        } catch (IOException e) {
            log.error("未获取到资源，path:{}, 错误信息:{}", packageDir, e.getMessage());
        }
        return classes;
    }

    /**
     * 获取文件类型的class
     * @param packageName
     * @param fileDir
     * @param classes
     */
    private void findFileClasses(String packageName, String fileDir, List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(fileDir);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.isDirectory()) {
            log.info("path不存在:{}", fileDir);
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirfiles = dir.listFiles(file -> (file.isDirectory()) || (file.getName().endsWith(".class")));
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findFileClasses(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className));
                } catch (ClassNotFoundException e) {
                    log.error("未找到类:{}, 错误信息:{}", packageName, e.getMessage());
                }
            }
        }
    }

    /**
     * 获取jar包中的class
     * @param packageName
     * @param fileDir
     * @param entries
     * @param classes
     */
    private void findJarClasses(String packageName, String fileDir, Enumeration<JarEntry> entries, List<Class<?>> classes) {
        // 同样的进行循环迭代
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(fileDir)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if ((idx != -1)) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        try {
                            // 添加到classes
                            classes.add(Class.forName(packageName + '.' + className));
                        } catch (ClassNotFoundException e) {
                            log.error("未找到类:{}, 错误信息:{}", packageName, e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
