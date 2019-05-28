package com.mhc.yunxian.bean;
import lombok.Data;

@Data
public class AddDifOrderRequest extends BaseRequest {

    /**
     * 父订单好
     */
    private String parentOrderNum;

    /**
     * 差价金额
     */
    private Integer price;

    /**
     * 0 补差价  4 退差价
     */
    private Integer type;

    /**
     * 订单备注
     */
    private String orderRemark;

    private String sellerDesc;

    /**
     * 子订单状态：1：退差、2：补差
     */
    private Integer orderType;

}
