package com.zc.test.bean.tck.auto.accessories;

import com.zc.annotation.Inject;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
public class RoundThing {

    public boolean packagePrivateMethod2Injected;

    @Inject
    void injectPackagePrivateMethod2() {
        packagePrivateMethod2Injected = true;
    }

    public boolean packagePrivateMethod3Injected;

    @Inject void injectPackagePrivateMethod3() {
        packagePrivateMethod3Injected = true;
    }

    public boolean packagePrivateMethod4Injected;

    @Inject void injectPackagePrivateMethod4() {
        packagePrivateMethod4Injected = true;
    }
}

