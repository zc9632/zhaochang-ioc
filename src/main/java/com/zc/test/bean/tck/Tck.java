package com.zc.test.bean.tck;

import com.zc.test.bean.tck.auto.Car;
import com.zc.test.bean.tck.auto.Convertible;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
public class Tck {

    private Tck() {}

    /**
     * Constructs a JUnit test suite for the given {@link Car} instance.
     *
     * @param car to test
     * @param supportsStatic true if the injector supports static member
     *  injection
     * @param supportsPrivate true if the injector supports private member
     *  injection
     *
     * @throws NullPointerException if car is null
     * @throws ClassCastException if car doesn't extend
     *  {@link Convertible Convertible}
     */
    public static Test testsFor(Car car, boolean supportsStatic,
                                boolean supportsPrivate) {
        if (car == null) {
            throw new NullPointerException("car");
        }

        if (!(car instanceof Convertible)) {
            throw new ClassCastException("car doesn't implement Convertible");
        }

        Convertible.localConvertible.set((Convertible) car);
        try {
            TestSuite suite = new TestSuite(Convertible.Tests.class);
            if (supportsStatic) {
                suite.addTestSuite(Convertible.StaticTests.class);
            }
            if (supportsPrivate) {
                suite.addTestSuite(Convertible.PrivateTests.class);
            }
            return suite;
        } finally {
            Convertible.localConvertible.remove();
        }
    }
}

