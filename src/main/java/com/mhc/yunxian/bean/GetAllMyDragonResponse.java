package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class GetAllMyDragonResponse extends BaseResponse {

    private Long shopId;
    List<GetAllMyDragon> data;

}
