package com.hc9.web.main.service.baofo;

import java.util.HashMap;
import java.util.Map;

import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;

/**
 * 
 * 根据用户传入的数据访问环讯
 * 
 * @author frank 2014-07-03 用getMercode()代替 getCert() 2014-7-24
 */
public class RechargeInfoService {
	
	/**
	 * 
	 * 充值 XML
	 * @param rechargeCall
	 * @param key
	 * @param userid
	 * @param amount
	 * @return
	 */
	public static Map<String, String> rechargeCall(String rechargeCall,String key) {
		// 将参数装进map里面
		String bfsign = CommonUtil.MD5( rechargeCall +"~|~"+ key);
		Map<String, String> map = new HashMap<String, String>();
		map.put("requestParams", rechargeCall);
		map.put("sign", bfsign);
		map.put("merchant_id", ParameterIps.getCert());
		map.put("terminal_id", ParameterIps.getTerminalnuMber());
		map.put("page_url", Constant.RECHARGEURL);
		map.put("service_url", Constant.ASYNCHRONISMRECHARGE);
		return map;
	}
	
	/**
	 * 提现XML
	 * @param rechargeCall
	 * @param key
	 * @param userid
	 * @param amount
	 * @return
	 */
	public static Map<String, String> withdrawalCall(String rechargeCall,String key) {
		// 将参数装进map里面
		String bfsign = CommonUtil.MD5( rechargeCall +"~|~"+ key);
		Map<String, String> map = new HashMap<String, String>();
		map.put("requestParams", rechargeCall);
		map.put("sign", bfsign);
		map.put("merchant_id", ParameterIps.getCert());
		map.put("terminal_id", ParameterIps.getTerminalnuMber());
		map.put("page_url", Constant.WITHDRAWAL);
		map.put("service_url", Constant.WITHDRAWASYNCHRONOUS);
		return map;
	}
	
	/***
	 * 授权协议（页面接口）
	 * @param userId
	 * @return
	 */
	public static Map<String, String> inAccreditCall(String userId){
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", ParameterIps.getCert());
		map.put("terminal_id", ParameterIps.getTerminalnuMber());
		map.put("user_id", userId);
		map.put("service_url", Constant.ASYNCHRONISMINACCREDIT);
		map.put("page_url", Constant.INACCREDIT);
		return map;
	}
	
	/****
	 * 用户注册宝付授权
	 * @param userId
	 * @return
	 */
	public static Map<String, String> inAccreditUserCall(String userId){
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", ParameterIps.getCert());
		map.put("terminal_id", ParameterIps.getTerminalnuMber());
		map.put("user_id", userId);
		map.put("service_url", Constant.ASYNCHRONISMINACCREDITUSER);
		map.put("page_url", Constant.INACCREDITUSRE);
		return map;
	}
}
