package com.hc9.web.main.service.dadabus.encrypt;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import it.sauronsoftware.base64.Base64;

/** RSA算法工具类 */
public class RsaUtil {
	/** 嗒嗒巴士公钥  */  
    private static RSAPublicKey dadaBusPublicKey;
    
    /** hc9私钥  */  
    private static RSAPrivateKey hc9PrivateKey;
    
    static {
//    	if(dadaBusPublicKey == null) {
//    		String dadaBusPublicKeyFilePath = DadaBusConfigInfo.getDadaBusPublicKey();
//    		try {
//				dadaBusPublicKey = DadaRSAEncrypt.loadDadaBusPublicKey(dadaBusPublicKeyFilePath);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//    	}
//    	if(hc9PrivateKey == null) {
//    		String hc9PrivateKeyFilePath = DadaBusConfigInfo.getQhhcPrivateKey();
//    		try {
//    			hc9PrivateKey = DadaRSAEncrypt.loadHc9PrivateKey(hc9PrivateKeyFilePath);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//    	}
    }
    
	/** 使用嗒嗒公钥加密后的请求参数字符串密文  */
	public static String encryptByDadaBusPublicKey(String paramStr) {
		try {
			/** 加密生成字节数组 */
	        byte[] cipher = DadaRSAEncrypt.encryptByPublicKey(dadaBusPublicKey, paramStr.getBytes("utf-8"));
	        /** 对字节数组进行base64编码处理 */
	        String result = new String(Base64.encode(cipher), "utf-8");
			return result;
		} catch (Exception e) {
			throw new RuntimeException("嗒嗒巴士加密加密过程中出现错误！");
		}  
	}
	
	/** 使用hc9的私钥对参数进行加密处理 */
	public static String encryptByHc9PrivateKey(String paramStr) {
		try {
			//加密  
	        byte[] cipher = DadaRSAEncrypt.encryptByPrivateKey(hc9PrivateKey, paramStr.getBytes());
	        String result = new String(Base64.encode(cipher));
			return result;
		} catch (Exception e) {
			throw new RuntimeException("hc9私钥加密过程中出现错误！");
		}
	}
}
