package com.mhc.yunxian.utils;

import com.alibaba.fastjson.JSON;
import com.mhc.yunxian.bean.MessageRequest;
import com.mhc.yunxian.bean.MessageResponseBody;
import com.mhc.yunxian.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.TimerTask;

/**
 * 通知操作创建工厂
 *
 * @Author MoXiaoFan
 * @Date 2019/1/17 10:58
 */

@Slf4j
public class NoticeTaskFactory {

    private static OkHttpClient okHttpClient = SpringContextHolder.getBean(OkHttpClient.class);
    private static WxUserService wxUserService = SpringContextHolder.getBean(WxUserService.class);

    private static Boolean parseMessageResponse(ResponseBody body) throws IOException {
        if (body != null) {
            MessageResponseBody messageResponseBody = JsonUtils.toObj(body.string(), MessageResponseBody.class);
            if ("ok".equalsIgnoreCase(messageResponseBody.getErrmsg()) && Integer.valueOf(0).equals(messageResponseBody.getErrcode())) {
                log.info("---------------------------------服务通知发送成功!----------------------------------------");
                return Boolean.TRUE;
            }
            log.error("-----------服务通知发送失败----------------错误信息{}", messageResponseBody.getErrmsg());
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    public static TimerTask sendMsg(String accessToken, MessageRequest request) {
        return new TimerTask() {
            @Override
            public void run() {
                int num = 0;
                try {
                    okhttp3.Response httpResponse = okHttpClient.newCall(new Request.Builder()
                            .url("https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=" + accessToken)
                            .post(okhttp3.RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(request)))
                            .build()).execute();
                    ResponseBody body = httpResponse.body();
                    Boolean flag = parseMessageResponse(body);
                    if (!flag && num != 3) {
                        Thread.sleep(10);
                        num++;
                        sendMsg(wxUserService.getWxAccessToKenInfo(), request);
                    }
                } catch (IOException e) {
                    log.error("消息发送失败:{}", e.getMessage());
                } catch (InterruptedException e) {
                    log.error("消息发送失败");
                }

            }
        };
    }
}
