package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class WithdrawMoneyResponse  extends BaseResponse{

    private List<WithdrawMoney> data;

    private long total = 0;

}
