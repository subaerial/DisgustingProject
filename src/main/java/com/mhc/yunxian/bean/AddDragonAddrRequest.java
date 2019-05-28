package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class AddDragonAddrRequest extends BaseRequest{

    private String sessionId;

    /**
     * 接龙地址id
     */
    private Integer dragonAddId;

    private String name;

    private String addr;

    private String longitude;

    private String latitude;

    private String openid;

    private String detailAddr;

    private String deleted;

}
