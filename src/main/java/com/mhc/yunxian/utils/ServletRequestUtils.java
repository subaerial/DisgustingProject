package com.mhc.yunxian.utils;

import org.springframework.util.CollectionUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ServletRequest工具类.
 *
 * @author
 * @version 1.0 18/5/6
 * @since 1.0
 */
public abstract class ServletRequestUtils {

    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    /**
     * 获取请求参数映射.
     *
     * @param request
     * @return
     */
    public static Map<String, String> getParameterMap(ServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        CollectionUtils.toIterator(request.getParameterNames()).forEachRemaining(e -> {
            map.put(e, request.getParameter(e));
        });

        return map;
    }

}
