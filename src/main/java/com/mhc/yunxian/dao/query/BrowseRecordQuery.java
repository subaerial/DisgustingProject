package com.mhc.yunxian.dao.query;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class BrowseRecordQuery extends BaseQuery {

    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 接龙编号
     */
    private String dragonNum;
    /**
     * 商家openId
     */
    private String sellerOpenid;

}
