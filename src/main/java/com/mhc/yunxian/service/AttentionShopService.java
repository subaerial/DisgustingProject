package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.request.param.ShopInfoRequestParam;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.dao.model.AttentionShop;

import java.util.List;

/**
 * Created by Administrator on 2018/12/9.
 *
 * @author Alin
 */
public interface AttentionShopService {

    /**
     * 关注店铺,若之前已关注则取消关注
     */
    BaseResponse attentionShop(ShopInfoRequestParam param);

    /**
     * 我关注的店铺
     */
    BaseResponse myAttentionShop(ShopInfoQuery query);

    /**
     * 浏览店铺(买家)
     *
     * @param query
     * @return
     */
    BaseResponse browseShop(ShopInfoQuery query);

    /**
     * 查询关注的店铺列表
     * @return
     */
    List<AttentionShop> queryAttentionShopList(ShopInfoQuery query);
}
