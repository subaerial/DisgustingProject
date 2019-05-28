package com.mhc.yunxian.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.mhc.yunxian.service.WechatOpenService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by zhangyingdong on 2018/05/03.
 */
@Slf4j
@Configuration
public class HttpServiceConfiguration {


    @Bean
    public OkHttpClient okHttpClient() {

        return new OkHttpClient.Builder()
                // 连接超时时间设置
                .connectTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(new LoggingInterceptor())
                // 读取超时时间设置
                .readTimeout(10, TimeUnit.SECONDS)
                // 失败不重试
                .retryOnConnectionFailure(true)
                .connectionPool(new ConnectionPool(100, 5L, TimeUnit.MINUTES))
                .hostnameVerifier((hostname, session) -> true)
                // 使用host name作为cookie保存的key
                .cookieJar(new CookieJar() {
                    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(HttpUrl.parse(url.host()), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(HttpUrl.parse(url.host()));
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .build();
    }

    @Bean
    public Retrofit wechatOpenRetrofit(@Autowired OkHttpClient okHttpClient) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return new Retrofit.Builder()
                .baseUrl("https://api.weixin.qq.com/")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(okHttpClient)
                .build();
    }

    @Bean
    public WechatOpenService wechatOpenService(@Autowired @Qualifier("wechatOpenRetrofit") Retrofit wechatOpenRetrofit) {
        return wechatOpenRetrofit.create(WechatOpenService.class);
    }


    class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long start = System.nanoTime();
            log.info("Sending Request [Url {}, Method {}, RequestBody {}] on {}.",
                    request.url(), request.method(), request.body(), chain.connection());
            log.debug("Request Headers is {}", request.headers());
            Response response = chain.proceed(request);
            log.info("Received {} in {}ms.",
                    response, (System.nanoTime() - start) / 1e6d);
            log.debug("Response Headers is {}", response.headers());
            return response;
        }
    }
}
