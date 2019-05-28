package com.mhc.yunxian.bean.request.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2018/12/10.
 *
 * @author Alin
 */
@ApiModel
@Data
public class BaseRequestParam {
	/**
	 * 会话ID
	 */
	@ApiModelProperty(value = "sessionId", notes = "sessionId")
	protected String sessionId;
}
