package com.mhc.yunxian.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpContextUtils {

    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (servletRequestAttributes == null) {
            return null;
        }

        return servletRequestAttributes.getRequest();
    }

    public static String doGet(String url, Map<String, String> param) {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, (String) param.get(key));
                }
            }
            URI uri = builder.build();

            HttpGet httpGet = new HttpGet(uri);

            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200)
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    public static String doGet(String url) {
        return doGet(url, null);
    }

    public static String doPost(String url, Map<String, String> headers, Map<String, String> params) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpPost httpPost = new HttpPost(url);

            if (!CollectionUtils.isEmpty(headers)) {
                for (Map.Entry<String, String> entry :
                        headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }

            if (!CollectionUtils.isEmpty(params)) {
                List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
                for (Map.Entry<String, String> entry :
                        params.entrySet()) {
                    paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
                httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }

        return resultString;
    }

    public static String doPut(String url, Map<String, String> param) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpPut httpPut = new HttpPut(url);

            if (!CollectionUtils.isEmpty(param)) {
                List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, (String) param.get(key)));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
                httpPut.setEntity(entity);
            }
            response = httpClient.execute(httpPut);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    public static String doPost(String url) {
        return doPost(url, null, null);
    }

    public static String doPost(String url, Map<String, String> params) {
        return doPost(url, null, params);
    }
//	public static String doPostSign(String url, Map<String, String> params, String _appid) {
//		//签名
//		long _timestamp = System.currentTimeMillis();
//		String _sign = AopUtil.getSign(params, _appid, _timestamp);
//
//		Map<String, String> headers = new HashMap<>();
//		headers.put("_timestamp", _timestamp + "");
//		headers.put("_sign", _sign);
//
//		return doPost(url, headers, params);
//	}

    public static String doPostJson(String url, String json) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpPost httpPost = new HttpPost(url);

            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }


    public static CloseableHttpResponse doPostJson_v2(String url, String json) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);

            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            response.setHeader("Connection", "keep-alive");
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            response.setHeader("Content-Type", "image/jpeg");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return response;
    }


    /**
     * 失败, 不能传参
     *
     * @param url
     * @param param
     * @return
     */
    public static String doDelete(String url, Map<String, String> param) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpDeleteWithBody httpDeleteWithBody = new HttpDeleteWithBody(url);

            if (!CollectionUtils.isEmpty(param)) {
                String json = JsonUtils.toJson(param);
                httpDeleteWithBody.setEntity(new StringEntity(json));
            }
            response = httpClient.execute(httpDeleteWithBody);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    @NotThreadSafe
    private static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        private static final String METHOD_NAME = "DELETE";

        public HttpDeleteWithBody() {
        }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }


    }

}
