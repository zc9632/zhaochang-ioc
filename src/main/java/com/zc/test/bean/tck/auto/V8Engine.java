package com.zc.test.bean.tck.auto;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.test.bean.tck.auto.accessories.SpareTire;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
public class V8Engine extends GasEngine {

    public V8Engine() {
        publicNoArgsConstructorInjected = true;
    }

    @Override
    @Inject
    void injectPackagePrivateMethod() {
        if (subPackagePrivateMethodInjected) {
            overriddenPackagePrivateMethodInjectedTwice = true;
        }
        subPackagePrivateMethodInjected = true;
    }

    /**
     * Qualifiers are swapped from how they appear in the superclass.
     */
    @Override
    public void injectQualifiers(Seat seatA, @Drivers Seat seatB,
                                 Tire tireA, @Named("spare") Tire tireB) {
        if ((seatA instanceof DriversSeat)
                || !(seatB instanceof DriversSeat)
                || (tireA instanceof SpareTire)
                || !(tireB instanceof SpareTire)) {
            qualifiersInheritedFromOverriddenMethod = true;
        }
    }

    @Override
    void injectPackagePrivateMethodForOverride() {
        subPackagePrivateMethodForOverrideInjected = true;
    }

    @Override
    @Inject public void injectTwiceOverriddenWithOmissionInMiddle() {
        overriddenTwiceWithOmissionInMiddleInjected = true;
    }

    @Override
    public void injectTwiceOverriddenWithOmissionInSubclass() {
        overriddenTwiceWithOmissionInSubclassInjected = true;
    }
}

