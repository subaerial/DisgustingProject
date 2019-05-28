package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.request.param.LogisticsRequestParam;
import com.mhc.yunxian.bean.request.query.BaseQuery;
import com.mhc.yunxian.dao.model.Logistics;

/**
 * Created by Administrator on 2018/12/5.
 * @author Alin
 */
public interface LogisticsService {
	/**
	 * 获取物流服务商列表
	 */
	BaseResponse queryLogisticsCompany(BaseQuery query);
	/**
	 * 获取物流信息
	 */
	Logistics getLogisticsInfoByOrderNum(String orderNum);
	/**
	 * 添加物流信息
	 */
	BaseResponse addLogisticsInfo(LogisticsRequestParam param);

}
