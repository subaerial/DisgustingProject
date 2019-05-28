package com.mhc.yunxian.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.MoneyRecordEnum;
import com.mhc.yunxian.enums.WxSubTypeEnum;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 微信消息处理
 * <p>
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/wx")
@EnableSwagger2
public class WxController {

    @Autowired
    WxService wxService;

    @Autowired
    WechatOpenService wechatOpenService;
    @Autowired
    BalanceService balanceService;
    @Value("${wx.admin.appid}")
    String wxAppid;
    @Value("${wx.admin.secret}")
    String wxSecret;

    @Value("${yunxian.service.ip}")
    String serviceIp;

    @Autowired
    WxUserService userService;

    @Autowired
    RemoteCache redisCache;

    @Autowired
    DrawMoneyService drawMoneyService;

    @Autowired
    MoneyRecordService moneyRecordService;

    @Autowired
    TaskService taskService;

    @Autowired
    WxUserService wxUserService;

    @Autowired
    OSSClientUtil ossClientUtil;

    @ApiOperation(value = "管理员微信公众号授权", notes = "")
    @RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST})
    public WxAuthResponse auth(@RequestBody BaseRequest baseRequest) throws IOException, ParseException {

        log.info("auth,code:{}", baseRequest.getCode());

        final WxAuthResponse authResponse = new WxAuthResponse();

        final WechatOpenService.OauthAccessToken oauthAccessToken = wechatOpenService.oauthAccessToken(wxAppid, wxSecret,
                baseRequest.getCode(), "authorization_code").execute().body();

        log.info("code换取access_token, code:{},return:{}", baseRequest.getCode(), oauthAccessToken != null ? oauthAccessToken.toString() : "");

        if (oauthAccessToken.getErrcode() != null) {
            log.error("code换取access_token失败,code:{},msg:{}", oauthAccessToken.getErrcode(), oauthAccessToken.getErrmsg());
            return (WxAuthResponse) authResponse.build(RespStatus.GET_ACCESS_TOKEN_ERROR);
        }

        final WechatOpenService.UserInfo userInfo = wechatOpenService.userInfo(oauthAccessToken.getAccess_token(),
                oauthAccessToken.getOpenid(),
                "zh_CN").execute().body();


        log.info("access_token换取userinfo, access_token:{},return:{}", oauthAccessToken.getAccess_token(), userInfo != null ? userInfo.toString() : "");

        if (oauthAccessToken.getErrcode() != null) {
            log.error("code换取access_token失败,code:{},msg:{}", oauthAccessToken.getErrcode(), oauthAccessToken.getErrmsg());
            return (WxAuthResponse) authResponse.build(RespStatus.GET_USER_INFO_ERROR);
        }

        //查看openid是否存在
        WxAdmin wxAdmin = wxService.getWxAdmin(oauthAccessToken.getOpenid());

        if (wxAdmin == null) {
            wxAdmin = new WxAdmin();

            wxAdmin.setCreateTime(new Date());
            wxAdmin.setHeadImg(userInfo.getHeadimgurl());
            wxAdmin.setNickName(userInfo.getNickname());
            wxAdmin.setOpenid(userInfo.getOpenid());

            if (wxService.addWxAdmin(wxAdmin) < 1) {
                return (WxAuthResponse) authResponse.build(RespStatus.ADD_ADMIN_FAIL);
            }

        } else {
            wxAdmin.setNickName(userInfo.getNickname());
            wxAdmin.setHeadImg(userInfo.getHeadimgurl());

            if (wxService.updateWxAdmin(wxAdmin) < 1) {
                log.error("微信用户信息更新失败,msg:{}", wxAdmin.toString());
            }

            if (wxAdmin.getState() == 1) {
                String sessionId = UUID.randomUUID().toString().replaceAll("-", "");

                redisCache.set("wx_sessionId:" + sessionId, wxAdmin.getOpenid(), 60 * 60, TimeUnit.SECONDS);

                authResponse.setSessionId(sessionId);
            }

        }

        authResponse.setCode(200);
        authResponse.setMsg("请求成功!");
        return authResponse;
    }

    @ApiOperation(value = "微信公众号授权", notes = "")
    @RequestMapping(value = "/user/auth", method = {RequestMethod.GET, RequestMethod.POST})
    public WxAuthResponse userAuth(@RequestBody BaseRequest baseRequest) throws IOException, ParseException {

        log.info("auth,code:{}", baseRequest.getCode());

        final WxAuthResponse authResponse = new WxAuthResponse();

        final WechatOpenService.OauthAccessToken oauthAccessToken = wechatOpenService.oauthAccessToken(wxAppid, wxSecret,
                baseRequest.getCode(), "authorization_code").execute().body();

        log.info("code换取access_token, code:{},return:{}", baseRequest.getCode(), oauthAccessToken != null ? oauthAccessToken.toString() : "");

        if (oauthAccessToken.getErrcode() != null) {
            log.error("code换取access_token失败,code:{},msg:{}", oauthAccessToken.getErrcode(), oauthAccessToken.getErrmsg());
            return (WxAuthResponse) authResponse.build(RespStatus.GET_ACCESS_TOKEN_ERROR);
        }

        final WechatOpenService.UserInfo userInfo = wechatOpenService.userInfo(oauthAccessToken.getAccess_token(),
                oauthAccessToken.getOpenid(),
                "zh_CN").execute().body();


        log.info("access_token换取userinfo, access_token:{},return:{}", oauthAccessToken.getAccess_token(), userInfo != null ? userInfo.toString() : "");

        if (oauthAccessToken.getErrcode() != null) {
            log.error("code换取access_token失败,code:{},msg:{}", oauthAccessToken.getErrcode(), oauthAccessToken.getErrmsg());
            return (WxAuthResponse) authResponse.build(RespStatus.GET_USER_INFO_ERROR);
        }

        WxUser wxUser = wxUserService.selectByUnionid(userInfo.getUnionid());

        if (wxUser == null) {
            return (WxAuthResponse) authResponse.build(RespStatus.PROGRAM_NOT_EXIST);
        }

        wxUser.setGzhOpenid(userInfo.getOpenid());

        if (!wxUserService.updateUserByOpenid(wxUser)) {
            return (WxAuthResponse) authResponse.build(RespStatus.SYSTEM_ERROR);
        }

        authResponse.setCode(200);
        authResponse.setMsg("请求成功!");
        return authResponse;
    }


    @RequestMapping(value = "/receiveWxMessage", method = {RequestMethod.POST})
    public String receiveWxMessage(@RequestBody HttpServletRequest request) throws IOException, DocumentException {

        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        // 得到xml根元素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();

        // 遍历所有子节点
        for (Element e : elementList) {
            map.put(e.getName(), e.getText());
        }

        wxService.autoReply(map);

        return "success";
    }

    @RequestMapping(value = "/getDrawMoney", method = {RequestMethod.POST})
    public DrawMoneyResponse getDrawMoney(@RequestBody DrawMoneyRequest request) {
        DrawMoneyResponse response = new DrawMoneyResponse();

        if (request == null || StringUtils.isBlank(request.getSessionId())
                || request.getDrawMoneyId() == null) {
            return (DrawMoneyResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        String openid = redisCache.get("wx_sessionId:" + request.getSessionId());

        if (StringUtils.isBlank(openid)) {
            return (DrawMoneyResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        DrawMoney drawMoney = drawMoneyService.getDrawMoneyById(request.getDrawMoneyId());

        if (drawMoney == null) {
            return (DrawMoneyResponse) response.build(RespStatus.CURRENT_NOT_DETAIL);
        }

        WxUser wxUser = userService.getWxUser(drawMoney.getOpenid());

        if (wxUser == null) {
            return (DrawMoneyResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        WithdrawMoney withdrawMoney = new WithdrawMoney();
        withdrawMoney.setDrawMoney(drawMoney.getDrawMoney());
        withdrawMoney.setOpenid(drawMoney.getOpenid());
        withdrawMoney.setStatus(drawMoney.getStatus());
        withdrawMoney.setDate(drawMoney.getCreateTime());
        withdrawMoney.setPhone(wxUser.getPhone());
        withdrawMoney.setName(wxUser.getNickName());
        withdrawMoney.setId(drawMoney.getId());
        withdrawMoney.setRate(drawMoney.getRate());

        response.setRecord(withdrawMoney);

        return response;
    }

    @RequestMapping(value = "/drawMoney", method = {RequestMethod.POST})
    @Transactional(rollbackFor = {DataException.class, Exception.class})
    public BaseResponse drawMoney(@RequestBody DrawMoneyRequest request) {
        BaseResponse response = new BaseResponse();

        if (request == null || StringUtils.isBlank(request.getSessionId())
                || request.getDrawMoneyId() == null) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        String openid = redisCache.get("wx_sessionId:" + request.getSessionId());

        if (StringUtils.isBlank(openid)){
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        DrawMoney drawMoney = drawMoneyService.getDrawMoneyById(request.getDrawMoneyId());

        if (drawMoney == null) {
            return response.build(RespStatus.CURRENT_NOT_DETAIL);
        }

        if (drawMoney.getStatus() == 2) {
            return response.build(RespStatus.DRAW_MONEY_HAD_COMPELETED);
        }

        Balance balance1 = balanceService.getBalance(drawMoney.getOpenid());
        if (balance1 == null) {
            return response.build(RespStatus.DRAW_MONEY_FAIL);
        }


        try {
            //打款到用户微信

            TransfersReponse transfersReponse = wxService.payToUserWx(drawMoney.getDrawOrderNum(),
                    drawMoney.getOpenid(),
                    drawMoney.getDrawMoney(),
                    serviceIp);

            if (!transfersReponse.getResult()) {
                response.build(7001, transfersReponse.getMsg());
                return response;
            }

            //修改提现状态
            drawMoney.setUpdateTime(new Date());
            drawMoney.setStatus(2);//已提现
            drawMoney.setPaymentNo(transfersReponse.getPaymentNo());
            drawMoney.setPaymentTime(DateUtils.convertString2Date2(transfersReponse.getPaymentTime()));
            if (!drawMoneyService.updateDrawMoney(drawMoney)) {
                throw new DataException("提现记录修改失败");
            }

            //更新交易纪录表（提现中->提现完成）
            MoneyRecord moneyRecord = moneyRecordService.selectRecordByDrawMoneyId(request.getDrawMoneyId().toString());
            moneyRecord.setRecordType(MoneyRecordEnum.PRESENT_COMPLETION.getCode());//提现完成
            moneyRecord.setCause(MoneyRecordEnum.PRESENT_COMPLETION.getMsg());
            if (!moneyRecordService.updateByDrawMoneyId(moneyRecord)) {
                throw new DataException("交易记录表修改失败");
            }


            try {
                taskService.noticeOfWithdrawToSeller(drawMoney.getFormId(), drawMoney.getDrawMoney(), drawMoney.getOpenid());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("提现通知错误", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return response.build(RespStatus.DRAW_MONEY_FAIL);
        }

        return response;
    }


    /**
     * 订阅事件
     * @param request
     * @param response
     * @return
     */
//    @RequestMapping(value = "/subscribe")
//    public String subscribe(HttpServletRequest request) {
//    /**
//     * 订阅事件
//     * @param echostr
//     * @return
//     */
//    @RequestMapping(value = "/subscribe", method = RequestMethod.GET)
//    public String subscribe(@RequestParam(value = "echostr",required = false) String echostr) {

//
//            return echostr;

//        Map<String, String> map = new HashMap<>();
//        try {
//            map = XmlUtils.inputStream2Map(request.getInputStream());
//            log.info("订阅事件入参：{}",JsonUtils.toJson(map));
//            String msgType = map.get("Event");
//            Map<String, String> params = new HashMap<>();
//            params.put("access_token", userService.getWxGZHAccessToken());
//            params.put("openid", map.get("FromUserName"));
//            params.put("lang", "zh_CN");
//            JSONObject json = JSON.parseObject(HttpContextUtils.doGet("https://api.weixin.qq.com/cgi-bin/user/info", params));
//            if (null != json.get("errcode")) {
//                throw new DataException("获取订阅用户信息失败");
//            }
//            WxUnion wxUnion = new WxUnion();
//            if (msgType.equals("subscribe")) {
//                //订阅，查询用户信息并保存unionId
//                wxUnion.setCreateTime(new Date());
//                wxUnion.setUnionId(json.getString("unionid"));
//                wxUnion.setWxOpenId(json.getString("openid"));
//                wxUnion.setIsSub(WxSubTypeEnum.SUBSCRIBE.getCode().byteValue());
//                wxService.saveWxInfo(wxUnion);
//            } else if (msgType.equals("unsubscribe")) {
//                wxUnion.setWxOpenId(map.get("FromUserName"));
//                wxUnion.setIsSub(WxSubTypeEnum.UNSUBSCRIBE.getCode().byteValue());
//                wxService.updateWxInfo(wxUnion);
//            }
//
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return "success";
//
//    }

    /**
     * 公众号接入
     * @return
     */
    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    public BaseResponse subscribe(HttpServletRequest request) {
        log.info("----------进入微信推送消息逻辑-----------");
        BaseResponse baseResponse = new BaseResponse();
        Map<String, String> map = new HashMap<>();
        try {
            map = XmlUtils.inputStream2Map(request.getInputStream());
            String fromUserName = map.get("FromUserName");
            log.info("公众平台推送消息入参 : {}",JsonUtils.toJson(map));
            Byte isSub = new Integer(-1).byteValue();
            if (map.get("MsgType")!=null && map.get("Event")!=null) {
                if (map.get("MsgType").equals("event")){
                    if (map.get("Event").equals("subscribe")){
                        isSub = WxSubTypeEnum.SUBSCRIBE.getCode();
                    }else if(map.get("Event").equals("unsubscribe")){
                        isSub = WxSubTypeEnum.UNSUBSCRIBE.getCode();
                    }
                }
            }
            if (isSub.intValue()==-1){
                throw new DataException("获取公众平台推送信息（Event）失败");
            }
            Map<String, String> params = new HashMap<>();
            params.put("access_token", userService.getWxGZHAccessToken());
            params.put("openid", fromUserName);
            params.put("lang", "zh_CN");
            JSONObject unionResponse = JSON.parseObject(HttpContextUtils.doGet("https://api.weixin.qq.com/cgi-bin/user/info", params));
            if (null != unionResponse.get("errcode")) {
                log.error("获取用户基本信息失败" + unionResponse.get("errmsg"));
                throw new DataException("获取用户基本信息（包括UnionID机制）失败" + unionResponse.get("errmsg"));
            }
            log.info("获取微信公众号信息：{}",JSONObject.toJSON(unionResponse));
            String unionid = unionResponse.getString("unionid");
            if (fromUserName != null) {
                WxUnion wxUnion = wxUserService.findWxUnionByOpenId(fromUserName);

                if (wxUnion == null){
                    wxUnion = new WxUnion();
                    wxUnion.setWxOpenId(fromUserName);
                    wxUnion.setUnionId(unionid);
                    wxUnion.setCreateTime(new Date());
                    wxUnion.setIsSub(isSub);
                    wxService.saveWxInfo(wxUnion);
                }else {
                    wxUnion.setUnionId(unionid);
                    wxUnion.setIsSub(isSub);
                    wxService.updateWxInfo(wxUnion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("关注公众号错误"+e.getMessage());
            return baseResponse.build(RespStatus.SYSTEM_ERROR);
        }
        return baseResponse.build(RespStatus.SUCCESS);
    }


    /**
     * 获取小程序码
     * @param request
     * @return
     */
    @RequestMapping(value = "/getWXACode", method = RequestMethod.POST)
    public BaseResponse getWXACode(@RequestBody GetWXACodeRequest request) {
        BaseResponse response = new BaseResponse();
        CloseableHttpResponse wxResponse = null;
        try {
            String access_token = "";
            JSONObject jsonObject = CommonUtils.getNewAccessToken();
            if (!jsonObject.isEmpty() && jsonObject.get("errcode") == null) {
                access_token = (String) jsonObject.get("access_token");
//                System.out.println(access_token);
            }
            if (access_token.length() > 0) {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("scene", request.getScene());
                paramMap.put("page", request.getPage());
                paramMap.put("width", request.getWidth());
                wxResponse = HttpContextUtils.doPostJson_v2("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + access_token, JSON.toJSONString(paramMap));
                HttpEntity entity = wxResponse.getEntity();
//                String localParentPath = "C:\\Project\\yunxian\\tmp\\";//本地存储
//                String localParentPath = "/usr/local/yunxian/tmp/wxacode_images/";//服务器存储
//                String fileName = request.getScene() + "_" + UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
                String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
//                File file = new File(localParentPath + fileName);
                byte[] resultBytes = EntityUtils.toByteArray(entity);
                String resultString = new String(resultBytes);
                if (!resultString.startsWith("{")) {
//                    Boolean isImageCreated = CommonUtils.byteArray2ImageFile(resultBytes, file);
//                    if (isImageCreated) {
                        String key = "images/" + fileName;
                        String etag = ossClientUtil.uploadLocalBytes2OSS(key, resultBytes);
                        if (!etag.isEmpty()) {
                            String internalUrl = ossClientUtil.getUrl(key, 1000L * 60 * 60 * 24 * 7);
                            String url = internalUrl.substring(0, internalUrl.indexOf("?"));
//                            if (!file.delete()) {
//                                log.error("localfile delete failure : " + file.getAbsolutePath());
//                            }
                            return response.build(RespStatus.SUCCESS.getKey(), url);
                        }
//                    }
                } else {
                    log.error(resultString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return response.build(RespStatus.SYSTEM_ERROR.getKey(), RespStatus.SYSTEM_ERROR.getDesc());
    }

}
