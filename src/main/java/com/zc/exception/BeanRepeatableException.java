package com.zc.exception;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaochang.
 * @Date 2022/3/5.
 * @desc
 */
@Data
@Slf4j
public class BeanRepeatableException extends RuntimeException{

    public BeanRepeatableException(String message) {
        super(message);
    }
}
