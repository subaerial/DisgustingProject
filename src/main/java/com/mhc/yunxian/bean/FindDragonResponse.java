package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.Comment;
import com.mhc.yunxian.dao.model.DragonAddr;
import com.mhc.yunxian.dao.model.GoodsInfo;
import com.mhc.yunxian.dao.model.WxUser;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class FindDragonResponse extends BaseResponse {

    private String headImg;
    private String nickName;
    private Integer orderNumber;
    private String openid;


    private Date createTime;
    private String dragonTitle;
    private String dragonRemark;
    private String dragonImgs;
    private List<DragonAddr> dragonAddr;
    private String phone;
    /**
     * 送货时间
     */
    private Long sendTime;
    private Integer isCOD;//是否支持货到付款
    private Integer isPayLater;


    private Integer partyNumber;//参与人数
    private Long endTime;
    private List<FindDragon> data;//参与者头像以及购买纪录

    private List<GoodsInfo> goodsList;//接龙商品信息

    private List<CommentInfo> commentList;//评论信息

    private Integer commentNumber;//评论数量

    private int isDelivery;

    private int globalLimit;

    private int userLimit;

    private Integer isCoupon;

    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺复购口碑
     */
    private Integer repurchase;
    /**
     * 显示地址
     */
    private String address;
    /**
     * 正在进行的接龙总数
     */
    private Integer dragonInProcessCount;

    /**
     * 店铺ID  默认值：null
     */
    private Long shopId;

    /**
     * 是否显示分享按钮
     */
    private Integer isShowBtn;


}
