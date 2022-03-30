package com.zc.test.bean;

import com.zc.annotation.Inject;
import com.zc.annotation.Named;
import com.zc.annotation.Provider;
import com.zc.annotation.Singleton;
import lombok.Data;

/**
 * @author zhaochang.
 * @Date 2022/2/20.
 * @desc
 */
@Named
@Singleton
@Data
public class Student {

    @Inject
    private Action action;

    @Inject
    private Provider<Action> actionProvider;

    public Action getAction(){
        return this.action;
    }

    public Action setAction(Action action){
        return this.action = action;
    }

}
