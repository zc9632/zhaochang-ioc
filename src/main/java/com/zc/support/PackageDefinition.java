package com.zc.support;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zhaochang.
 * @Date 2022/2/26.
 * @desc
 */
@Data
@AllArgsConstructor
public class PackageDefinition {
    /**
     * 包名
     */
    private String packageName;

    /**
     * 是否有效
     */
    private Boolean isValid = true;

    public void invalid(){
        isValid = false;
    }

    public boolean isInvalid() {
        return isValid;
    }
}
