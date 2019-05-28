package com.mhc.yunxian.utils;


import com.alibaba.fastjson.JSONObject;
import com.mhc.yunxian.exception.DataException;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AopUtils {





    /**
     * 获取参数名=值, 逗号连接
     * @param className
     * @param methodName
     * @param args
     * @param requestURI
     * @return
     * @throws NotFoundException
     */
    public static String getParams(String className, String methodName, Object[] args, String requestURI) throws NotFoundException {
        Asserts.notBlank(className, "className must not be null");
        Asserts.notBlank(methodName, "methodName must not be null");
        if (args == null || args.length == 0) {
            return null;
        }


        String[] parameterNames = getFieldsName(AopUtils.class, className, methodName);

        if (ArrayUtils.isEmpty(parameterNames)) {
            return null;
        }

        JSONObject jo = new JSONObject();
        for (int i = 0, len = parameterNames.length; i < len; i++) {
            if (StringUtils.isBlank(parameterNames[i])) {
                throw new DataException("参数名称为空");
            }

            if (args.length == parameterNames.length) {

                //打印HttpServletResponse报错, getOutputStream() has already been called for this response, 不会解决, 暂时不打印就行了
                if (args[i] instanceof HttpServletResponse) {
                    args[i] = null;
                }
                jo.put(parameterNames[i], args[i]);
            }
        }
        return jo.toString();

    }

    /**
     * 得到方法参数的名称, 基本类型和对象的名称都能拿到
     * @param cls
     * @param clazzName
     * @param methodName
     * @return
     * @throws NotFoundException
     */
    public static String[] getFieldsName(Class cls, String clazzName, String methodName) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        //ClassClassPath classPath = new ClassClassPath(this.getClass());
        ClassClassPath classPath = new ClassClassPath(cls);
        pool.insertClassPath(classPath);

        CtClass cc = pool.get(clazzName);
        CtMethod cm = cc.getDeclaredMethod(methodName);
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            // exception
        }
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        List<String> list = new ArrayList<>();

        String[] paramNames = new String[cm.getParameterTypes().length];
        for (int i = 0; i < paramNames.length; i++){
            String variableName = attr.variableName(i + pos);
            if ("this".equals(variableName)) {
                continue;
            }
            if ("httpServletRequest".equals(variableName)){
                continue;
            }
            list.add(variableName);	//paramNames即参数名
        }

        String[] arr = list.toArray(new String[list.size()]);
        return arr;
    }

}
