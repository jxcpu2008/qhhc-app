package com.hc9.web.main.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.baofo.BankCardService;
import com.hc9.web.main.util.AppValidator;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.StringUtil;

/** 银行卡相关 */
@Controller
@RequestMapping("/bankcard")
public class BankCardController {
	
	@Resource
    private BankCardService bankCardService;
	
	/** 绑定银行卡时调用宝付接口发送手机短信验证码 */
	@CheckLoginOnMethod
    @RequestMapping(value="/sendSmsCodeForBindCard",method = RequestMethod.POST)
   	@ResponseBody
    public String sendSmsCodeForBindCard(HttpServletRequest request) {
    	Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		//安全校验
		if(user == null){
			return generateAppData(-1, "请重新登录!");
		}
    	Map<String, Object> resultMap = bankCardService.sendSmsCodeForBindCard(user.getId());
    	return JsonUtil.toJsonStr(resultMap);
    }

	/** 绑定银行卡接口 */
	@CheckLoginOnMethod
    @RequestMapping(value="/bindBankCard",method = RequestMethod.POST)
   	@ResponseBody
    public String bindBankCard(HttpServletRequest request, 
    		String bankCardNo, String validCode, String imageCode, 
    		String bankName, String proValue, String cityValue, String bankAddress) {
    	try {
    		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
    				getAttribute(Constant.SESSION_USER);
    		//安全校验
			if(user == null){
				return generateAppData(-1, "请重新登录!");
			}
			// 取验证码
			String validate = (String) request.getSession().getAttribute(
					"user_login");
			if(StringUtil.isBlank(validate)) {
	    		return generateAppData(1, "图形验证码不能为空!");
	    	}
			if(!validate.equalsIgnoreCase(imageCode)) {
				return generateAppData(2, "图形验证码不匹配!");
			}
    		long userId = user.getId();
	    	if (StringUtil.isBlank(bankCardNo)) {
	    		return generateAppData(4, "银行卡号不能为空!");
	    	}
			//验证码
			if (StringUtil.isBlank(validCode)) {
				return generateAppData(5, "绑定失败,验证码不能为空!");
			}
			//银行卡号
			if (!AppValidator.isBankCard(bankCardNo)) {
				return generateAppData(6, "绑定失败,银行卡号不合法!");
			}
	    	Map<String, Object> resultMap = bankCardService.bindBankCard(userId, bankCardNo, validCode, 
	        		bankName, proValue, cityValue, bankAddress);
	    	return JsonUtil.toJsonStr(resultMap);
    	} catch (Exception e) {
			LOG.error("------------------------查询用户绑定的银行卡时,出现异常!--------------------------", e);
			return generateAppData(1, "系统异常,请稍后重试!");
		}
    }
	
	/**
	 * 封装返回参数
	 * @param rcode 操作类型 0成功 1失败 -1失效
	 * @param msg 返回信息
	 * @return Map<String, Object>
	 */
	private String generateAppData(int code, String msg) {
		Map<String, Object> result = new HashMap<>();
		result.put("code", code);
		result.put("msg", msg);
		LOG.error("返回值：" + JsonUtil.toJsonStr(result));
		return JsonUtil.toJsonStr(result);
	}
}