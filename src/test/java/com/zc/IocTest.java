package com.zc;

import com.zc.support.ApplicationContext;
import com.zc.test.bean.Student;
import com.zc.test.circulardependency.A;
import com.zc.test.circulardependency.B;
import com.zc.test.circulardependency.CDObject;
import com.zc.test.circulardependency.ProviderA;
import com.zc.test.circulardependency.SingletonB;
import com.zc.test.circulardependency.TestObject;
import com.zc.test.configuration.TestConfigurationBean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
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
        ApplicationContext ac = ApplicationContext.createApplicationContext();
        ac.printBeans();
    }

    /**
     * 测试是否可以获取到bean及属性注入
     */
    @Test
    public void testGetStudent(){
        ApplicationContext ac = ApplicationContext.createApplicationContext();
        Student student = ac.getBean(Student.class);
        log.info("student:{}", student);
        Assert.assertNotNull(student.getAction());
    }

    /**
     * 测试是拿到的是否是相同的对象
     */
    @Test
    public void testSameBean(){
        ApplicationContext ac = ApplicationContext.createApplicationContext();
        Student student1 = ac.getBean(Student.class);
        Student student2 = ac.getBean(Student.class);
        Assert.assertNotSame(student1, student2);
        Assert.assertSame(student1.getAction(), student2.getAction());
    }

    /**
     * 查看配置路径是否生效
     */
    @Test
    public void testPackageConfiguration(){
        ApplicationContext ac = ApplicationContext.createApplicationContext();
        TestConfigurationBean bean = ac.getBean(TestConfigurationBean.class);
        Assert.assertNull(bean);
    }

    @Ignore
    @Test(expected = ExceptionInInitializerError.class)
    public void testCircularDependency() {
        ApplicationContext ac = ApplicationContext.createApplicationContext();
    }

    @Test
    public void testProviderCD(){
        ApplicationContext ac = ApplicationContext.createApplicationContext();
        ac.getBean(ProviderA.class);
        ac.getBean(A.class);
    }

    @org.junit.Test
    public void testSingletonCD(){
        ApplicationContext ac = ApplicationContext.createApplicationContext();
        ac.getBean(SingletonB.class);
        ac.getBean(B.class);
    }

    @Test
    public void testProviderGetSameObject(){
        ApplicationContext ac = ApplicationContext.createApplicationContext();
        TestObject testObject = ac.getBean(TestObject.class);
        CDObject cdObject1 = testObject.getCdObject().get();
        CDObject cdObject2 = testObject.getCdObject().get();
        Assert.assertNotSame(cdObject1, cdObject2);
    }

    @Data
    static class ZcTest{
        private Student student1;
        private Student student2;
    }


}
