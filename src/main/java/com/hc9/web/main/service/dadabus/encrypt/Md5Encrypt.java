package com.hc9.web.main.service.dadabus.encrypt;

import java.security.MessageDigest;

/** MD5加密算法 */
public class Md5Encrypt {
	
	/** MD5 */
	public static String md5EncryptString(String values) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(values.getBytes("UTF-8"));
		} catch (Exception e) {
			System.out.println(values + " 采用MD5加密算法进行加密时报错！");
		}
		
		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}
}