package com.zc;

import com.zc.support.ApplicationContext;
import com.zc.test.bean.Student;
import com.zc.test.configuration.TestConfigurationBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhaochang.
 * @Date 2022/3/6.
 * @desc
 */
@Slf4j
public class IocTest {
    /**
     * 打印所有的bean
     */
    @Test
    public void testBeanGenerate(){
        ApplicationContext ac = new ApplicationContext();
        ac.printBeans();
    }

    /**
     * 测试是否可以获取到bean及属性注入
     */
    @Test
    public void testGetStudent(){
        ApplicationContext ac = new ApplicationContext();
        Student student = ac.getBean(Student.class);
        log.info("student:{}", student);
        Assert.assertNotNull(student.getAction());
    }

    /**
     * 测试是否拿到的是相同的对象
     */
    @Test
    public void testSameBean(){
        ApplicationContext ac = new ApplicationContext();
        Student student1 = ac.getBean(Student.class);
        Student student2 = ac.getBean(Student.class);
        Assert.assertSame(student1, student2);
        Assert.assertSame(student1.getAction(), student2.getAction());
    }

    /**
     * 查看配置路径是否生效
     */
    @Test
    public void testPackageConfiguration(){
        ApplicationContext ac = new ApplicationContext();
        TestConfigurationBean bean = ac.getBean(TestConfigurationBean.class);
        Assert.assertNull(bean);
    }
}
