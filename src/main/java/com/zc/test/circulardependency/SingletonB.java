package com.zc.test.circulardependency;


import com.zc.annotation.Inject;
import com.zc.annotation.Singleton;

/**
 * @author zhaochang.
 * @Date 2022/3/31.
 * @desc
 */
@Singleton
public class SingletonB {
	@Inject
    B b;
}
