package com.mhc.yunxian.controller;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.admin.FindWxUserRequest;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.MoneyRecordEnum;
import com.mhc.yunxian.enums.OrderStatusEnum;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.DateUtils;
import com.mhc.yunxian.utils.HttpUtil;
import com.mhc.yunxian.utils.KeyTool;
import com.mhc.yunxian.utils.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/5/26.
 */
@Slf4j
@RestController
public class DrawMoneyController {
	@Autowired
	private DrawMoneyService drawMoneyService;
	@Autowired
	private BalanceService balanceService;
	@Autowired
	private WxUserService wxUserService;
	@Autowired
	private TaskService taskService;

	@Autowired
	PartyInfoService partyInfoService;

	@Autowired
	MoneyRecordService moneyRecordService;

	@Autowired
	OrderService orderService;

	@Autowired
	SystemParamService systemParamService;

	@Autowired
    WxService wxService;

	@Autowired
	private SendStationMsgService sendStationMsgService;

    @Value("${yunxian.service.ip}")
    String serviceIp;




    /**
     * 检查是否超过提现额度
     * @param request
     * @return
     */
    @RequestMapping(value = "/checkDrawMoney",method = RequestMethod.POST)
    public CheckDrawMoneyResponse getBankInfo(@RequestBody WithdrawMoneyRequest request){
        final CheckDrawMoneyResponse response = new CheckDrawMoneyResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null){
            return (CheckDrawMoneyResponse)response.build(RespStatus.USER_NOT_EXIST);
        }

