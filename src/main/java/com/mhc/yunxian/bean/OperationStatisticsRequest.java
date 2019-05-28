package com.mhc.yunxian.bean;
import lombok.Data;

import java.util.Date;

@Data
public class OperationStatisticsRequest extends BaseRequest{

    private String year;

    private String month;

    private String day;

    private String buyerName;

    private String sellerName;


    private Date beginDate;

    private Date endDate;
}
