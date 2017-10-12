package com.hc9.web.main.service.dadabus.encrypt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.hc9.web.main.util.StringUtil;
import com.jubaopen.commons.LOG;

/** 嗒嗒巴士配置信息读取类  */
public class DadaBusConfigInfo {
	private static Properties pro = null;
	/** 嗒嗒巴士接口所在地址 */
	private static String dadaBusServiceUrl = "";
	/** 获取嗒嗒巴士公钥所在地址 */
	private static String dadaBusPublicKey = "";
	
	/** 获取前海红筹私钥所在地址 */
	private static String qhhcPrivateKey = "";
	
	/** 获取嗒嗒巴士为前海红筹分配的合作方代码 */
	private static String qhhcDadaBusCode = "";
	
	/** 嗒嗒巴士代金券类型id对应的map */
	private static Map<String, String> cashTypeIdMap = null;
	
	/** 嗒嗒巴士抢票时间-车票乘坐时间映射map */
	private static Map<String, String> giveAndUseTimeMap = null;
	
	static{
		try {
			 pro = PropertiesLoaderUtils.loadAllProperties("config/outside/dadabus.properties");
		} catch (IOException e) {
			LOG.error("加载哒哒巴士相关配置信息出错！", e);
		}
	} 
	
	/** 哒哒巴士接口所在地址 */
	public static String getDadaBusServiceUrl() {
		if(StringUtil.isBlank(dadaBusServiceUrl)) {
			dadaBusServiceUrl = pro.getProperty("DADABUSSERVICEURL");
		}
		return dadaBusServiceUrl;
	}
	
	/** 获取哒哒巴士公钥所在地址 */
	public static String getDadaBusPublicKey() {
		if(StringUtil.isBlank(dadaBusPublicKey)) {
			dadaBusPublicKey = pro.getProperty("DADABUSPUBLICKEY");
		}
		return dadaBusPublicKey;
	}
	
	/** 获取前海红筹私钥所在地址 */
	public static String getQhhcPrivateKey() {
		if(StringUtil.isBlank(qhhcPrivateKey)) {
			qhhcPrivateKey = pro.getProperty("QHHCPRIVATEKEY");
		}
		return qhhcPrivateKey;
	}
	
	/** 获取哒哒巴士为前海红筹分配的合作方代码 */
	public static String getQhhcDadaBusCode() {
		if(StringUtil.isBlank(qhhcDadaBusCode)) {
			qhhcDadaBusCode = pro.getProperty("DADABUSQHHCCODE");
		}
		return qhhcDadaBusCode;
	}
	
	/** 嗒嗒巴士代金券类型Id map */
	public static Map<String, String> getCashTypeIdMap() {
		if(cashTypeIdMap == null) {
			cashTypeIdMap = new HashMap<String, String>();
			String cashTypeIdStr = pro.getProperty("DADABUSCASHTYPEID");
			String[] typeIdArr = cashTypeIdStr.split(",");
			for(String str: typeIdArr) {
				String[] idArr = str.split(":");
				cashTypeIdMap.put(idArr[0], idArr[1]);
			}
		}
		return cashTypeIdMap;
	}
	
	/** 嗒嗒巴士抢票时间-车票乘坐时间映射map */
	public static Map<String, String> getGiveAndUseTimeMap() {
		if(giveAndUseTimeMap == null) {
			giveAndUseTimeMap = new HashMap<String, String>();
			giveAndUseTimeMap.put("2015-12-15", "2016-02-01");
			giveAndUseTimeMap.put("2015-12-16", "2016-02-01");
			giveAndUseTimeMap.put("2015-12-17", "2016-02-02");
			giveAndUseTimeMap.put("2015-12-18", "2016-02-03");
			giveAndUseTimeMap.put("2015-12-19", "2016-02-04");
			giveAndUseTimeMap.put("2015-12-20", "2016-02-05");
			giveAndUseTimeMap.put("2015-12-21", "2016-02-06");
			giveAndUseTimeMap.put("2015-12-22", "2016-02-07");
			giveAndUseTimeMap.put("2015-12-23", "2016-02-04");
			giveAndUseTimeMap.put("2015-12-24", "2016-02-05");
			giveAndUseTimeMap.put("2015-12-25", "2016-02-06");
		}
		return giveAndUseTimeMap;
	}
}