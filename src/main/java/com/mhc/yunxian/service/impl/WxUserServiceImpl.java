package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.OperationStatisticsRequest;
import com.mhc.yunxian.bean.admin.FindWxUserRequest;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.dao.WxUnionDao;
import com.mhc.yunxian.dao.WxUserDao;
import com.mhc.yunxian.dao.model.WxUnion;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.WechatOpenService;
import com.mhc.yunxian.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class WxUserServiceImpl implements WxUserService {


    @Autowired
    RemoteCache redisCache;

    @Autowired
    WxUserDao wxUserDao;

    @Value("${wx.yunxian.appid}")
    String wxAppid;

    @Value("${wx.yunxian.secret}")
    String wxSecret;

    @Value("${wx.admin.appid}")
    String wxAdminAppid;

    @Value("${wx.admin.secret}")
    String wxAdminSecret;

    @Autowired
    WechatOpenService wechatOpenService;

    @Autowired
    private WxUnionDao wxUnionDao;


    @Override
    //@Cacheable(value = RedisKeys.WXUSER_KEY,key = "'yunxian:wxuser:id:'+#openid",sync = true)
    public WxUser getWxUser(String openid) {
        return wxUserDao.selectByOpenid(openid);
    }

    @Override
    public WxUser getWxUserById(Integer uid) {
        return wxUserDao.selectByPrimaryKey(uid);
    }

    @Override
    public boolean saveUser(WxUser wxUser) {


        return wxUserDao.insertSelective(wxUser) > 0;

    }

    /**
     * 获取服务通知accessToken
     *
     * @return
     */
    @Override
    public String getWxAccessToKenInfo() {

        String accessToken = redisCache.get("yunxian:accesstokeninfo");

        if (StringUtils.isBlank(accessToken)) {
            try {
                WechatOpenService.AccessTokenInfo accessTokenInfo = wechatOpenService.accessToken(wxAppid, wxSecret, "client_credential").execute().body();
                accessToken = accessTokenInfo.getAccess_token();
                log.info("get access_token:{}", accessToken);
                redisCache.set("yunxian:accesstokeninfo", accessTokenInfo.getAccess_token(), accessTokenInfo.getExpires_in() - 10, TimeUnit.SECONDS);
                return accessToken;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("获取accessToKenInfo失败:{}", e.getMessage());
            }
        } else {
            log.info("get access token from cache:{}", accessToken);

        }

        return accessToken;
    }

    /**
     * 获取公众号accessToken
     *
     * @return
     */
    @Override
    public String getWxGZHAccessToken() {
        String accessToken = redisCache.get("yunxianGZH:accesstoken");

        if (StringUtils.isBlank(accessToken)) {
            try {
                WechatOpenService.AccessTokenInfo accessTokenInfo = wechatOpenService.accessToken(wxAdminAppid, wxAdminSecret, "client_credential").execute().body();
                accessToken = accessTokenInfo.getAccess_token();
                log.info("get wxGZH access_token:{}", accessToken);
                redisCache.set("yunxianGZH:accesstoken", accessTokenInfo.getAccess_token(), accessTokenInfo.getExpires_in() - 10, TimeUnit.SECONDS);
                return accessToken;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("获取公众号accessToken失败:{}", e);
            }
        } else {
            log.info("get wxGZH access token from cache:{}", accessToken);

        }

        return accessToken;
    }

    @Override
    public boolean updateUser(WxUser wxUser) {

        return wxUserDao.updateUserByOpenid(wxUser) > 0;
    }

    @Override
    public List<WxUser> getAllUser(FindWxUserRequest request) {
        return wxUserDao.selectAllUser(request);
    }

    @Override
    public WxUser selectByOpenid(String openid) {
        return wxUserDao.selectByOpenid(openid);
    }

    @Override
    public WxUser getUserBySessionId(String sessionId) {
        return wxUserDao.getUserBySessionId(sessionId);
    }

    @Override
    public boolean updateUserBySessionId(WxUser wxUser) {
        if (wxUserDao.updateUserBySessionId(wxUser) > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUserByOpenid(WxUser wxUser) {
        return wxUserDao.updateUserByOpenid(wxUser) > 0;
    }

    @Override
    public int countRegister(OperationStatisticsRequest request) {
        return wxUserDao.countRegister(request);
    }

    @Override
    public WxUser selectByUnionid(String unionid) {
        return wxUserDao.selectByUnionid(unionid);
    }

    @Override
    public WxUnion findWxUnionByUnionId(String unionid) {
        if (StringUtils.isBlank(unionid)) {
            throw new DataException("unionId为空");
        }
        WxUnion wxUnion = wxUnionDao.findByUnionId(unionid);
        return wxUnion;
    }

    @Override
    public WxUnion findWxUnionByOpenId(String fromUserName) {
        return wxUnionDao.findByOpneId(fromUserName);
    }

    @Override
    public List<WxUser> getUserByDate(OperationStatisticsRequest request) {
        return wxUserDao.getUserByDate(request);
    }

}
