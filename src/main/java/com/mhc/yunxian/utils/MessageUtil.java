package com.mhc.yunxian.utils;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.utils
 * @author: 昊天
 * @date: 2019/1/24 2:20 PM
 * @since V1.1.0-SNAPSHOT
 */
public class MessageUtil {

    /**
     *  把消息内容中占位符替换成相应的结果
     *  方法重载，根据参数条件不同返回不同类型的内容
     *  可选参数：dragonName，addr，price，sendTime，dragonTitle，userName
     */

    private static final String regex = "(?<=\\【)[^】]*(?=\\】)";

    /**
     * 使用于消息类型0，10
     * @param msg
     * @param dragonName 接龙名称
     * @return
     */
    public static String replaceMsg(String msg,String dragonName){
        if(StringUtils.isBlank(msg) || StringUtils.isBlank(dragonName)){
            return "";
        }
        List<String> matchers = getMatchers(regex, msg);
        if(CollectionUtils.isEmpty(matchers) || matchers.size()< 1){
            return "";
        }
        return msg.replace(matchers.get(0), dragonName);
    }

    /**
     * 适用于消息类型9
     * @param msg
     * @param price （商品价格）
     * @return
     */
    public static String replaceMsg(String msg,Double price){
        if(StringUtils.isBlank(msg) || null == price){
            return "";
        }
        List<String> matchers = getMatchers(regex, msg);
        if(CollectionUtils.isEmpty(matchers) || matchers.size()< 1){
            return "";
        }
        return msg.replace("【"+matchers.get(0)+"】", "<span style='color:red;'>"+price+"</span>");
    }

    /**
     * 适用于消息类型 1，7，8
     * @param msg
     * @param args1  接龙名称 / 用户名称
     * @param args2  自提地点 /接龙标题
     * @return
     */
    public static String replaceMsg(String msg,String args1,String args2){
        if(StringUtils.isBlank(msg) || StringUtils.isBlank(args1) || StringUtils.isBlank(args2)){
            return "";
        }
        List<String> matchers = getMatchers(regex, msg);
        if(CollectionUtils.isEmpty(matchers) || matchers.size()< 2){
            return "";
        }
        return msg.replace(matchers.get(0), args1).replace(matchers.get(1),args2);
    }

    /**
     * 适用于消息类型 6
     * @param msg
     * @param dragonName 接龙名称
     * @param sendTime   发货时间
     * @return
     */
    public static String replaceMsg(String msg,String dragonName,Integer sendTime){
        if(StringUtils.isBlank(msg) || StringUtils.isBlank(dragonName) || null ==sendTime){
            return "";
        }
        List<String> matchers = getMatchers(regex, msg);
        if(CollectionUtils.isEmpty(matchers) || matchers.size()< 2){
            return "";
        }
        return msg.replace(matchers.get(0), dragonName).replace(matchers.get(1),"<span style='color:red;'>"+sendTime+"小时</span>");
    }

    /**
     * 适用于消息类型 2，3，4，5
     * @param msg
     * @param dragonName 接龙名称
     * @param price 商品价格
     * @return
     */
    public static String replaceMsg(String msg,String dragonName,Double price){
        if(StringUtils.isBlank(msg) || StringUtils.isBlank(dragonName) || null == price){
            return "";
        }
        List<String> matchers = getMatchers(regex, msg);
        if(CollectionUtils.isEmpty(matchers) || matchers.size()< 2){
            return "";
        }
        return msg.replace(matchers.get(0), dragonName).replace("【"+matchers.get(1)+"】","<span style='color:red;'>"+price+"</span>");
    }


    /**
     * <p> 根据正则表达式找出匹配的字符串集合 </p>
     * @param regex   表达式内容
     * @param source  源字符串
     * @return List<String>
     * @author 昊天
     * @date 2019/1/24 4:05 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public static List<String> getMatchers(String regex, String source){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }




    public static void main(String[] args) {
        String s = "你在【接龙名称】的订单已经发货超过【发货时间】，请及时收货，发货24小时后即将自动收货。如已取货请点击进入操作收货。";
        String s1 = MessageUtil.replaceMsg(s, "别打脸的接龙",4);
        System.out.println(s1);
        System.out.println((1*1.00f)/100);
    }
}
