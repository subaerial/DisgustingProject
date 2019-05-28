package com.mhc.yunxian.bean;

import com.mhc.yunxian.bean.index.DragonInfoVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/12/13.
 */
@Data
public class ShopDragonInfo {
    /**
     * 店铺Id
     */
    private Long ShopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店主openId
     */
    private String ShopkeeperOpenId;
    /**
     * 店铺logo
     */
    private String shopHeadPicture;
    /**
     * 店铺的复购口碑
     */
    private Integer repurchaseCount;

    /**
     * 接龙列表
     */
    private List<DragonInfoVO> dragonInfoVOList;

    private Date browseTime;

}