package com.mhc.yunxian.bean.response;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.ShopDragonInfo;
import com.mhc.yunxian.bean.index.DragonInfoVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 *
 * @author Administrator
 * @date 2018/12/13
 */
@Data
@ApiModel
public class BrowsedDragonResponse extends BaseResponse {

	private List<ShopDragonInfo> shopDragonInfoList;
	/**
	 * 接龙列表
	 */
	private List<DragonInfoVO> dragonInfoVOList;
	/**
	 * 接龙数
	 */
	private Integer dragonInfoVOListSize;

	private List<AttentionShopVO> attentionShopVOList;

	/**
	 * 店铺Id
	 */
	private Long ShopId;
	/**
	 * 店铺名称
	 */
	private String shopName;
	/**
	 * 店主openId
	 */
	private String ShopkeeperOpenId;
	/**
	 * 店铺logo
	 */
	private String shopHeadPicture;
	/**
	 * 店铺的复购口碑
	 */
	private Integer repurchaseCount;
}


