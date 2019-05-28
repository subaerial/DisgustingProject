package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class OrderStatisticsResponse extends BaseResponse {

    private int todayNumber;

    private int todayMoney;

    private int weekNumber;

    private int weekMoney;

    private int totalNumber;

    private int totalMoney;

    private int monthMoney;

    private int monthNumber;

}
