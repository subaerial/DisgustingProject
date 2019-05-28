package com.mhc.yunxian.bean;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class MyUserIndexResponse extends BaseResponse {

    private int totalNumber;

    private int todayNumber;

    private int weekNumber;

    private int monthNumber;

    private int yearNumber;

    private int lastYearNumber;

    private int lastMonthNumber;

    private int lastWeekNumber;

    private int lastDayNumber;

}
