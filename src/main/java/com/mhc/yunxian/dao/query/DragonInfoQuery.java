package com.mhc.yunxian.dao.query;

import lombok.Data;

@Data
public class DragonInfoQuery extends BaseQuery {
    /**
     * 创建者openid
     */
    private String openid;
    /**
     * 接龙状态  0:进行中，1:已结束
     */
    private Integer dragonStatus;
}
