package com.mhc.yunxian.aop;

import com.mhc.yunxian.enums.ResultCodeEnum;
import com.mhc.yunxian.exception.DataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 异常统一处理类
 *
 * @Author MoXiaoFan
 * @Date 2019/1/18 9:13
 */

// @ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 拦截业务异常
     */
    @ExceptionHandler(DataException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public DataException notFound(DataException e) {
        log.error("拦截到业务异常：{}", e.toString());
        return new DataException(e.getCode(), e.getMsg());
    }


    /**
     * 拦截未知的运行时异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public DataException notFound(RuntimeException e) {
        log.error("拦截到未知的运行时异常：{}", e.toString());
        return new DataException(ResultCodeEnum.SERVER_ERROR.getCode(), ResultCodeEnum.SERVER_ERROR.getMsg());
    }

}
