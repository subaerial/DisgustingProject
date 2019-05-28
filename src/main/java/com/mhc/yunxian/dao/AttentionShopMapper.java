/* Qimu Co.,Lmt. */
package com.mhc.yunxian.dao;


import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.dao.model.AttentionShop;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 本文件由 mybatis-generator 自动生成
 *
 * @author Qimu Co.,Lmt.
 */
@Repository
public interface AttentionShopMapper {
	/**
	 * 根据主键删除数据库的记录
	 *
	 * @param attentionShopId
	 */
	int deleteByPrimaryKey(Long attentionShopId);

	/**
	 * 新写入数据库记录
	 *
	 * @param record
	 */
	int insert(AttentionShop record);

	/**
	 * 动态字段,写入数据库记录
	 *
	 * @param record
	 */
	int insertSelective(AttentionShop record);

	/**
	 * 根据指定主键获取一条数据库记录
	 *
	 * @param attentionShopId
	 */
	AttentionShop selectByPrimaryKey(Long attentionShopId);

	/**
	 * 动态字段,根据主键来更新符合条件的数据库记录
	 *
	 * @param record
	 */
	int updateByPrimaryKeySelective(AttentionShop record);

	/**
	 * 根据主键来更新符合条件的数据库记录
	 *
	 * @param record
	 */
	int updateByPrimaryKey(AttentionShop record);

	/**
	 * 查询我关注的店铺列表
	 */
	List<AttentionShop> queryAttentionShopList(ShopInfoQuery query);
}