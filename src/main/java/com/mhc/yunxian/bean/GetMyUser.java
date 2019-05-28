package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GetMyUser implements Serializable {

    private String headImg;
    private String nickName;
    private Integer buyTime;
    private Integer totalMoney;
    private String userName;//收货人
    private String phone;
    private String userAddr;

    private String openid;
    /**
     * 新增接龍复购次数
     */
    private Integer repurchaseCount;

    private Date commitTime;

}
