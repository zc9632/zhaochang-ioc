package com.zc.test.bean;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;

/**
 * @author zhaochang.
 * @Date 2022/2/20.
 * @desc
 */
@Named
public class Student {

    @Inject
    private Action action;

    public Action getAction(){
        return this.action;
    }

    public Action setAction(Action action){
        return this.action = action;
    }

}
