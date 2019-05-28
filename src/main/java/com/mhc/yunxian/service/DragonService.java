package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.index.GetIndexDragonListRequest;
import com.mhc.yunxian.bean.index.GetIndexDragonListResponse;
import com.mhc.yunxian.bean.request.param.DragonDetailRequestParam;
import com.mhc.yunxian.bean.response.DragonDetailRepurchaseVO;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.dao.query.DragonInfoQuery;
import com.mhc.yunxian.enums.DragonDateTypeEnum;

import java.util.Date;
import java.util.List;

public interface DragonService {


    List<DragonInfo> getAllDragon(Integer pageNum);

    List<DragonInfo> getAllDragonInfo();

    void addInfoV3(CreateDragonRequest request);

    DragonInfo getDragon(String dragonNum);

    DragonInfo getParentDragon(String dragonNum);

    List<DragonInfo> getDragonByOpenid(String openid);

    void updateDragonV3(UpdateDragonRequest request);

    boolean updateDragonStatus(DragonInfo info);

    boolean updateDragonEndTime(UpdateDragonRequest request);

    List<Banner> getBroadcastImg();

    List<DragonInfo> getAllDragonWithEndTime();

    List<DragonInfo> selectDragonByStatusAndOpenid(DragonInfo dragonInfo);

    void addDragonBrowseRecord(BrowseRecord browseRecord);

    List<BrowseRecord> findTodayRecord(BrowseRecord browseRecord);

    boolean updateBrowseRecord(BrowseRecord browseRecord);


    int countSellerNumByTime(OperationStatisticsRequest request);

    GetIndexDragonListResponse getAllDragonInfoV7(GetIndexDragonListRequest request);

    void updateDraft(DragonDraft dragonDraft);

    DragonDraft getDraft(String openid);

    void deleteDraft(String openid);

    List<DragonAddr> getAllDragonAddr(DragonAddrRequest request);

    List<DragonAddr> selectByDragonNum(String dragonNum);

    void addDragonAddr(AddDragonAddrRequest request);

    List<DragonDetailRepurchaseVO> dragonDetailRepurchaseInfo(DragonDetailRequestParam param);

    BaseResponse updownDragon(String dragonNum, String sessionId, Integer state);

    List<String> selectLatestThreeShopOfDragon();

    /**
     * 获取周期性的发货最近一期发货或截单时间
     *
     * @param cycle              周期性发货对象
     * @param dragonDateTypeEnum 发货或截单枚举
     * @return 最近一期发货或截单时间
     */
    Date getDragonDeliveryCycleDate(String cycle, DragonDateTypeEnum dragonDateTypeEnum);

    /**
     * 获取本周的截单或发货时间列表
     *
     * @param hour      截单或发货:小时
     * @param dayOfWeek 每周几整型数组 例如 1 表示周日 7 表示周日
     * @return 返回本周的世界列表
     */
    List<Date> getDateList(Integer hour, Integer[] dayOfWeek, Integer type);

    /**
     * 获取最近一期的时间
     *
     * @param dateList
     * @return
     */
    Date getDate(List<Date> dateList);

    /**
     * 获取每周发货时间的字符串表示
     *
     * @param cycle
     * @return
     */
    String getDragonDeliveryTime(String cycle);

    /**
     * 参与接龙人数及信息
     *
     * @return
     */
    List<FindDragon> getPartDragonUser(String dragonNum, Date cutOffTime);

    /**
     * 分页查询最新的进行中的接龙
     *
     * @param dragonInfoQuery
     * @return
     */
    List<DragonInfo> queryLatestDragon(DragonInfoQuery dragonInfoQuery);

    /**
     * 获取在进行中的周期行发货的接龙
     * @return
     */
    List<DragonInfo> selectDragonByStatusAndCycle();

    /**
     * 接龙下个人订单统计
     * @param dragonNum
     * @return
     */
    List<DragonOrderCount> queryDragonOrderCount(String dragonNum);
}
