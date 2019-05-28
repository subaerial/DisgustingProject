package com.mhc.yunxian.bean.request.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by Administrator on 2018/12/12.
 *
 * @author Alin
 */
@ApiModel
@Data
public class LogisticsRequestParam extends BaseRequestParam {
    /**
     * 物流公司id
     */
    private Long logisticsCompanyId;
    /**
     * 物流公司名称
     */
    private String logisticsCompanyName;

    /***
     * 物流信息
     */
    private String logisticsContent;
    /***
     * 物流编号
     */
    private String logisticsCode;

    /**
     * 用户openId
     */
    private String userOpenId;
    /***
     * 订单编号
     */
    private String orderNum;

    /**
     * 接龙编号
     */
    private String dragonNum;


}
