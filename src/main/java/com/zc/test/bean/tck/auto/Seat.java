package com.zc.test.bean.tck.auto;

import com.zc.annotation.Inject;
import com.zc.annotation.Singleton;
import com.zc.test.bean.tck.auto.accessories.Cupholder;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
@Singleton
public class Seat {

    private final Cupholder cupholder;

    @Inject
    Seat(Cupholder cupholder) {
        this.cupholder = cupholder;
    }

    public Cupholder getCupholder() {
        return cupholder;
    }
}

