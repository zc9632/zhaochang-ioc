package com.zc.test.bean.tck.auto;

import com.zc.annotation.Inject;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
public abstract class GasEngine extends Engine {

    @Override
    public void injectTwiceOverriddenWithOmissionInMiddle() {
        overriddenTwiceWithOmissionInMiddleInjected = true;
    }

    @Override
    @Inject
    public void injectTwiceOverriddenWithOmissionInSubclass() {
        overriddenTwiceWithOmissionInSubclassInjected = true;
    }
}

