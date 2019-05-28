package com.mhc.yunxian.bean.response;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.index.DragonInfoVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 *
 * @author Alin
 * @date 2018/12/14
 */
@Data
@ApiModel
public class MyAttentionShopResponse extends BaseResponse {

	List<AttentionShopVO> attentionShopVOS;

}
