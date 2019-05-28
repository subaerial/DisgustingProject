package com.mhc.yunxian.enums;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * 枚举信息
 *
 * @author xuzhongming
 * @version 1.0 18/8/06
 * @since 1.0
 */
public interface EnumMsg<T> extends EnumCode<T> {

    /**
     * 获取枚举信息.
     *
     * @return 枚举信息
     */
    public String getMsg();

    /**
     * 格式化枚举描述信息.
     *
     * @param formatArgs 格式化参数
     * @return 枚举描述信息
     */
    default String getMsg(Object... formatArgs) {
        String msg = getMsg();
        if (ArrayUtils.isEmpty(formatArgs) || StringUtils.isBlank(msg)) {
            return msg;
        }

        return MessageFormat.format(msg, formatArgs);
    }
}
