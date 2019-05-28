/* Qimu Co.,Lmt. */
package com.mhc.yunxian.dao;


import com.mhc.yunxian.dao.model.Logistics;
import org.springframework.stereotype.Repository;

/**
 * 本文件由 mybatis-generator 自动生成
 * @author Qimu Co.,Lmt.
 */
@Repository
public interface LogisticsMapper {
    /** 
     * 根据主键删除数据库的记录
     * @param logisticsId
     */
    int deleteByPrimaryKey(Long logisticsId);

    /** 
     * 新写入数据库记录
     * @param record
     */
    int insert(Logistics record);

    /** 
     * 动态字段,写入数据库记录
     * @param record
     */
    int insertSelective(Logistics record);

    /** 
     * 根据指定主键获取一条数据库记录
     * @param logisticsId
     */
    Logistics selectByPrimaryKey(Long logisticsId);

    /** 
     * 动态字段,根据主键来更新符合条件的数据库记录
     * @param record
     */
    int updateByPrimaryKeySelective(Logistics record);

    /** 
     * 根据主键来更新符合条件的数据库记录
     * @param record
     */
    int updateByPrimaryKey(Logistics record);

    /**
     * 根据订单编号查询物流信息
     * @param orderNum
     * @return
     */
    Logistics getLogisticsInfoByOrderNum(String orderNum);
}