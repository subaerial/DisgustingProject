package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetMyBalanceResponce extends BaseResponse {

    private int balance;

    private Integer dragonButIsOpen;

    private Integer isWhite;

    private int realBalance;

    private int isSeller = 0;
}
