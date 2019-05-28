/* Qimu Co.,Lmt. */
package com.mhc.yunxian.dao;


import com.mhc.yunxian.bean.request.query.BaseQuery;
import com.mhc.yunxian.dao.model.LogisticsCompany;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 本文件由 mybatis-generator 自动生成
 *
 * @author
 */
@Repository
public interface LogisticsCompanyMapper {
	/**
	 * 根据主键删除数据库的记录
	 *
	 * @param logisticsCompanyId
	 */
	int deleteByPrimaryKey(Long logisticsCompanyId);

	/**
	 * 新写入数据库记录
	 *
	 * @param record
	 */
	int insert(LogisticsCompany record);

	/**
	 * 动态字段,写入数据库记录
	 *
	 * @param record
	 */
	int insertSelective(LogisticsCompany record);

	/**
	 * 根据指定主键获取一条数据库记录
	 *
	 * @param logisticsCompanyId
	 */
	LogisticsCompany selectByPrimaryKey(Long logisticsCompanyId);

	/**
	 * 动态字段,根据主键来更新符合条件的数据库记录
	 *
	 * @param record
	 */
	int updateByPrimaryKeySelective(LogisticsCompany record);

	/**
	 * 根据主键来更新符合条件的数据库记录
	 *
	 * @param record
	 */
	int updateByPrimaryKey(LogisticsCompany record);

	/**
	 * 查询支持的物流公司
	 *
	 * @param query
	 * @return
	 */
	List<LogisticsCompany> queryLogisticsCompany(BaseQuery query);
}