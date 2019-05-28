package com.mhc.yunxian.enums;

import lombok.Getter;

/**
 * 日期枚举
 */
public enum WeekEnum {

    SUNDAY(1, "周日"),
    MONDAY(2, "周一"),
    TUESDAY(3, "周二"),
    WEDNESDAY(4, "周三"),
    THURSDAY(5, "周四"),
    FRIDAY(6, "周五"),
    SATURDAY(7, "周六");


    @Getter
    private Integer num;
    @Getter
    private String day;

    WeekEnum(Integer num, String day) {
        this.num = num;
        this.day = day;
    }

    /**
     * 根据num值获取周几
     *
     * @param num
     * @return
     */
    public static String getDayByNum(Integer num) {
        if (null == num || num < SUNDAY.num || num > SATURDAY.num) {
            return "";
        }
        for (WeekEnum week : WeekEnum.values()) {
            if (week.getNum().equals(num)) {
                return week.getDay();
            }
        }
        return "";
    }
}
