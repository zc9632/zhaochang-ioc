package com.zc.test.bean.tck.auto;

import com.zc.annotation.Inject;
import com.zc.test.bean.tck.auto.accessories.RoundThing;
import com.zc.test.bean.tck.auto.accessories.SpareTire;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
public class Tire extends RoundThing {

    protected static final FuelTank NEVER_INJECTED = new FuelTank();

    protected static final Set<String> moreProblems = new LinkedHashSet<String>();

    FuelTank constructorInjection = NEVER_INJECTED;
    @Inject FuelTank fieldInjection = NEVER_INJECTED;
    FuelTank methodInjection = NEVER_INJECTED;
    @Inject static FuelTank staticFieldInjection = NEVER_INJECTED;
    static FuelTank staticMethodInjection = NEVER_INJECTED;

    boolean constructorInjected;

    protected boolean superPrivateMethodInjected;
    protected boolean superPackagePrivateMethodInjected;
    protected boolean superProtectedMethodInjected;
    protected boolean superPublicMethodInjected;
    protected boolean subPrivateMethodInjected;
    protected boolean subPackagePrivateMethodInjected;
    protected boolean subProtectedMethodInjected;
    protected boolean subPublicMethodInjected;

    protected boolean superPrivateMethodForOverrideInjected;
    protected boolean superPackagePrivateMethodForOverrideInjected;
    protected boolean subPrivateMethodForOverrideInjected;
    protected boolean subPackagePrivateMethodForOverrideInjected;
    protected boolean protectedMethodForOverrideInjected;
    protected boolean publicMethodForOverrideInjected;

    public boolean methodInjectedBeforeFields;
    public boolean subtypeFieldInjectedBeforeSupertypeMethods;
    public boolean subtypeMethodInjectedBeforeSupertypeMethods;
    public static boolean staticMethodInjectedBeforeStaticFields;
    public static boolean subtypeStaticFieldInjectedBeforeSupertypeStaticMethods;
    public static boolean subtypeStaticMethodInjectedBeforeSupertypeStaticMethods;
    public boolean similarPrivateMethodInjectedTwice;
    public boolean similarPackagePrivateMethodInjectedTwice;
    public boolean overriddenProtectedMethodInjectedTwice;
    public boolean overriddenPublicMethodInjectedTwice;

    @Inject public Tire(FuelTank constructorInjection) {
        this.constructorInjection = constructorInjection;
    }

    @Inject void supertypeMethodInjection(FuelTank methodInjection) {
        if (!hasTireBeenFieldInjected()) {
            methodInjectedBeforeFields = true;
        }
        if (hasSpareTireBeenFieldInjected()) {
            subtypeFieldInjectedBeforeSupertypeMethods = true;
        }
        if (hasSpareTireBeenMethodInjected()) {
            subtypeMethodInjectedBeforeSupertypeMethods = true;
        }
        this.methodInjection = methodInjection;
    }

    @Inject static void supertypeStaticMethodInjection(FuelTank methodInjection) {
        if (!Tire.hasBeenStaticFieldInjected()) {
            staticMethodInjectedBeforeStaticFields = true;
        }
        if (SpareTire.hasBeenStaticFieldInjected()) {
            subtypeStaticFieldInjectedBeforeSupertypeStaticMethods = true;
        }
        if (SpareTire.hasBeenStaticMethodInjected()) {
            subtypeStaticMethodInjectedBeforeSupertypeStaticMethods = true;
        }
        staticMethodInjection = methodInjection;
    }

    @Inject private void injectPrivateMethod() {
        if (superPrivateMethodInjected) {
            similarPrivateMethodInjectedTwice = true;
        }
        superPrivateMethodInjected = true;
    }

    @Inject void injectPackagePrivateMethod() {
        if (superPackagePrivateMethodInjected) {
            similarPackagePrivateMethodInjectedTwice = true;
        }
        superPackagePrivateMethodInjected = true;
    }

    @Inject protected void injectProtectedMethod() {
        if (superProtectedMethodInjected) {
            overriddenProtectedMethodInjectedTwice = true;
        }
        superProtectedMethodInjected = true;
    }

    @Inject public void injectPublicMethod() {
        if (superPublicMethodInjected) {
            overriddenPublicMethodInjectedTwice = true;
        }
        superPublicMethodInjected = true;
    }

    @Inject private void injectPrivateMethodForOverride() {
        subPrivateMethodForOverrideInjected = true;
    }

    @Inject void injectPackagePrivateMethodForOverride() {
        subPackagePrivateMethodForOverrideInjected = true;
    }

    @Inject protected void injectProtectedMethodForOverride() {
        protectedMethodForOverrideInjected = true;
    }

    @Inject public void injectPublicMethodForOverride() {
        publicMethodForOverrideInjected = true;
    }

    protected final boolean hasTireBeenFieldInjected() {
        return fieldInjection != NEVER_INJECTED;
    }

    protected boolean hasSpareTireBeenFieldInjected() {
        return false;
    }

    protected final boolean hasTireBeenMethodInjected() {
        return methodInjection != NEVER_INJECTED;
    }

    protected static boolean hasBeenStaticFieldInjected() {
        return staticFieldInjection != NEVER_INJECTED;
    }

    protected static boolean hasBeenStaticMethodInjected() {
        return staticMethodInjection != NEVER_INJECTED;
    }

    protected boolean hasSpareTireBeenMethodInjected() {
        return false;
    }

    boolean packagePrivateMethod2Injected;

    @Inject void injectPackagePrivateMethod2() {
        packagePrivateMethod2Injected = true;
    }

    public boolean packagePrivateMethod3Injected;

    @Inject
    void injectPackagePrivateMethod3() {
        packagePrivateMethod3Injected = true;
    }

    public boolean packagePrivateMethod4Injected;

    void injectPackagePrivateMethod4() {
        packagePrivateMethod4Injected = true;
    }
}

