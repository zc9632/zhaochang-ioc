package com.zc.test.circulardependency;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;

/**
 * @author zhaochang.
 * @Date 2022/6/20.
 * @desc
 */
@Named
public class CDObject {
    @Inject
    private TestObject testObject;
}
