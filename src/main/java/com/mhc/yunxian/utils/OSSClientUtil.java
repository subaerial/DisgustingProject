package com.mhc.yunxian.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Random;

/**
 * Created by Robin on 2017/7/17.
 */
@Component
public class OSSClientUtil {
    Log log = LogFactory.getLog(OSSClientUtil.class);

    @Value("${oss.endpoint}")
    private String endpoint = "https://image.yunxian.shop";
    // accessKey
    @Value("${oss.accessKeyId}")
    private String accessKeyId = "LTAI6gHwoTVG0CkE";
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret = "ncdF9rb9we0sNSSkZtveQBXYlRe9dE";
    //空间
    @Value("${oss.bucketName}")
    private String bucketName = "yunxian";
    //文件存储目录
    @Value("${oss.filedir}")
    private String filedir = "productImages/";


    private OSSClient ossClient = null;

    public OSSClientUtil() {
        log.info(accessKeyId);
        ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 初始化
     */
    public void init() {
        ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 销毁
     */
    public void destory() {
        ossClient.shutdown();
    }

    /**
     * 上传图片
     *
     * @param url
     */
    public void uploadImg2Oss(String url) {
        File fileOnServer = new File(url);
        FileInputStream fin;
        try {
            fin = new FileInputStream(fileOnServer);
            String[] split = url.split("/");
            this.uploadFile2OSS(fin, split[split.length - 1]);
        } catch (FileNotFoundException e) {
            try {
                throw new Exception("图片上传失败!");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public String uploadImg2Oss(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        Random random = new Random();
        String name = random.nextInt(10000) + System.currentTimeMillis() + substring;
        try {
            InputStream inputStream = file.getInputStream();
            this.uploadFile2OSS(inputStream, name);
            return name;
        } catch (Exception e) {
            log.info("============>imgErr：" + e.toString());
            e.printStackTrace();
            return "imgErr";
        }
    }

    public String uploadLocalFile2OSS(String key, File file) {
        return ossClient.putObject(this.bucketName, key, file).getETag();
    }

    public String uploadLocalBytes2OSS(String key, byte[] bytes) {
        return ossClient.putObject(this.bucketName, key, new ByteArrayInputStream(bytes)).getETag();
    }

    /**
     * 获得图片路径
     *
     * @param fileUrl
     * @return
     */
    public String getImgUrl(String fileUrl, Long OSSURLInvalidTime) {
        if (!StringUtils.isEmpty(fileUrl)) {
            String[] split = fileUrl.split("/");
            return this.getUrl(this.filedir + split[split.length - 1], OSSURLInvalidTime);
        }
        return null;
    }

    /**
     * 上传到OSS服务器  如果同名文件会覆盖服务器上的
     *
     * @param instream 文件流
     * @param fileName 文件名称 包括后缀名
     * @return 出错返回"" ,唯一MD5数字签名
     */
    public String uploadFile2OSS(InputStream instream, String fileName) {
        String ret = "";
        try {
            //创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getContentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            //上传文件
            PutObjectResult putResult = ossClient.putObject(this.bucketName, this.filedir + fileName, instream, objectMetadata);
            ret = putResult.getETag();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Description: 判断OSS服务文件上传时文件的contentType
     *
     * @param FilenameExtension 文件后缀
     * @return String
     */
    public static String getContentType(String FilenameExtension) {
        if (FilenameExtension.equalsIgnoreCase("bmp")) {
            return "image/bmp";
        }
        if (FilenameExtension.equalsIgnoreCase("gif")) {
            return "image/gif";
        }
        if (FilenameExtension.equalsIgnoreCase("jpeg") ||
                FilenameExtension.equalsIgnoreCase("jpg") ||
                FilenameExtension.equalsIgnoreCase("png")) {
            return "image/jpeg";
        }
        if (FilenameExtension.equalsIgnoreCase("html")) {
            return "text/html";
        }
        if (FilenameExtension.equalsIgnoreCase("txt")) {
            return "text/plain";
        }
        if (FilenameExtension.equalsIgnoreCase("vsd")) {
            return "application/vnd.visio";
        }
        if (FilenameExtension.equalsIgnoreCase("pptx") ||
                FilenameExtension.equalsIgnoreCase("ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (FilenameExtension.equalsIgnoreCase("docx") ||
                FilenameExtension.equalsIgnoreCase("doc")) {
            return "application/msword";
        }
        if (FilenameExtension.equalsIgnoreCase("xml")) {
            return "text/xml";
        }
        return "image/jpeg";
    }

    /**
     * 获得url链接
     *
     * @param key
     * @return
     */
    public String getUrl(String key, Long OSSURLInvalidTime) {
        // 设置URL过期时间为10年  3600l* 1000*24*365*10
        //Date expiration = new Date(new Date().getTime() + 3600l * 1000 * 24 * 365 * 10);
        Date expiration = new Date(new Date().getTime() + OSSURLInvalidTime);
        // 生成URL
        URL url = ossClient.generatePresignedUrl(this.bucketName, key, expiration);
        if (url != null) {
            return url.toString();
        }
        return null;
    }


}
