package com.zc.test.bean.tck.auto;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.test.bean.tck.auto.accessories.SpareTire;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
public abstract class Engine {

    protected boolean publicNoArgsConstructorInjected;
    protected boolean subPackagePrivateMethodInjected;
    protected boolean superPackagePrivateMethodInjected;
    protected boolean subPackagePrivateMethodForOverrideInjected;
    protected boolean superPackagePrivateMethodForOverrideInjected;

    protected boolean overriddenTwiceWithOmissionInMiddleInjected;
    protected boolean overriddenTwiceWithOmissionInSubclassInjected;

    protected Seat seatA;
    protected Seat seatB;
    protected Tire tireA;
    protected Tire tireB;

    public boolean overriddenPackagePrivateMethodInjectedTwice;
    public boolean qualifiersInheritedFromOverriddenMethod;

    @Inject
    void injectPackagePrivateMethod() {
        superPackagePrivateMethodInjected = true;
    }

    @Inject void injectPackagePrivateMethodForOverride() {
        superPackagePrivateMethodForOverrideInjected = true;
    }

    @Inject public void injectQualifiers(@Drivers Seat seatA, Seat seatB,
                                         @Named("spare") Tire tireA, Tire tireB) {
        if (!(seatA instanceof DriversSeat)
                || (seatB instanceof DriversSeat)
                || !(tireA instanceof SpareTire)
                || (tireB instanceof SpareTire)) {
            qualifiersInheritedFromOverriddenMethod = true;
        }
    }

    @Inject public void injectTwiceOverriddenWithOmissionInMiddle() {
        overriddenTwiceWithOmissionInMiddleInjected = true;
    }

    @Inject public void injectTwiceOverriddenWithOmissionInSubclass() {
        overriddenTwiceWithOmissionInSubclassInjected = true;
    }
}

