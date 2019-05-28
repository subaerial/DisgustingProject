package com.mhc.yunxian.bean.response;

import com.mhc.yunxian.bean.index.DragonInfoVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/12/14.
 */
@Data
public class AttentionShopVO {
	/**
	 * 店家id
	 */
	private Long shopId;
	/**
	 * 店家openId
	 */
	private String shopkeeperOpenId;
	/**
	 * 店家名称
	 */
	private String shopName;
	/**
	 * 店家头像
	 */
	private String shopHeadPicture;
	/**
	 * 店家正在进行的接龙数
	 */
	private Integer dragonInProcessCount;
	/**
	 * 店家店家复购口碑
	 */
	private Integer repurchase;
	/**
	 * 关注时间(记录的创建时间)
	 */
	private Date gmtCreate;
	/**
	 * 店家正在进行的接龙列表
	 */
	private List<DragonInfoVO> dragonInfoVOList;
}
