package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class FindWxUserResponse extends BaseResponse {

    List<FindWxUser> data;

    private long total = 0;


}