        if(request.getMoney() == null || request.getDrawType() == null){
            return (CheckDrawMoneyResponse)response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        final Balance balance = balanceService.getBalance(wxUser.getOpenid());
        if (balance == null){
            return (CheckDrawMoneyResponse)response.build(RespStatus.BLANCE_IS_NULL);
        }

        //余额为零无法提现
        if (balance.getBalance() == 0){
            return (CheckDrawMoneyResponse)response.build(RespStatus.BALANCE_IS_ZERO);
        }

        List<OrderInfo> orderInfos = orderService.getOrderBySellerId(wxUser.getOpenid());

        int realBalance = balance.getBalance();

        for(OrderInfo orderInfo : orderInfos){
            if(orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())||
                    orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())||
                    orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_RECEIVED.getCode())){
                realBalance -= orderInfo.getOrderMoney();
            }
        }

        if(realBalance < request.getMoney()){
            return (CheckDrawMoneyResponse)response.build(RespStatus.BALANCE_NOT_ENOUGH);
        }

        //提现手续费

        SystemParam systemParam = systemParamService.selectOneByParamGroup("FORMALITY_RATE");

        if(null == systemParam){
            throw new DataException("提现费率获取失败");
        }

        UserBankInfo userBankInfo = drawMoneyService.getBankInfo(wxUser.getOpenid());

        double formalityRate = Double.parseDouble(systemParam.getParamValue());

        if( formalityRate > 0 && userBankInfo!= null
                && (userBankInfo.getUsedLimit()+request.getMoney()) > userBankInfo.getLimit()){
            Integer serviceCharge = new Long(Math.round(
                    (userBankInfo.getUsedLimit() + request.getMoney() - userBankInfo.getLimit())* formalityRate)).intValue();

            if((request.getMoney() + serviceCharge) > realBalance){

                Integer realDrawMoney = 0;

                Integer realServiceCharge = 0;

                realDrawMoney =new Long(Math.round((realBalance)/(1+formalityRate))).intValue();

                realServiceCharge = new Long(Math.round(realDrawMoney * formalityRate)).intValue();

                response.setRealDrawMoney(realDrawMoney);

                response.setServiceCharge(realServiceCharge);
            }
        }

        return response;
    }



    /**
	 * 申请提现接口
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/withdrawMoney",method = RequestMethod.POST)
	@Transactional(rollbackFor = {DataException.class,Exception.class}, isolation = Isolation.SERIALIZABLE)
	public BaseResponse drawMoney(@RequestBody WithdrawMoneyRequest request) throws Exception {
		final BaseResponse response=new BaseResponse();
		final WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
		if (wxUser == null){
			return response.build(RespStatus.USER_NOT_EXIST);
		}

//		if(request.getMoney() == null || request.getDrawType() == null){
//			return response.build(RespStatus.ILLEGAL_ARGUMENT);
//		}

		if(request.getMoney() == null){
			return response.build(RespStatus.ILLEGAL_ARGUMENT);
		}

		if (request.getMoney() > 2000000){
			return response.build(RespStatus.DRAW_MONEY_TOO_LARGE);
		}

		//申请之前查询该用户是否已经申请
		final List<DrawMoney> drawMonies =  drawMoneyService.getDrawMoneyDetail(wxUser.getOpenid());
		for (DrawMoney drawMoney:drawMonies) {
			if (drawMoney.getStatus() == 1){
				return response.build(RespStatus.DRAW_MONEY_ERROR);
			}
		}

		//查询余额
		final Balance balance1 = balanceService.getBalance(wxUser.getOpenid());
		//该条余额数据加数据库行级锁
		Balance balance = balanceService.lockBalance(balance1.getId());
		if (balance == null){
			return response.build(RespStatus.BLANCE_IS_NULL);
		}

		//余额为零无法提现
		if (balance.getBalance() == 0){
			return response.build(RespStatus.BALANCE_IS_ZERO);
		}

		List<OrderInfo> orderInfos = orderService.getOrderBySellerId(wxUser.getOpenid());

		int realBalance = balance.getBalance();

		for(OrderInfo orderInfo : orderInfos){
			if(orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())||
					orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())||
					orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_RECEIVED.getCode())){
				realBalance -= orderInfo.getOrderMoney();
			}
		}

		if(realBalance < request.getMoney()){
			return response.build(RespStatus.BALANCE_NOT_ENOUGH);
		}


		//提现手续费

		SystemParam systemParam = systemParamService.selectOneByParamGroup("FORMALITY_RATE");

		if(null == systemParam){
			throw new DataException("提现费率获取失败");
		}

		DrawMoney record=new DrawMoney();
		record.setCreateTime(new Date());
		record.setDrawMoney(request.getMoney());//提现金额
		record.setOpenid(wxUser.getOpenid());
		record.setFormId(request.getFormId());
		record.setStatus(1);//申请中   //1:申请中；2:已提现；
		record.setDrawType(request.getDrawType());
		record.setRate(systemParam.getParamValue());
		record.setDrawOrderNum(KeyTool.createOrderNo());
		//插入提现信息到提现记录表
		try {
			if (drawMoneyService.insertDrawMoney(record) < 1){
				return response.build(RespStatus.SYSTEM_ERROR);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new DataException("添加提现纪录错误:"+e);
		}

		DrawMoney record2 = drawMoneyService.selectByOpenIdAndFormId(wxUser.getOpenid(),request.getFormId());


		UserBankInfo userBankInfo = drawMoneyService.getBankInfo(wxUser.getOpenid());

		double formalityRate = Double.parseDouble(systemParam.getParamValue());

		Integer serviceCharge = 0;

		if( formalityRate > 0 && userBankInfo!= null && request.getDrawType() == 1
				&& (userBankInfo.getUsedLimit()+request.getMoney()) > userBankInfo.getLimit()){
			serviceCharge = new Long(Math.round(
					(userBankInfo.getUsedLimit() + request.getMoney() - userBankInfo.getLimit())* formalityRate)).intValue();

			final MoneyRecord moneyRecord2 = new MoneyRecord();
			moneyRecord2.setCreateTime(new Date());
			moneyRecord2.setMoney(serviceCharge);
			moneyRecord2.setRecordType(MoneyRecordEnum.SERVICE_CHARGE.getCode());
			moneyRecord2.setOpenid(wxUser.getOpenid());
			moneyRecord2.setBalance(String.valueOf(balance.getBalance() - serviceCharge));
			moneyRecord2.setCause(MoneyRecordEnum.SERVICE_CHARGE.getMsg());
			if (!moneyRecordService.add(moneyRecord2)){
				log.error("插入交易表错误");
				throw new DataException("插入交易表错误");
			}

			userBankInfo.setUsedLimit(userBankInfo.getLimit()+request.getMoney());

			if(drawMoneyService.updateUserBankInfo(userBankInfo) < 1){
				log.error("更新银行信息表错误");
				throw new DataException("更新银行信息表错误");
			}


		}


		//插入交易明细表
		final MoneyRecord moneyRecord = new MoneyRecord();
		moneyRecord.setCreateTime(new Date());
		moneyRecord.setMoney(request.getMoney());
		moneyRecord.setRecordType(MoneyRecordEnum.IN_THE_PRESENT.getCode());//提现中
		moneyRecord.setOpenid(wxUser.getOpenid());
		moneyRecord.setBalance(String.valueOf(balance.getBalance() - request.getMoney()));
		moneyRecord.setDrawMoneyId(String.valueOf(record2.getId()));
		moneyRecord.setCause(MoneyRecordEnum.IN_THE_PRESENT.getMsg());
		if (!moneyRecordService.add(moneyRecord)){
			log.error("插入交易表错误");
			throw new DataException("插入交易表错误");
		}

		//更新余额表
		balance.setBalance(balance.getBalance() - request.getMoney() - serviceCharge);
		balance.setUpdateTime(new Date());
		try {
			if (!balanceService.updateBalanceByOpenid(balance)){
				return response.build(RespStatus.SYSTEM_ERROR);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new DataException("更新余额错误:"+e);
		}



		response.build(200,"提现发起成功!");

		SystemParam systemParamMobile = systemParamService.selectOneByParamGroup("mobile");

		if(null == systemParamMobile){
			throw new DataException("提现通知手机号获取失败");
		}

		String mobile = systemParamMobile.getParamValue();

		//微信模板消息通知运营人员

		try {
			List<WxAdmin> wxAdmins = wxService.selectAdmin();

			for(WxAdmin wxAdmin : wxAdmins){

				taskService.addDrawMoneyMessageToAdmin(wxUser.getNickName(),record.getDrawMoney(),wxAdmin.getOpenid(),record2.getId());

			}

		}catch (Exception e){
			e.printStackTrace();
			log.error("模板消息发送失败:",e);
		}

		//短信通知运营人员
		SendSmsResponse sendSmsResponse = null;
		try {
			sendSmsResponse = SendMessageUtil.sendSmsSeller(wxUser.getNickName(),mobile);
		} catch (ClientException exce) {
			exce.printStackTrace();
			log.error("短信发送失败:"+exce);
			log.error("msg:"+exce);
		}

		if(!sendSmsResponse.getCode().equals("OK")){
			log.error("短信发送失败:"+sendSmsResponse.getMessage());
		}

		return response;

	}

	/**
	 * 申请提现的所有用户
	 * @return
	 */
	@RequestMapping(value = "/drawMoneyUser",method = RequestMethod.POST)
	public WithdrawMoneyResponse withdrawMoney(@RequestBody FindWxUserRequest request){
		final WithdrawMoneyResponse response = new WithdrawMoneyResponse();
		final List<WithdrawMoney> list = Lists.newArrayList();

		//获取所有申请提现的数据
		PageHelper.startPage(request.getPage(), request.getSize());
		final List<DrawMoney> drawMoneyList = drawMoneyService.selectByStatus();
		PageInfo<DrawMoney> pageInfo = new PageInfo<>(drawMoneyList);



		if (drawMoneyList == null){
			return (WithdrawMoneyResponse) response.build(RespStatus.SYSTEM_ERROR);
		}

		for (DrawMoney drawMoney:pageInfo.getList()) {
				WxUser wxUser = wxUserService.getWxUser(drawMoney.getOpenid());
				//List<OrderInfo> orderInfos = orderService.getOrderByOpenid(drawMoney.getOpenid());
				WithdrawMoney withdrawMoney = new WithdrawMoney();
				withdrawMoney.setDrawMoney(drawMoney.getDrawMoney());
				withdrawMoney.setOpenid(drawMoney.getOpenid());
				withdrawMoney.setStatus(drawMoney.getStatus());
				withdrawMoney.setDate(drawMoney.getCreateTime());
				withdrawMoney.setPhone(wxUser.getPhone());
				withdrawMoney.setName(wxUser.getNickName());
				withdrawMoney.setId(drawMoney.getId());
				withdrawMoney.setRate(drawMoney.getRate());
				list.add(withdrawMoney);
		}



		response.setData(list);

		response.setTotal(pageInfo.getTotal());

		return response;
	}

	/**
	 * 提现成功接口
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/drawMoneySuccess",method = RequestMethod.POST)
	@Transactional(rollbackFor = {DataException.class, Exception.class})
	public BaseResponse drawMoneySuccess(@RequestBody DrawMoneySuccessRequest request){
        BaseResponse response = new BaseResponse();

		final WxUser wxUser = wxUserService.getWxUser(request.getOpenid());
		if (wxUser == null){
			return response.build(RespStatus.SYSTEM_ERROR);
		}

		final Balance balance1 = balanceService.getBalance(wxUser.getOpenid());
		if (balance1 == null){
			return response.build(RespStatus.SYSTEM_ERROR);
		}

        DrawMoney drawMoney = drawMoneyService.getDrawMoneyById(request.getDrawMoneyId());
		if (drawMoney == null){
			return response.build(RespStatus.CURRENT_NOT_DETAIL);
		}
        if(drawMoney.getStatus() == 2){
            return response.build(RespStatus.DRAW_MONEY_HAD_COMPELETED);
        }

        try {
            //打款到用户微信
            if (drawMoney.getDrawMoney() <= 2000000) {
                TransfersReponse transfersReponse = wxService.payToUserWx(drawMoney.getDrawOrderNum(),
                        drawMoney.getOpenid(),
                        drawMoney.getDrawMoney(),
                        serviceIp);

                if (!transfersReponse.getResult()) {
                    log.error("微信提现失败:{}", transfersReponse);
                    response.build(7001, transfersReponse.getMsg());
                    return response;
                }
                drawMoney.setPaymentNo(transfersReponse.getPaymentNo());
                drawMoney.setPaymentTime(DateUtils.convertString2Date2(transfersReponse.getPaymentTime()));
            } else {
                log.warn("提现金额超过20000元，直接跳过微信支付 req：{}",JSONObject.toJSON(drawMoney));
                drawMoney.setPaymentNo("-1");
                drawMoney.setPaymentTime(new Date());
            }
            //修改提现状态
            drawMoney.setUpdateTime(new Date());
            drawMoney.setStatus(2);//已提现
            if (!drawMoneyService.updateDrawMoney(drawMoney)){
                throw new DataException("提现记录修改失败");
            }

            //更新交易纪录表（提现中->提现完成）
            MoneyRecord moneyRecord = moneyRecordService.selectRecordByDrawMoneyId(request.getDrawMoneyId().toString());
            moneyRecord.setRecordType(MoneyRecordEnum.PRESENT_COMPLETION.getCode());//提现完成
            moneyRecord.setCause(MoneyRecordEnum.PRESENT_COMPLETION.getMsg());
            if (!moneyRecordService.updateByDrawMoneyId(moneyRecord)){
                throw new DataException("交易记录表修改失败");
            }


            try {
                taskService.noticeOfWithdrawToSeller(drawMoney.getFormId(),drawMoney.getDrawMoney(),drawMoney.getOpenid());
            } catch (Exception e) {
				e.printStackTrace();
                log.error("提现通知错误",e);
            }

            //提现到账通知卖家
            sendStationMsgService.sendWithdrawalToAccunt(wxUser.getOpenid(),drawMoney.getDrawMoney()*1.00/100);

        }catch (Exception e){
            return response.build(RespStatus.DRAW_MONEY_FAIL);
        }

		return response;
	}

	/**
	 * 获取银行卡信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/yunxian/getBankInfo",method = RequestMethod.POST)
	public UserBankInfoResponse getBankInfo(@RequestBody SessionRequest request){
		final UserBankInfoResponse response = new UserBankInfoResponse();

		WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

		if(wxUser == null && StringUtils.isBlank(request.getOpenid())){
			return (UserBankInfoResponse)response.build(RespStatus.SESSION_ID_EXPIRE);
		}

		try {
			response.setUserBankInfo(drawMoneyService.getBankInfoVO(
					wxUser != null ? wxUser.getOpenid() : request.getOpenid()
			));
		}catch (Exception e){
			e.printStackTrace();
			return (UserBankInfoResponse)response.build(RespStatus.SYSTEM_ERROR);
		}

		return response;
	}

	/**
	 * 获取所有银行
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/yunxian/getAllBank",method = RequestMethod.POST)
	public GetAllBankResponse getAllBank(){
		final GetAllBankResponse response = new GetAllBankResponse();

		try {
			response.setBankList(drawMoneyService.getAllBank());
		}catch (Exception e){
			e.printStackTrace();
			return (GetAllBankResponse)response.build(RespStatus.SYSTEM_ERROR);
		}

		return response;
	}

	/**
	 * 获取所有省份(直辖市)
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/yunxian/getAllProvince",method = RequestMethod.POST)
	public GetAllProvinceResponse getAllProvince(){
		final GetAllProvinceResponse response = new GetAllProvinceResponse();

		try {
			response.setCityList(drawMoneyService.getAllProvince());
		}catch (Exception e){
			e.printStackTrace();
			return (GetAllProvinceResponse)response.build(RespStatus.SYSTEM_ERROR);
		}

		return response;
	}

	/**
	 * 获取城市(区)
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/yunxian/getCityByProvince",method = RequestMethod.POST)
	public GetAllProvinceResponse getCityByProvince(CityRequest request){
		final GetAllProvinceResponse response = new GetAllProvinceResponse();

		try {
			response.setCityList(drawMoneyService.selectByProvince(request.getParentCode()));
		}catch (Exception e){
			e.printStackTrace();
			return (GetAllProvinceResponse)response.build(RespStatus.SYSTEM_ERROR);
		}

		return response;
	}



	/**
	 * 补全银行卡信息
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/yunxian/completeBankInfo",method = RequestMethod.POST)
	public BaseResponse completeBankInfo(CompleteBankInfoRequest request){
		final BaseResponse response = new BaseResponse();

		WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

		if(wxUser == null){
			return response.build(RespStatus.SESSION_ID_EXPIRE);
		}

		//判断是否为信用卡
		String url =
				"https://ccdcapi.alipay.com/validateAndCacheCardInfo.json?cardNo="+request.getBankAb()+"&cardBinCheck=true";
		String param = HttpUtil.httpGet(url);

		if(!request.getAccountNo().substring(0,1).equals("62")){
			return response.build(RespStatus.NOT_BANK_YINGLIAN);
		}


		if(param != null){
			JSONObject jsonObject = JSONObject.parseObject(param);

			String result = jsonObject.getString("cardType");

			if(StringUtils.isNotBlank(result) && result.equals("CC")){
				return response.build(RespStatus.NOT_BANK_CC);
			}
		}

		try {
			drawMoneyService.completeBankInfo(request);
		}catch (Exception e){
			e.printStackTrace();
			return response.build(RespStatus.SYSTEM_ERROR);
		}

		return response;
	}



}
