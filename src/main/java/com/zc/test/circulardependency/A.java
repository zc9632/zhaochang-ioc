package com.zc.test.circulardependency;


import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.annotation.Provider;

/**
 * @author zhaochang.
 * @Date 2022/3/31.
 * @desc
 */
@Named
public class A {
	@Inject
	Provider<ProviderA> aProvider;
}
