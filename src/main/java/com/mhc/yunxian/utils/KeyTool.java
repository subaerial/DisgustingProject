package com.mhc.yunxian.utils;

import org.apache.commons.lang3.RandomUtils;

import java.util.Date;
import java.util.Random;

/**
 *
 * 生成密钥
 * Created by zhangyingdong on 2018/05/07.
 */
public final class KeyTool {

    private KeyTool(){}

    public static String createOrderNo() {
        return new StringBuilder(DateUtils.convertDate2String(new Date(), "yyMMddHHmmssSSS"))
                .append(RandomUtils.nextInt(10000, 99999)).toString();
    }



}
