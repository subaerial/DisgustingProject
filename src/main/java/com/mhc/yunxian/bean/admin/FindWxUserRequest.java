package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.BaseRequest;
import lombok.Data;

@Data
public class FindWxUserRequest extends BaseRequest {

    private String nickName;

    private int state = 0;


}
