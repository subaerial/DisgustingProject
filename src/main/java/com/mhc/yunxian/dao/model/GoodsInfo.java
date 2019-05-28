package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Administrator
 */
@Data
public class GoodsInfo {

    private Integer id;

    /**
     * 商品编号
     */
    private String goodsNum;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private Integer price;

    /**
     * 商品总数量
     */
    private Integer totalNumber;

    /**
     * 商品图片
     */
    private String goodsImg;

    private Date updateTime;

    private Date createTime;
    /**
     * 购买数量,查询订单返回时使用,插入数据可忽略
     */
    private Integer buyNumber;

    private Float buyWeight;

    private Integer limitBuyNum;

    private Integer salesVolume;

    private String specification;

    /**
     * 创建人(卖家)openID
     */
    private String creatorOpenId;

    /**
     * 逻辑删除
     */
    private Boolean deleted;

    /**
     * 商品复购次数
     */
    private Integer repurchaseNum;

    /**
     * 购买次数
     */
    private Integer buyNum;

}