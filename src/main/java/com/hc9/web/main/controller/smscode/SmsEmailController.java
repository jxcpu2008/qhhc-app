package com.hc9.web.main.controller.smscode;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.PhoneBlackList;
import com.hc9.web.main.redis.SmsEmailCache;
import com.hc9.web.main.service.RegistrationService;
import com.hc9.web.main.service.smsmail.SmsUtilService;
import com.hc9.web.main.util.CSRFTokenManager;
import com.hc9.web.main.util.DateUtil;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;

/** 短信邮件相关入口类 */
@RequestMapping("/smsEmail")
public class SmsEmailController {
	@Resource
	private SmsUtilService smsUtilService;
	
	@Resource
	private RegistrationService registrationService;
	
	@Resource
	private HibernateSupport commonDao;

	/** web注册发送短信验证码 */
	@RequestMapping(value="/sendRegisterCode",method = RequestMethod.POST)
	@ResponseBody
	public String sendRegisterCode(String CSRFToken, String imagecode, String phone, HttpServletRequest request) {
		if(StringUtil.isBlank(imagecode)) {
			return "error-404";
		}
		if(request.getSession().getAttribute(CSRFTokenManager.CSRF_TOKEN_FOR_SESSION_ATTR_NAME)==null){
			return "error-404";
		}
		if(StringUtil.isBlank(CSRFToken) || !CSRFToken.equals(request.getSession().getAttribute(CSRFTokenManager.CSRF_TOKEN_FOR_SESSION_ATTR_NAME).toString())){
			return "error-404";
		}
		String imgCode = (String)request.getSession().getAttribute("user_login");
		if(!imagecode.trim().equals(imgCode)) {
			return "error-404";
		}
		try {
			if(!SmsEmailCache.isAbleGetRegisterCode(phone)) {
				return "0";
			}
			boolean bool = registrationService.checkPhone(null, phone);
			if(bool) {//校验手机号是否已经注册过
				Date date = new Date();
				String today = DateUtil.format(date, "yyyy-MM-dd");
				/** 未被注册过，当天最多三次获取注册短信的机会 */
				int maxRegCodeNum = SmsEmailCache.getMaxRgeisterSmsCodeNumOneDay();
				int todayCodeNum = SmsEmailCache.getTodayRgeisterSmsCodeNum(today, phone);
				if(todayCodeNum >= maxRegCodeNum) {
					return "0";
				} else {
					int maxBlackListNum = SmsEmailCache.getMaxBlackListNum();
					int registerBlackListSmsNum = SmsEmailCache.getRegisterBlackListSmsNum(phone);
					if(registerBlackListSmsNum >= maxBlackListNum) {
						PhoneBlackList  vo = new PhoneBlackList();
						vo.setSourceType(1);
						vo.setPhone(phone);
						vo.setCreateTime(DateUtil.format("yyyy-MM-dd HH:mm:ss"));
						commonDao.save(vo);
						return "9";
					} else {
						smsUtilService.sendCodeForReg(request, phone);
						request.getSession().setAttribute("regPhone", phone);
						SmsEmailCache.increaseTodayRgeisterSmsCodeNum(today, phone);
						SmsEmailCache.increaseRegisterBlackListSmsNum(phone);
						
					 	return "1";
					}
				}
			} else {
				return "0";
			}
		} catch (Exception e) {
			LOG.error("生成短信验证码出错！", e);
			return "0";
		}
	}
	
}
