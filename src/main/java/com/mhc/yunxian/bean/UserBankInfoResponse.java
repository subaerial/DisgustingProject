package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.UserBankInfo;
import lombok.Data;

@Data
public class UserBankInfoResponse extends BaseResponse{

    private UserBankInfoVO userBankInfo;

}
