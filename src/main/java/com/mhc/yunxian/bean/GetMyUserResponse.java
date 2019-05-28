package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class GetMyUserResponse extends BaseResponse {


    List<GetMyUser> data;
}
