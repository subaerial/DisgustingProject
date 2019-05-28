package com.mhc.yunxian.enums;

/**
 * Created by Administrator on 2018/12/10.
 */
public enum ShopEnum {

    /**
     * 店铺营业状态
     */
    SHOP_STATUS_OPEN(0, "开业中"),
    SHOP_STATUS_CLOSE(1, "已关闭"),

    /**
     * 已关注(关注)
     */
    ALREADY_ATTENTION(0, "已关注(关注)"),
    /**
     * 已取消关注
     */
    CANCEL_ATTENTION(1, "已取消关注"),
    ;
    Integer code;
    String description;

    ShopEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
