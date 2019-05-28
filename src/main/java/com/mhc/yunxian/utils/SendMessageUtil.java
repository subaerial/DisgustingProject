package com.mhc.yunxian.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.mhc.yunxian.dao.SystemParamDao;
import com.mhc.yunxian.dao.model.SystemParam;
import com.mhc.yunxian.enums.DifTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class SendMessageUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public static String YourSignName = "云鲜";// 短信签名名称， 需要修改


    /*模版内容:您的接龙：${title} 有新付款下单，客户名称：${name}，请您及时查收！*/

    //产品名称:云通信短信API产品,开发者无需替换
    static final String product = "Dysmsapi";
    //产品域名,开发者无需替换
    static final String domain = "dysmsapi.aliyuncs.com";

    // TODO 此处需要替换成开发者自己的AK(在阿里云访问控制台寻找)
    static final String accessKeyId = "LTAI6gHwoTVG0CkE";
    static final String accessKeySecret = "ncdF9rb9we0sNSSkZtveQBXYlRe9dE";

    public static SendSmsResponse sendSms(String mobile, String title , String name) throws ClientException {

        String YourSMSTemplateCode = "SMS_139972035";// 短信模板code 需要修改
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(mobile);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(YourSignName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(YourSMSTemplateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
//        request.setTemplateParam("{\"title\":\"" + code + "\"}");

        if(title.length() >= 20){
            title = title.substring(0,17) + "...";
        }

        if(name.length() >= 20){
            name = name.substring(0,17) + "...";
        }

        request.setTemplateParam("{\"title\":\"" + title + "\",\"name\":\"" + name + "\"}");

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        log.info("给以下用户发送发布接龙短信:"+mobile);

        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        return sendSmsResponse;
    }



    /*补差订单通知*/
    public static SendSmsResponse sendSmsDif(Integer price,String mobile, String title,Integer orderType) throws ClientException {

        String YourSMSTemplateCode;
        String templateParam;
        if(DifTypeEnum.RETREAT_ORDER.getType().equals(orderType)) {
            //退差
            YourSMSTemplateCode = "SMS_158947755";
            templateParam = "{\"title\":\"" + title + "\",\"money\":\"" + BigDecimal.valueOf(price).divide(BigDecimal.valueOf(100)) + "\"}";

        }else{
            //补差
            YourSMSTemplateCode = "SMS_140525042";
            templateParam = "{\"title\":\"" + title + "\"}";
        }
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(mobile);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(YourSignName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(YourSMSTemplateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
//        request.setTemplateParam("{\"title\":\"" + code + "\"}");
        if(title.length() >= 20){
            title = title.substring(0,17) + "...";
        }

        request.setTemplateParam(templateParam);

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        log.info("给以下用户发送补差短信:"+mobile);

        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        return sendSmsResponse;
    }

    /*发货通知*/
    public static SendSmsResponse sendSmsDeliver(String mobile, String title , String address) throws ClientException {

        String YourSMSTemplateCode = "SMS_140560132";// 短信模板code 需要修改
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(mobile);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(YourSignName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(YourSMSTemplateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
//        request.setTemplateParam("{\"title\":\"" + code + "\"}");

        if(title.length() >= 20){
            title = title.substring(0,17) + "...";
        }

        if(address.length() >= 20){
            address = address.substring(0,17) + "...";
        }

        request.setTemplateParam("{\"title\":\"" + title + "\",\"address\":\"" + address + "\"}");

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        log.info("给以下用户发送发布发货短信:"+mobile);

        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        return sendSmsResponse;
    }

    /*卖家提现通知*/
    public static SendSmsResponse sendSmsSeller(String seller, String mobile) throws ClientException {

        String YourSMSTemplateCode = "SMS_141605075";// 短信模板code 需要修改

        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(mobile);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(YourSignName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(YourSMSTemplateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
//        request.setTemplateParam("{\"title\":\"" + code + "\"}");

        if(seller.length() >= 20){
            seller = seller.substring(0,17) + "...";
        }

        request.setTemplateParam("{\"seller\":\"" + seller  + "\"}");

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        log.info("给以下用户发送提现通知短信:"+mobile);

        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        return sendSmsResponse;
    }

    /*买家申请退款通知*/
    public static SendSmsResponse sendSmsRefund(String mobile, String customer , String title) throws ClientException {

        String YourSMSTemplateCode = "SMS_141580077";// 短信模板code 需要修改
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(mobile);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(YourSignName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(YourSMSTemplateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
//        request.setTemplateParam("{\"title\":\"" + code + "\"}");

        if(title.length() >= 20){
            title = title.substring(0,17) + "...";
        }

        if(customer.length() >= 20){
            customer = customer.substring(0,17) + "...";
        }

        request.setTemplateParam("{\"title\":\"" + title + "\",\"customer\":\"" + customer + "\"}");

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        log.info("给以下用户发送买家申请退款短信:"+mobile);

        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

        return sendSmsResponse;
    }



}
