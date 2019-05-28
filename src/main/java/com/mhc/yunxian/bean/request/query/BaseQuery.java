package com.mhc.yunxian.bean.request.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Administrator on 2018/12/7.
 *
 * @author Alin
 */
@ApiModel(value = "BaseQuery", description = "请求查询对象基类")
public class BaseQuery {

	/**
	 * 会话ID
	 */
	@ApiModelProperty(value = "sessionId", notes = "sessionId")
	protected String sessionId;

	/**
	 * 页码
	 */
	@ApiModelProperty(value = "pageNo", notes = "当前页码")
	protected Integer pageNo = 1;

	/**
	 * 每页个数
	 */
	@ApiModelProperty(value = "pageSize", notes = "每页显示的记录数")
	protected Integer pageSize = 10;
	/**
	 * 偏移量
	 */
	private Integer offset;

	/**
	 * 排序sql
	 */
	private String orderBySql;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 分页数据偏移量
	 */
	public Integer getOffset() {
		computeOffset();
		return offset;
	}

	/**
	 * 计算分页偏移量
	 */
	public Integer computeOffset() {
		if (null == pageNo || null == pageSize) {
			return null;
		}
		offset = (pageNo - 1) * pageSize;
		return offset;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
		computeOffset();
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
		computeOffset();
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public String getOrderBySql() {
		return orderBySql;
	}

	public void setOrderBySql(String orderBySql) {
		this.orderBySql = orderBySql;
	}
}
