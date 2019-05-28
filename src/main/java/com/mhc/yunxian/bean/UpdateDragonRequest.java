package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class UpdateDragonRequest extends BaseRequest {


    private String openid;
    private String sessionId;
    private String title;
    private String remark;
    private String dragonImage;
    private Long endTime;
    /**
     * 是否货到付款
     */
    private Integer isCOD;
    private Long sendTime;
    private List<Integer> dragonAddrIds;
    /**
     * 接龙号
     */
    private String dragonNum;
    private String phone;
    private Integer isPayLater;
    private int isDelivery;
    private int globalLimit;

    List<Goods> data;
    /**
     * 接龙视频
     */
    private String dragonVideo;
    /**
     * 截单时间
     */
    private String cutOffTime;
    /**
     * 发货周期对象
     */
    private DeliveryCycle deliveryCycle;

}
