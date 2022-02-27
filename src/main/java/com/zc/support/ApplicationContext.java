package com.zc.support;

import java.util.List;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */

public class ApplicationContext {

    private BeanFactory factory;

    /**
     * 没有配置扫描路径时候的默认路径是当前项目路径
     */
    private static final String DEFAULT_PATH = System.getProperty("user.dir");

    public ApplicationContext(){
        init();
    }

    public ApplicationContext(String... packages){
        init(packages);
    }

    private void init(String... packages) {
        factory = new DefaultFactory();
        FileScanner.addPackages(packages);
    }
}
