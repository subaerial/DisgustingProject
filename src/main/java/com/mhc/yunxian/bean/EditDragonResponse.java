package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.DragonAddr;
import com.mhc.yunxian.dao.model.GoodsInfo;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class EditDragonResponse extends BaseResponse{

    private String headImg;
    private String nickName;
    private String sessionId;


    private Date createTime;
    private String dragonTitle;
    private String dragonRemark;
    private String dragonImgs;
    private List<DragonAddr> dragonAddr;
    private String phone;
    private Long sendTime;
    private Integer isCOD;//是否支持货到付款
    private Integer isPayLater;
    private Long endTime;
    private List<GoodsInfo> goodsList;//接龙商品信息
    private int isDelivery;
    private int globalLimit;
    private Integer isCoupon;//能否使用电子卷

    /**
     * 接龙视频
     */
    private String dragonVideo;
    /**
     * 接龙的发货对象
     */
    private DeliveryCycle deliveryCycle;

    private Integer hasBuy;

}
