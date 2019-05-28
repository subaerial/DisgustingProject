package com.mhc.yunxian.utils;

import java.util.Random;

/**
 * Created by Administrator on 2018/4/16.
 */
public class RandomCodeUtil {
	public static String getRandomCode(int codeLength) {
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		int length = str.length();
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < codeLength; i++) {
			int number = random.nextInt(length);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}
}
