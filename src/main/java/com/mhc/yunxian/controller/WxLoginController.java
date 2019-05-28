package com.mhc.yunxian.controller;

import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.model.Balance;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.service.BalanceService;
import com.mhc.yunxian.service.WechatOpenService;
import com.mhc.yunxian.service.WxUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 微信登录
 * <p>
 * Created by huzichi on 2018/1/15.
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/")
@EnableSwagger2
@Api(tags = "wxLogin", description = "微信登录")
public class WxLoginController extends HttpClientController {

    @Autowired
    WechatOpenService wechatOpenService;
    @Autowired
    BalanceService balanceService;
    @Value("${wx.yunxian.appid}")
    String wxAppid;
    @Value("${wx.yunxian.secret}")
    String wxSecret;

    @Autowired
    WxUserService userService;

    @ApiOperation(value = "微信小程序授权", notes = "")
    @RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST})
    public WxAuthResponse auth(@RequestBody BaseRequest baseRequest) throws IOException, ParseException {

        log.info("auth,code:{}", baseRequest.getCode());

        final WxAuthResponse authResponse = new WxAuthResponse();

        final WechatOpenService.Js2SessionInfo js2SessionInfo = wechatOpenService.js2session(wxAppid, wxSecret,
                baseRequest.getCode(), "authorization_code").execute().body();
        log.info("code换取session_key, code:{},return:{}", baseRequest.getCode(), js2SessionInfo != null ? js2SessionInfo.toString() : "");

        if (js2SessionInfo.getErrcode() != null) {
                log.error("code换取session_key失败,code:{},msg:{}", js2SessionInfo.getErrcode(), js2SessionInfo.getErrmsg());
                return (WxAuthResponse) authResponse.build(RespStatus.WX_GETOPENID_ERROR);
        }
        WxUser wxUser = new WxUser();
        //从响应信息中获取openid
        wxUser.setOpenid(js2SessionInfo.getOpenid());
        //生成sessionId
        String sessionId=UUID.randomUUID().toString().replaceAll("-","");
        wxUser.setSessionId(sessionId);
//        设置sessionId过期时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(sdf.format(new Date(System.currentTimeMillis()+1000*60*60*24*2)));

        wxUser.setExpirationTime(date);

        log.info("************SessionId过期时间："+wxUser.getExpirationTime());

        WxUser user = userService.getWxUser(wxUser.getOpenid());
        if (null==user){
            //不存在,说明是第一次登陆
            wxUser.setCreateTime(new Date());
            //设置用户默认数据
            wxUser.setCity("杭州");
            wxUser.setCountry("中国");
            wxUser.setLanguage("zh_CN");
            wxUser.setHeadImgUrl("0");
            wxUser.setNickName("云鲜_"+wxUser.getOpenid().substring(0,6));
            wxUser.setPhone("0");
            wxUser.setUserStatus(0);
            wxUser.setUnionid(js2SessionInfo.getUnionid());
            //保存openid和sessionId
            boolean flag = userService.saveUser(wxUser);
            if(flag){
                log.info("新增用户成功!");
                //初始化账户余额
                Balance balance=new Balance();
                balance.setBalance(0);
                balance.setCreateTime(new Date());
                balance.setOpenid(wxUser.getOpenid());
                if(balanceService.insertBalance(balance) < 1){
                    return (WxAuthResponse) authResponse.build(RespStatus.SYSTEM_ERROR);
                }
            }else{
                log.info("新增用户失败!");
            }

        }else{
            //更新sessionId和sessionId过期时间
            log.info("*****openid*******:"+wxUser.getOpenid());

            //保存用户unionId信息
            if (StringUtils.isNotBlank(js2SessionInfo.getUnionid()) && StringUtils.isBlank(user.getUnionid())){
                wxUser.setUnionid(js2SessionInfo.getUnionid());
            }
            if(!userService.updateUser(wxUser)){
                return (WxAuthResponse) authResponse.build(RespStatus.SYSTEM_ERROR);
            }
            if(user.getUserStatus() == 1){
                return (WxAuthResponse) authResponse.build(RespStatus.ACCOUNT_IS_BANNED);
            }
        }
        authResponse.setOpenid(js2SessionInfo.getOpenid());
        authResponse.setUnionid(js2SessionInfo.getUnionid());
        authResponse.setSessionId(sessionId);
        authResponse.setCode(200);
        authResponse.setMsg("请求成功!");
        return authResponse;
    }

    @ApiOperation(value = "微信小程序登录", notes = "")
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public WxLoginResponse select(@RequestBody WxLoginRequest request,HttpServletRequest httprequest){
        log.info("微信小程序登录: {}", request.toString());

        final WxLoginResponse authResponse = new WxLoginResponse();
        //查看该用户是否已被封禁
        if (request.getSessionId() != null){
            final WxUser user = userService.getUserBySessionId(request.getSessionId());
            if(user == null){
                return (WxLoginResponse) authResponse.build(RespStatus.SESSION_ID_EXPIRE);
            }
            if (user.getUserStatus() == 1){
                return (WxLoginResponse) authResponse.build(RespStatus.ACCOUNT_IS_BANNED);
            }
        }

        WxUser wxUser=new WxUser();
        wxUser.setSessionId(request.getSessionId());
        wxUser.setCity(request.getCity());
        wxUser.setCountry(request.getCountry());

        if(StringUtils.isBlank(request.getAvatarUrl())){
            request.setAvatarUrl("https://image.yunxian.shop/images/wxfile://tmp_053e0a82a115f0e8449cf145d791053f.jpg");
        }

        wxUser.setHeadImgUrl(request.getAvatarUrl());

        wxUser.setLanguage(request.getLanguage());
        wxUser.setNickName(request.getNickName());
        wxUser.setPhone(request.getPhone());
        wxUser.setOpenid(request.getOpenid());
        wxUser.setIp(getClientIp(httprequest));
        wxUser.setProvince(request.getProvince());
        wxUser.setSex(request.getGender());
        //wxUser.setUserStatus(0);
        wxUser.setVersion(request.getVersion());
        wxUser.setUpdateTime(new Date());
        //根据sessionId插入用户信息
        boolean flag = userService.updateUserBySessionId(wxUser);
        if (flag){
            authResponse.build(200,"用户信息更新成功!");
        }else{
            authResponse.build(400,"用户信息更新失败!");
        }
        return authResponse;
    }

    @RequestMapping(value = "/getUserInfo", method = {RequestMethod.POST})
    public UserInfoResponse select(@RequestBody WxLoginRequest request){

        UserInfoResponse response = new UserInfoResponse();

        WxUser wxUser = userService.getUserBySessionId(request.getSessionId());

        if(null == wxUser){
            response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        response.setWxUser(wxUser);

        return response;
    }



}