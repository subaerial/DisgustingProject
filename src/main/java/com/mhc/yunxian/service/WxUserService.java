package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.OperationStatisticsRequest;
import com.mhc.yunxian.bean.WxLoginRequest;
import com.mhc.yunxian.bean.admin.FindWxUserRequest;
import com.mhc.yunxian.dao.model.WxUnion;
import com.mhc.yunxian.dao.model.WxUser;

import java.util.List;

public interface WxUserService {


	WxUser getWxUser(String openid);

	WxUser getWxUserById(Integer uid);

	boolean saveUser(WxUser wxUser);


	String getWxAccessToKenInfo();

	String getWxGZHAccessToken();

	boolean updateUser(WxUser wxUser);

	List<WxUser> getAllUser(FindWxUserRequest request);

	WxUser selectByOpenid(String openid);

	//根据sessionId查询用户
	WxUser getUserBySessionId(String sessionId);

	boolean updateUserBySessionId(WxUser wxUser);

	boolean updateUserByOpenid(WxUser wxUser);

	int countRegister(OperationStatisticsRequest request);

	WxUser selectByUnionid(String unionid);

	WxUnion findWxUnionByUnionId(String unionid);

	WxUnion findWxUnionByOpenId(String fromUserName);

	/**
	 * 根据时间段统计用户
	 * @param request
	 * @return
	 */
	List<WxUser> getUserByDate(OperationStatisticsRequest request);

}
