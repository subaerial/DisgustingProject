package com.mhc.yunxian.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Json工具类(基于Jackson)
 *
 * @author <a href="mailto:simonzbf@163.com">大兵</a>
 * @version 1.0 18/4/23
 * @since 1.0
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 对象转Json.
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String toJson(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("obj to json failure", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Json转对象.
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T toObj(String json, Class<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("json to obj failure", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Json转对象.
     *
     * @param json
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T toObj(String json, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("json to obj failure", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 将json数据转换成pojo对象list
     * <p>Title: jsonToList</p>
     * <p>Description: </p>
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T> List<T> toList(String jsonData, Class<T> beanType) {
        if (StringUtils.isEmpty(jsonData)) {
            return null;
        }
        if (jsonData == null) {
            return null;
        }

        TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
        JavaType javaType = typeFactory.constructParametricType(List.class, beanType);
        try {
            List<T> list = OBJECT_MAPPER.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("json to list failure", e);
            throw new RuntimeException(e);
        }
    }
}
