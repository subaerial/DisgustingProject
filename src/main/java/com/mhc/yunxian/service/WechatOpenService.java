package com.mhc.yunxian.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import javax.servlet.http.HttpServletRequest;

/**
 * 微信OAuth2.0接口封装
 * Created by zhangyingdong on 2018/05/02.
 */
public interface WechatOpenService {


    @GET("sns/jscode2session")
    Call<Js2SessionInfo> js2session(@Query("appid") String appid,
                                    @Query("secret") String secret,
                                    @Query("js_code") String code,
                                    @Query("grant_type") String grantType);

    @GET("sns/oauth2/access_token")
    Call<OauthAccessToken> oauthAccessToken(@Query("appid") String appid,
                                    @Query("secret") String secret,
                                    @Query("code") String code,
                                    @Query("grant_type") String grantType);

    @GET("sns/userinfo")
    Call<UserInfo> userInfo(@Query("access_token") String accessToken,
                                            @Query("openid") String openid,
                                            @Query("lang") String lang);

    @GET("cgi-bin/token")
    Call<AccessTokenInfo> accessToken(@Query("appid") String appid,
                                      @Query("secret") String secret,
                                      @Query("grant_type") String grantType);


    @POST("wxa/getwxacodeunlimit")
    Call<String> getWxacode(@Query("access_token") String accessToken,
                            @Query("scene") String scene,
                            @Query("page") String page);



    @Getter
    @Setter
    @ToString
    class UserInfo {
        private String openid;
        private String nickname;
        private String sex;
        private String province;
        private String city;
        private String country;
        private String headimgurl;

        @JsonIgnore
        private String privilege;

        private String language;
        private String unionid;
        private Integer errcode;
        private String errmsg;

    }



    @Getter
    @Setter
    @ToString
    class Js2SessionInfo {
        private String openid;
        private String session_key;
        private String unionid;
        private Integer errcode;
        private String errmsg;

    }

    @Getter
    @Setter
    @ToString
    class OauthAccessToken {
        private String access_token;
        private Integer expires_in;
        private String refresh_token;
        private String unionid;
        private String openid;
        private String scope;
        private Integer errcode;
        private String errmsg;

    }


    @Getter
    @Setter
    @ToString
    class AccessTokenInfo {
        private String access_token;
        private Integer expires_in;
        private Integer errcode;
        private String errmsg;

    }
}
