package com.zc.test.circulardependency;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.annotation.Singleton;

/**
 * @author zhaochang.
 * @Date 2022/6/20.
 * @desc
 */
@Named
@Singleton
public class SingletonObject {
    @Inject
    private TestObject testObject;
}
