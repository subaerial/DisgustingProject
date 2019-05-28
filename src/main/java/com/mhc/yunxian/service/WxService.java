package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.TransfersReponse;
import com.mhc.yunxian.dao.model.WxAccessToken;
import com.mhc.yunxian.dao.model.WxAdmin;
import com.mhc.yunxian.dao.model.WxAutoReply;
import com.mhc.yunxian.dao.model.WxUnion;

import java.util.List;
import java.util.Map;

public interface WxService {

    WxAccessToken getWxAccessToken();

    int insertWxAccessToken(WxAccessToken wxAccessToken);

    int updateWxAccessToken(WxAccessToken wxAccessToken);

    void autoReply(Map<String,String> map);

    List<WxAutoReply> getKeywordList();

    WxAutoReply selectByKeyword(String keyword);

    WxAutoReply selectById(Integer id);

    int insertWxAutoReply(WxAutoReply wxAutoReply);

    int deleteWxAutoReply(Integer id);

    WxAdmin getWxAdmin(String openid);

    int updateWxAdmin(WxAdmin wxAdmin);

    int addWxAdmin(WxAdmin wxAdmin);

    TransfersReponse payToUserWx(String partnerTradeNo, String openid, Integer amount, String ip);

    List<WxAdmin> selectAdmin();

    void saveWxInfo(WxUnion wxUnion);

    void updateWxInfo(WxUnion wxUnion);

    void updateWxUnionById(WxUnion wxUnion);

}
