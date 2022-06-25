package com.zc.test.circulardependency;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.annotation.Provider;

/**
 * @author zhaochang.
 * @Date 2022/6/20.
 * @desc
 */
@Named
public class TestObject {
    @Inject
    private Provider<ProviderObject> providerObject;

    @Inject
    private SingletonObject singletonObject;

    @Inject
    private CDObject cdObject;
}
