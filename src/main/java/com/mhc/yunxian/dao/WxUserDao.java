package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.OperationStatisticsRequest;
import com.mhc.yunxian.bean.admin.FindWxUserRequest;
import com.mhc.yunxian.dao.model.WxUser;

import java.util.List;

public interface WxUserDao {
	int deleteByPrimaryKey(Integer id);

	int insertSelective(WxUser record);

	WxUser selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(WxUser record);

	WxUser selectByOpenid(String openid);

	/**
	 * 根据sessionId查询用户
	 */
	WxUser getUserBySessionId(String sessionId);

	/**
	 * 根据sessionId更新用户信息
	 */
	int updateUserBySessionId(WxUser wxUser);

	List<WxUser> selectAllUser(FindWxUserRequest request);

	int updateUserByOpenid(WxUser wxUser);

	int countRegister(OperationStatisticsRequest request);

	List<WxUser> selectByOpenidList(List<String> openidList);

	WxUser selectByUnionid(String unionid);

	List<WxUser> getAllUserForDataSync();

	List<WxUser> getUserByDate(OperationStatisticsRequest request);
}