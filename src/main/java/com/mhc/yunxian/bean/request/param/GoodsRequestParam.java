package com.mhc.yunxian.bean.request.param;

import com.mhc.yunxian.bean.Goods;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2018/12/10.
 *
 * @author Alin
 */
@Data
@ApiModel
public class GoodsRequestParam extends BaseRequestParam {
	/**
	 * 商品Id
	 */
	private Integer goodsId;
	/**
	 * 商品编号
	 */
	String goodsNum;
	@ApiModelProperty
	List<Goods> goods;
}
