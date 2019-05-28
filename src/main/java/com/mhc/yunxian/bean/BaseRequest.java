package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseRequest implements Serializable {

	public String code;

	private Integer page = 1;

	private Integer size = 20;

}
