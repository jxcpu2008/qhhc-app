package com.hc9.web.main.service.baofo;

import java.util.HashMap;
import java.util.Map;

import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.util.CommonUtil;

/** 注册相关 */
public class RegisterService {
	/**
	 * 加密信息并返回加密后的信息
	 * 
	 * @param registerCall 提交信息的xml文件
	 * @return 返回加密后的文件集合
	 */
	public static Map<String, String> registerCall(String registerCall) {
		// 将参数装进map里面
		String bfsign = CommonUtil.aesEncryptKey16(registerCall,
				ParameterIps.getDes_algorithm());
		Map<String, String> map = new HashMap<String, String>();
		map.put("requestParams", registerCall);
		map.put("sign", bfsign);
		map.put("terminal_id", ParameterIps.getTerminalnuMber());
		map.put("merchant_id", ParameterIps.getCert());
		return map;
	}
}