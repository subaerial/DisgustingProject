package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 * 接龙-->个人订单统计
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/2/19 10:00 AM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class DragonOrderCountResponse extends BaseResponse{

    List<DragonOrderCount> dragonOrderCounts;

}
