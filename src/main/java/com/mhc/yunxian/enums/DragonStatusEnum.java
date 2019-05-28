package com.mhc.yunxian.enums;

/**
 * Created by Administrator on 2018/12/11.
 */
public enum DragonStatusEnum {
    /**
     * 进行中
     */
    IN_PROGRESS(0, "进行中"),
    /**
     * 已结束
     */
    ALREADY_OVER(1, "已结束"),
    ;
    Integer status;
    String description;

    DragonStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
