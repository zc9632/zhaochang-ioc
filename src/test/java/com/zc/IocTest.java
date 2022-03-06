package com.zc;

import com.zc.support.ApplicationContext;
import org.junit.Test;

/**
 * @author zhaochang.
 * @Date 2022/3/6.
 * @desc
 */
public class IocTest {
    @Test
    public void testBeanGenerate(){
        ApplicationContext ac = new ApplicationContext();
        ac.printBeans();
    }
}
