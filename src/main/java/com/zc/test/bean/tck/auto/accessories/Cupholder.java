package com.zc.test.bean.tck.auto.accessories;

import com.zc.annotation.Inject;
import com.zc.annotation.Provider;
import com.zc.annotation.Singleton;
import com.zc.test.bean.tck.auto.Seat;

/**
 * @author zhaochang.
 * @Date 2022/3/27.
 * @desc
 */
@Singleton
public class Cupholder {

    public final Provider<Seat> seatProvider;

    @Inject
    public Cupholder(Provider<Seat> seatProvider) {
        this.seatProvider = seatProvider;
    }
}
