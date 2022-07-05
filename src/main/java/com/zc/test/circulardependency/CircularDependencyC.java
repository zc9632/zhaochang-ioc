package com.zc.test.circulardependency;


import com.zc.annotation.Inject;
import com.zc.annotation.Named;

/**
 * @author zhaochang.
 * @Date 2022/3/31.
 * @desc
 */
@Named
public class CircularDependencyC {
	@Inject
    C c;
}
