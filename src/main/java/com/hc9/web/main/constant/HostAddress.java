package com.hc9.web.main.constant;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.hc9.web.main.util.LOG;
/**
 * 获取主机地址路径信息
 *
 */
public class HostAddress {
	private static Properties pro = null;
	static{
		try {
			 pro = PropertiesLoaderUtils.loadAllProperties("config/user/host.properties");
		} catch (IOException e) {
			LOG.error("获取资源文件失败");
		}
	} 
	/**
	 * 获取主机路径
	 * @return
	 */
	public static String getHostAddress(){
		return pro.getProperty("https.host");
	}
	
	/**
	 * 获取资源路径
	 * @return
	 */
	public static String getResourceAddress(){
		return pro.getProperty("static.host");
	}
}
