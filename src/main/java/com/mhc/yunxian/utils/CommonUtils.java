package com.mhc.yunxian.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class CommonUtils {

    private static String appid = "wxc38768c84fd437b6";

    private static String secret = "638f506b89aa993c2576cc8c2da4b1b3";


    @Autowired
    private Environment environment;


    public static String getImageString(byte[] data) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();
        return data != null ? encoder.encode(data) : "";
    }


    public static Boolean byteArray2ImageFile(byte[] data, File file) throws Exception {
        boolean flag = false;
        if (!file.getParentFile().exists()) {
            file.mkdirs();
        }
        if (file.createNewFile()) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
            int temp;
            while ((temp = dis.read()) != -1) {
                dos.write(temp);
            }
            dos.flush();
            dos.close();
            dis.close();
            flag = true;
        }
        return flag;
    }

    /**
     * @param imageStr base64编码字符串
     * @param file     生成文件对象
     * @return
     * @Description: 将base64编码字符串转换为图片
     */
    public static boolean generateImage(String imageStr, File file) throws Exception {
        boolean flag = false;
        if (imageStr != null) {
            if (!file.getParentFile().exists()) {
                file.mkdirs();
            }
            if (file.createNewFile()) {
                BASE64Decoder decoder = new BASE64Decoder();
                // 解密
                byte[] b = decoder.decodeBuffer(imageStr);
                // 处理数据
                for (int i = 0; i < b.length; ++i) {
                    if (b[i] < 0) {
                        b[i] += 256;
                    }
                }
                OutputStream out = new FileOutputStream(file);
                out.write(b);
                out.flush();
                out.close();
                flag = true;
            }
        }
        return flag;
    }


    public static List removeDuplicate(List list) {
        List listTemp = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            if (!listTemp.contains(list.get(i))) {
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }

    public static String uploadHeadImg(String url) throws Exception {
        String imageName = imageName() + ".jpg";    //图片名称
        String path = new File("").getAbsolutePath() + "/";
        OSSClientUtil ossClientUtil = new OSSClientUtil(); //初始化上传客户端
        download(url, imageName, path);   //下载图片到本地（见下面）
        File file = new File(path + imageName);   //根据下载的文件创建新文件
        //文件不存在，则上传默认头像
        if (!file.exists()) {
            return null;
        }

        FileInputStream input = new FileInputStream(file);

        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + imageName;

        String key = ossClientUtil.uploadFile2OSS(input, fileName);
        String result = ossClientUtil.getImgUrl(fileName, 3600L * 1000 * 24 * 365 * 10);

        return result;
    }


    /**
     * 下载图片到本地
     *
     * @param urlString
     * @param filename
     * @param savePath
     * @throws Exception
     */
    public static void download(String urlString, String filename, String savePath) throws Exception {
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(5 * 1000);
        // 输入流
        InputStream is = con.getInputStream();
        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        OutputStream os = new FileOutputStream(sf.getPath() + "/" + filename);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }

    /**
     * 文件夹名称-年月
     *
     * @return
     */
    public static String folderNameYM() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
        String strDate = formatter.format(new Date());
        return strDate;
    }

    /**
     * 文件夹名称-天
     *
     * @return
     */
    public static String folderNameD() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        String strDate = formatter.format(new Date());
        return strDate;
    }


    /**
     * 图片名称生成
     *
     * @return
     */
    public static String imageName() {
        Random random = new Random();//生成随机数
        String strDate = Long.toString(System.currentTimeMillis());
        for (int i = 0; i < 3; i++) {
            strDate = strDate + random.nextInt(9);
        }
        return strDate;
    }


    /**
     * 获取WX_ACCESS_TOKEN
     *
     * @return
     */
    public static JSONObject getNewAccessToken() {

        JSONObject jsonObject = new JSONObject();

        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&" +
                "appid=" + appid +
                "&secret=" + secret;

        String param = HttpUtil.httpGet(url);

        if (param != null) {
            jsonObject = JSONObject.parseObject(param);

        } else {

            jsonObject.put("errcode", -1);
            jsonObject.put("errmsg", "GET返回为空");

        }

        return jsonObject;

    }


    /**
     * 自动回复tesy
     *
     * @return
     */
    public static void autoReply(String openid, String content, String accessToken) {

        JSONObject jsonObject = new JSONObject();

        String jsonStrs = "{\"touser\":\"" + openid + "\",\"msgtype\":\"text\",\"text\":{ \"content\":\"" + content + "\"}}";


        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + accessToken;

        String param = HttpUtil.httpPost(url, jsonStrs);

        if (param != null) {
            log.error("POST返回为空");
        } else {

            if (jsonObject.getString("errcode") != null && !jsonObject.getString("errcode").equals("0")) {
                log.error("自动回复失败:" + jsonObject.getString("errmsg"));
            } else {
                log.info("自动回复成功");
            }

        }

    }
}
