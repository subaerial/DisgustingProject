package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class CheckDrawMoneyResponse extends BaseResponse{

    private Integer realDrawMoney;

    private Integer serviceCharge;

}
