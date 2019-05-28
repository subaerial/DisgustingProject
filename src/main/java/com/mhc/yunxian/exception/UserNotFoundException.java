package com.mhc.yunxian.exception;

/**
 * Created by huzichi on 2016/11/22.
 */
public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String msg){
        super(msg);
    }
}
