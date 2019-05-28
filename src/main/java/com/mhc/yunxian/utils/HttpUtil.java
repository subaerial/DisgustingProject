package com.mhc.yunxian.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Administrator on 2018/5/22.
 */
@Slf4j
public class HttpUtil {
	public static String getClientIp(HttpServletRequest request) {

		try {
			if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
				return (request.getHeader("X-Real-IP"));
			}

			String ip = request.getHeader("x-forwarded-for");

			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("X-Real-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
			return ip;

		} catch (Exception e) {
			e.printStackTrace();
			log.info("get client ip error: {}",e.getMessage());
		}

		return "0.0.0.0";
	}


    /**
	 *  发起get请求
	 *  @param url
	 *  @return	 result
	 *  */
    public static String httpGet(String url) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (Exception e) {
			e.printStackTrace();
            return null;
        }
        return result;
    }

	/**	 * 发送httppost请求	 *
	 *  * @param url
	 *  * @param data
	 *  * @return	 */
	public static String httpPost(String url, String data) {
		String result = null;
		OkHttpClient httpClient = new OkHttpClient();
		RequestBody requestBody = RequestBody.create(MediaType.parse("text/html;charset=utf-8"), data);
		Request request = new Request.Builder().url(url).post(requestBody).build();
		try {
			Response response = httpClient.newCall(request).execute();
			result = response.body().string();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}





	

}
