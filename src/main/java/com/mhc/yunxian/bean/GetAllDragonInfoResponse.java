package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class GetAllDragonInfoResponse extends BaseResponse {

    List<Dragon> data;

    private Integer totalNumber;
    private Integer totalPartyNumber;

    private Integer orderNumber;

    private String head;
    private String nickName;


    private String openid;

}
