package com.zc.test.bean.tck.auto;

import com.zc.annotation.Inject;
import com.zc.test.bean.tck.auto.accessories.Cupholder;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
public class DriversSeat extends Seat {

    @Inject
    public DriversSeat(Cupholder cupholder) {
        super(cupholder);
    }
}
