package com.mhc.yunxian.enums;

/**
 * 商品限购状态，暂时没用到
 *
 * @Author MoXiaoFan
 * @Date 2019/2/26 14:03
 */
public enum DragonLimitEnum {

    /**
     * 0：不限购
     */
    NO_LIMIT(0, "不限购"),
    /**
     * 1：全局限购
     */
    GLOBAL_LIMIT(1, "全局限购"),
    /**
     * 2：对指定商品进行限购
     */
    GOODS_LIMIT(2, "商品限购");

    Integer status;
    String message;

    DragonLimitEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
