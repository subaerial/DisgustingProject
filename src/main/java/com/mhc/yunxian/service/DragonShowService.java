package com.mhc.yunxian.service;

public interface DragonShowService {

    //获取show
    Integer selectDragonShow(Integer version);

    //更新show
    void updateDragonShow(Integer show, Integer version);

    /**
     * 是否显示分享按钮
     */
    Integer selectBtnShow(Integer version);

}

