package com.mhc.yunxian.bean.admin;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FindWxUser implements Serializable {

    private String openid;
    private String phone;
    private String nickName;
    private Integer userStatus;
    private Integer money;//余额
    private Integer isWhite;//是否白名单

    private Date regTime;
    private Date lastLoginTime;

    private Integer myUserOrderNumber;//我的用户的订单数量

    private Integer myOrderNumber;//我下单的数量


}
