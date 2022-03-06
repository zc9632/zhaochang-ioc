package org.example;

import static org.junit.Assert.assertTrue;

import com.zc.testbean.Student;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void test() {
        System.out.println(Student.class.getSimpleName());
        System.out.println(Student.class.getName());
    }
}
