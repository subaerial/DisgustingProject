package com.mhc.yunxian.controller;

import com.github.pagehelper.PageHelper;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.request.param.ShopInfoRequestParam;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.service.AttentionShopService;
import com.mhc.yunxian.service.WxUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @date 2018/12/9
 */
@Api
@Slf4j
@RequestMapping()
@Controller
public class AttentionShopController {

	@Autowired
	private WxUserService userService;
	@Autowired
	private AttentionShopService attentionShopService;
	@Autowired
	private WxUserService wxUserService;

	/**
	 * 关注店铺,若之前已关注,则取消关注
	 */
	@ResponseBody
	@ApiOperation(value = "/attentionShop", notes = "关注店铺")
	@RequestMapping(value = "/attentionShop", method = RequestMethod.POST)
	public BaseResponse attentionShop(@RequestBody ShopInfoRequestParam param) {
		// 传入参数需要卖家userId
		log.debug("请求参数{}", param.toString());
		WxUser wxUser = userService.getUserBySessionId(param.getSessionId());
		if (null == wxUser) {
			return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
		}
		param.setUserId(Long.valueOf(wxUser.getId()));
		return attentionShopService.attentionShop(param);
	}

	/**
	 * 我关注的店铺
	 */
	@ResponseBody
	@ApiOperation(value = "/myAttentionShop", notes = "我关注店铺")
	@RequestMapping(value = "/myAttentionShop", method = RequestMethod.POST)
	public BaseResponse myAttentionShop(@RequestBody ShopInfoQuery query) {
		log.debug("请求参数{}", query.toString());
		WxUser wxUser = wxUserService.getUserBySessionId(query.getSessionId());
		if (null == wxUser) {
			return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
		}
		query.setShopkeeperOpenId(wxUser.getOpenid());
		PageHelper.startPage(query.getPageNo(), query.getPageSize());
		return attentionShopService.myAttentionShop(query);
	}
}
