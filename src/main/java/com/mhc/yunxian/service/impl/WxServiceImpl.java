package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.TransfersReponse;
import com.mhc.yunxian.dao.WxAccessTokenDao;
import com.mhc.yunxian.dao.WxAdminDao;
import com.mhc.yunxian.dao.WxAutoReplyDao;
import com.mhc.yunxian.dao.WxUnionDao;
import com.mhc.yunxian.dao.model.WxAccessToken;
import com.mhc.yunxian.dao.model.WxAdmin;
import com.mhc.yunxian.dao.model.WxAutoReply;
import com.mhc.yunxian.dao.model.WxUnion;
import com.mhc.yunxian.service.WxService;
import com.mhc.yunxian.service.WxUserService;
import com.mhc.yunxian.utils.CommonUtils;
import com.mhc.yunxian.utils.PaymentUtil;
import com.mhc.yunxian.utils.RandomCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WxServiceImpl implements WxService {


    private String appid = "wxc38768c84fd437b6";

    private String mchId = "1504015191";

    private String secretKey = "3233jkdddDDF454Fddddf2seFDEDd3dD";

    private String transfersUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";

    @Autowired
    private WxAccessTokenDao wxAccessTokenDao;

    @Autowired
    private WxAutoReplyDao wxAutoReplyDao;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private WxAdminDao wxAdminDao;

    @Autowired
    private WxUnionDao wxUnionDao;

    @Override
    public WxAccessToken getWxAccessToken() {

        return wxAccessTokenDao.selectByPrimaryKey(1);

    }

    @Override
    public int insertWxAccessToken(WxAccessToken wxAccessToken) {
        return wxAccessTokenDao.insertSelective(wxAccessToken);
    }

    @Override
    public int updateWxAccessToken(WxAccessToken wxAccessToken) {
        return wxAccessTokenDao.updateByPrimaryKeySelective(wxAccessToken);
    }

    @Override
    @Async
    public void autoReply(Map<String, String> map) {

        if(map.get("MsgType").equals("text")){
            String keyword = map.get("Content");

            String openid = map.get("FromUserName");

            if(StringUtils.isNotBlank(openid)){

                WxAutoReply wxAutoReply = wxAutoReplyDao.selectByKeyword(keyword);

                if(wxAutoReply != null){
                    CommonUtils.autoReply(openid,wxAutoReply.getMessage(),wxUserService.getWxAccessToKenInfo());
                }


            }else{
                log.error("文本错误，OPENID为空");
            }
        }else{
            log.error("不符合自动回复格式");
        }

    }

    @Override
    public List<WxAutoReply> getKeywordList() {
        return wxAutoReplyDao.selectAll();
    }

    @Override
    public WxAutoReply selectByKeyword(String keyword) {
        return wxAutoReplyDao.selectByKeyword(keyword);
    }

    @Override
    public WxAutoReply selectById(Integer id) {
        return wxAutoReplyDao.selectByPrimaryKey(id);
    }

    @Override
    public int insertWxAutoReply(WxAutoReply wxAutoReply) {
        return wxAutoReplyDao.insertSelective(wxAutoReply);
    }

    @Override
    public int deleteWxAutoReply(Integer id) {
        return wxAutoReplyDao.deleteByPrimaryKey(id);
    }

    @Override
    public WxAdmin getWxAdmin(String openid) {
        return wxAdminDao.selectByOpenid(openid);
    }

    @Override
    public int updateWxAdmin(WxAdmin wxAdmin) {
        return wxAdminDao.updateByPrimaryKeySelective(wxAdmin);
    }

    @Override
    public int addWxAdmin(WxAdmin wxAdmin) {
        return wxAdminDao.insertSelective(wxAdmin);
    }

    @Override
    public TransfersReponse payToUserWx(String partnerTradeNo, String openid, Integer drawMoney, String ip) {
        TransfersReponse reponse = new TransfersReponse();

        log.info("微信提现支付开始");

        try {
            //生成的随机字符串
            String nonce_str = RandomCodeUtil.getRandomCode(32);
            //支付金额，单位：分，这边需要转成字符串类型，否则后面的签名会失败
            String amount = drawMoney.toString();

            Map<String, String> packageParams = new HashMap<String, String>();
            //商户账号appid
            packageParams.put("mch_appid", appid);
            //商户号
            packageParams.put("mchid", mchId);
            //随机字符串
            packageParams.put("nonce_str", nonce_str);
            //商户订单号
            packageParams.put("partner_trade_no", partnerTradeNo);
            //用户OPENID
            packageParams.put("openid", openid);
            //校验用户姓名选项
            packageParams.put("check_name", "NO_CHECK");
            //支付金额，这边需要转成字符串类型，否则后面的签名会失败
            packageParams.put("amount", amount);
            //企业付款备注
            packageParams.put("desc", "云鲜提现");
            //ip
            packageParams.put("spbill_create_ip", ip);

            // 除去数组中的空值和签名参数
            packageParams = PaymentUtil.paraFilter(packageParams);
            // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
            String prestr = PaymentUtil.createLinkString(packageParams);
            //MD5运算生成签名
            String mysign = PaymentUtil.sign(prestr, secretKey, "utf-8").toUpperCase();

            //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去
            String xml = "<xml>" + "<mch_appid>" + appid + "</mch_appid>"
                    + "<mchid>" + mchId + "</mchid>"
                    + "<nonce_str>" + nonce_str + "</nonce_str>"
                    + "<partner_trade_no>" + partnerTradeNo + "</partner_trade_no>"
                    + "<openid>" + openid + "</openid>"
                    + "<check_name>" + "NO_CHECK" + "</check_name>"
                    + "<amount>" + amount + "</amount>"
                    + "<desc>" + "云鲜提现" + "</desc>"
                    + "<spbill_create_ip>" + ip + "</spbill_create_ip>"
                    + "<sign>" + mysign + "</sign>"
                    + "</xml>";

            System.out.println("调试模式_零钱支付接口 请求XML数据：" + xml);
            //执行退款
            String result = payToUser(transfersUrl, xml);
            System.out.println("调试模式_零钱支付接口 返回XML数据：" + result);

            // 将解析结果存储在HashMap中
            Map map = PaymentUtil.doXMLParse(result);
            //返回状态码
            String return_code = (String) map.get("return_code");
            String result_code = (String) map.get("result_code");
            //返回给移动端需要的参数

            if (return_code.equals("SUCCESS") && result_code.equals("SUCCESS")) {

                reponse.setResult(true);
                reponse.setPaymentNo((String) map.get("payment_no"));
                reponse.setPaymentTime((String) map.get("payment_time"));

            }else if(return_code.equals("SUCCESS") && !result_code.equals("SUCCESS")){
                log.error("微信提现支付失败: err_code:{},err_code_des:{}",
                        (String) map.get("err_code"),
                        (String) map.get("err_code_des"));
                reponse.setResult(false);
                reponse.setMsg((String) map.get("err_code_des"));
            }else{
                log.error("微信提现支付失败: msg:{}",xml);
                reponse.setMsg("微信提现支付失败");
                reponse.setResult(false);
            }

        }catch (Exception e){
            e.printStackTrace();
            log.error("微信提现支付失败",e);
            reponse.setMsg("微信提现支付失败");
            reponse.setResult(false);
        }
        return reponse;
    }

    @Override
    public List<WxAdmin> selectAdmin() {
        return wxAdminDao.selectAdmin();
    }

    /**
     * 保存关注公众号用户unionId信息
     * @param wxUnion
     */
    @Override
    public void saveWxInfo(WxUnion wxUnion) {
        wxUnionDao.insertSelective(wxUnion);
    }

    /**
     * 编辑关注公众号用户unionId信息
     * @param wxUnion
     */
    @Override
    public void updateWxInfo(WxUnion wxUnion) {
        wxUnionDao.updateByOpenId(wxUnion);
    }

    /**
     * 根据id主键更新关注公众号用户wxUnion记录
     * @param wxUnion
     */
    @Override
    public void updateWxUnionById(WxUnion wxUnion) {
        wxUnionDao.updateByPrimaryKeySelective(wxUnion);
    }

    /**
     * 微信零钱支付请求
     *
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    private String payToUser(String url, String data) throws Exception {
        System.out.println("进入微信零钱支付请求函数");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        FileInputStream is = new FileInputStream(new File("/usr/local/yunxian/apiclient_cert.p12"));
        try {
            keyStore.load(is, "1504015191".toCharArray());
        } finally {
            is.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(
                keyStore,
                "1504015191".toCharArray())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
        );
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {
            HttpPost httpost = new HttpPost(url); // 设置响应头信息
            httpost.addHeader("Connection", "keep-alive");
            httpost.addHeader("Accept", "*/*");
            httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpost.addHeader("Host", "api.mch.weixin.qq.com");
            httpost.addHeader("X-Requested-With", "XMLHttpRequest");
            httpost.addHeader("Cache-Control", "max-age=0");
            httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
            httpost.setEntity(new StringEntity(data, "UTF-8"));
            CloseableHttpResponse response = httpclient.execute(httpost);
            try {
                HttpEntity entity = response.getEntity();

                String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                EntityUtils.consume(entity);
                return jsonStr;
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
}
