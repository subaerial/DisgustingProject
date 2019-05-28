package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.WxUser;
import lombok.Data;

@Data
public class UserInfoResponse extends BaseResponse {

    private WxUser wxUser;

}
