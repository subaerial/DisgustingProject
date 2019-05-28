package com.mhc.yunxian.bean.index;

import com.mhc.yunxian.bean.DeliveryCycle;
import com.mhc.yunxian.dao.model.DragonAddr;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DragonInfoVO {

    private String dragonImg;

    private String dragonNum;

    private List<String> headImg;

    private String remark;
    /**
     * 标题
     */
    private String title;
    /**
     * 当前接龙参与人数
     */
    private Integer partyNumber;

    private Date createTime;

    private Integer orderNumber;

    private List<DragonAddr> dragonAddrs;

    private String endTime;

    /**
     * 发货时间
     */
    private String sendTime;

    /**
     * 截单时间
     */
    private String cutOffTime;
    /**
     * 发货周期
     */
    private DeliveryCycle deliveryCycle;

    private Integer isDelivery;

    /**
     * 浏览时间
     */
    private Date browseTime;
}
