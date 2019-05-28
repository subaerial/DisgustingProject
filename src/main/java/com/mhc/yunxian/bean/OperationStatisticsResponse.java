package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class OperationStatisticsResponse extends BaseResponse {

    private Integer orderNumber;

    private Integer orderMoney;

    private Integer registerNumber;

    private Integer sellerNumber;

    /**
     * 本周订单数量
     */
    private Integer orderCountOfWeek;
    /**
     * 上周订单数量
     */
    private Integer orderCountOfLastWeek;
    /**
     * 本周销售额
     */
    private Integer salesAmountOfWeek;

    /**
     * 上周销售额
     */
    private Integer salesAmountOfLastWeek;

    /**
     * 本周注册人数
     */
    private Integer registerCountOfWeek;

    /**
     * 上周注册人数
     */
    private Integer registerCountOfLastWeek;

    /**
     * 本周卖家人数
     */
    private Integer sellerCountOfWeek;

    /**
     * 上周卖家人数
     */
    private Integer sellerCountOfLastWeek;

}
