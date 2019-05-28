package com.mhc.yunxian.enums;

/**
 * @author
 */
public enum IsDeleteEnum {
	/**
	 * 记录没有删除
	 */
	IS_NOT_DELETE(0, "记录没有删除"),
	/**
	 * 记录已经删除
	 */
	IS_DELETE(1, "记录已经删除"),;


	private Integer code;
	private String explain;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getExplain() {
		return explain;
	}

	public void setExplain(String explain) {
		this.explain = explain;
	}

	IsDeleteEnum(Integer code, String explain) {
		this.code = code;
		this.explain = explain;
	}
}
