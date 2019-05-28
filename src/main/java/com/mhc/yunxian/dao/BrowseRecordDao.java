package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.BrowseRecord;
import com.mhc.yunxian.dao.query.BrowseRecordQuery;

import java.util.List;

public interface BrowseRecordDao {
	int deleteByPrimaryKey(Integer id);

	int insert(BrowseRecord record);

	int insertSelective(BrowseRecord record);

	BrowseRecord selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(BrowseRecord record);

	int updateByPrimaryKey(BrowseRecord record);

	List<BrowseRecord> findTodayRecord(BrowseRecord browseRecord);

	List<BrowseRecord> findBrowseRecord(BrowseRecord browseRecord);

	/**
	 * 分页查询最新浏览记录
	 * @param browseRecordQuery
	 * @return
	 */
	List<BrowseRecord> findLatestBrowseRecord(BrowseRecordQuery browseRecordQuery);


	List<String> selectSellerOpenid(int userId);
}