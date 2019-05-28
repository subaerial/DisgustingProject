package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.DragonAddr;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class Dragon implements Serializable {

    private String dragonNum;
    private String title;
    private String remark;
    private String dragonImg;
    private Integer partNumber;
    private String nickName;
    private String head;
    private Date createTime;
    private Long endTime;
    private List<DragonAddr> dragonAddrs;
    List<String> headImg;//参与接龙用户的头像

    private Integer orderNumber;
    private int isDelivery;


}
