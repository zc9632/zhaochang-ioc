package com.zc.test.bean;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.annotation.Singleton;

/**
 * @author zhaochang.
 * @Date 2022/3/6.
 * @desc
 */
@Singleton
public class Action {
    @Inject
    private Student student;
}
