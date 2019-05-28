package com.mhc.yunxian.dao;

import org.apache.ibatis.annotations.Param;

public interface DragonShowDao {

    //获取show
    Integer selectDragonShow(Integer version);

    //更新show
    void updateDragonShow(@Param("isShow") Integer show, @Param("version") Integer version);

    /**
     * 是否显示分享按钮
     */
    Integer selectBtnShow(Integer version);

}
