package com.hc9.web.main.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.normal.Md5Util;
import com.hc9.commons.normal.Validate;
import com.hc9.web.main.common.annotation.CheckLogin;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.constant.IntegralType;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Validcodeinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.RedisUtil;
import com.hc9.web.main.redis.SmsEmailCache;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.IntegralSevice;
import com.hc9.web.main.service.MyindexService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.ValidcodeInfoService;
import com.hc9.web.main.service.baofo.BaoFuLoansignService;
import com.hc9.web.main.service.smsmail.SmsUtilService;
import com.hc9.web.main.util.CSRFTokenManager;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoginRelVo;

/** 个人中心 */
@Controller
@RequestMapping("/member")
@CheckLogin(value = CheckLogin.WEB)
public class MyindexController {

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private SmsUtilService smsUtilService;

	@Resource
	private MyindexService myindexService;

	@Resource
	private ValidcodeInfoService validcodeInfoService;

	@Resource
	private HibernateSupport dao;

	@Resource
	private IntegralSevice integralSevice;

	@Resource
	private BaoFuLoansignService baoFuLoansignService;
	
	@Resource
	private CacheManagerService cacheManagerService;
	
	private String result = "result";

	/** 手机绑定 */
	@RequestMapping("/bindPhone.htm")
	@ResponseBody
	public String bindPhone(HttpServletRequest request, String smscode,
			String newPhone,String tradePwd) {
		Userbasicsinfo userbasicsinfo = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		Userbasicsinfo user = userbasicsinfoService.queryUserById(userbasicsinfo.getId());
		Validcodeinfo validcode = (Validcodeinfo) dao.findObject(
				"from Validcodeinfo v where v.userbasicsinfo.id=?",
				userbasicsinfo.getId());
		String regCode = (String)request.getSession().getAttribute("regCode");
		// 判断手机验证码是否正确
		if (smscode.equals(regCode)) {
			// 判断手机验证码是否超时
			/*Long time = new Date().getTime();
			Long endtime = validcode.getSmsoverTime();
			if (endtime < time) {*/
				tradePwd = Md5Util.execute(tradePwd);
				if (tradePwd.equals(user.getTransPassword())) {
					try {
						user.getUserrelationinfo().setPhone(newPhone);
						user.getUserrelationinfo().setPhonePass(1);
						userbasicsinfoService.update(user);
						// 绑定成功
						String numberCode = StringUtil.getvalidcode();
						validcode.setSmsCode(numberCode);
						validcodeInfoService.update(validcode);
						// 手机绑定加分
						integralSevice.phoneAuth(user, IntegralType.PHONE);
						request.getSession().removeAttribute("regCode");  // 清楚验证码
						
						LoginRelVo loginRelVo = SysCacheManagerUtil.getLoginRelVoById("" + user.getId());
						loginRelVo.setPhone(newPhone);
						cacheManagerService.updateLoginRelVoToRedis(loginRelVo);
						LOG.error("更新用户" + loginRelVo.getId() + "缓存相关信息成功！");
						return "5";
					} catch (Exception e) {// 绑定失败
						LOG.error("绑定手机号码失败！", e);
						return "2";
					}
				} else {
					return "3";   // 交易密码错误
				}
			/*} else {// 验证码超时
				return "0";
			}*/
		} else {  // 验证码错误
			return "1";
		}

	}

	/** 发送邮箱激活邮件 */
	@RequestMapping("/replymail")
	@ResponseBody
	public String replyMail(HttpServletRequest request, String username) {
		return myindexService.replyMail(request, username);
	}

	/** 通过发送邮件重置邮箱 */
	@RequestMapping("/resetMail")
	@ResponseBody
	public String resetMail(HttpServletRequest request, String email) {
		Userbasicsinfo u = (Userbasicsinfo) request.getSession().getAttribute(
				Constant.SESSION_USER);
		u = userbasicsinfoService.queryUserById(u.getId());
		try {
			// 发送激活邮件
			try {
				myindexService.sendResetEmail(u, email, request);
				result = "1";
			} catch (Exception e) {
				e.printStackTrace();
				result = "0";
			}
			request.getSession().setAttribute("newEmail", email); // 保存需要需要写入的邮箱
		} catch (Exception e) {
			e.printStackTrace();
			result = "0";
		}
		return result;
	}

	/**
	 * 身份验证具体操作
	 * 
	 * @param name 会员真实姓名
	 * @param cardId 会员身份证号
	 * @param request HttpServletRequest
	 * @return String
	 */
	@RequestMapping("/identityValidateImpl")
	@ResponseBody
	public String identityValidateImpl(String name, String cardId,
			HttpServletRequest request) {
		return myindexService.identityValidateImpl(name, cardId, request);
	}

	/**
	 * 注册发送短信
	 * 
	 * @param phone
	 * @param request
	 * @param urlcase
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/sendSMSForReg",method = RequestMethod.POST)
	@ResponseBody
	public String sendSMSForReg(@RequestParam String CSRFToken,String phone, HttpServletRequest request)throws Exception {
		if("18380599919".equals(phone)){
			return "error-404";
		}
		if(request.getSession().getAttribute(CSRFTokenManager.CSRF_TOKEN_FOR_SESSION_ATTR_NAME)==null){
			return "error-404";
		}
		if(CSRFToken == null ||!CSRFToken.equals(request.getSession().getAttribute(CSRFTokenManager.CSRF_TOKEN_FOR_SESSION_ATTR_NAME).toString())){
			return "error-404";
		}
		try {
		 	//做临时切换校验功能
		 	//获取缓存里的数值，判断结果，如果为1，沿用校验，0，关闭校验
		 	String validateStatus=SmsEmailCache.getSmsValidateStatus();

		 	String smsResult="";
		 	if(null!=validateStatus && "1".equals(validateStatus)){
		 		smsResult=sendSmsWithValidate(request, phone);
		 	}else{
		 		smsResult=sendSmsWithoutValidate(request, phone);		 		

		 	}
		 	request.getSession().setAttribute("regPhone", phone);
		 	
		 	return smsResult;
		} catch (Exception e) {
			LOG.error("生成短信验证码出错！", e);
			return "0";
		}
	}
	
	/** 校验后发送短信 */
	private String sendSmsWithValidate(HttpServletRequest request,String phone) throws Exception{
		String userInfo = "";
	  	Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
	 	if(userbasic != null) {
	 		userInfo = "用户id:" + userbasic.getId() + ",用户名：" + userbasic.getName();
		}
	    String sessionId = request.getSession().getId();
	    String sessionIdkey = "INT:HC9:SMS:REG:CODE:GEN:NUM:" + sessionId;
	    LOG.error("-->本次session: "+sessionId);
	    if(RedisUtil.validCodeGenNum(sessionIdkey, phone)) {
	    	LOG.error(sessionId + " 校验通过！ ");
	    	String queryString = request.getQueryString();
		 	smsUtilService.sendCodeForReg(request, phone);
			LOG.info(userInfo + ",phone:" + phone + " 产生一个手机验证码, sessionId=" + sessionId 
				+ ",ip=" + request.getRemoteAddr() + ",port=" + request.getRemotePort() 
				+ ",queryString:" + queryString);
			RedisHelper.incrBy(sessionIdkey, 1);
			RedisUtil.increasePhoneValidCodeTotalNum(phone);
			return "1";
	   	} else {
	   		LOG.error(sessionId + " 校验不通过！ ");
	   		return "2";
	 	}
	}
	private String sendSmsWithoutValidate(HttpServletRequest request,String phone) throws Exception{
		LOG.error("-->校验关闭");
		smsUtilService.sendCodeForReg(request, phone);
		return "1";
	}
	/**
	 * 针对已经绑定的手机验证
	 * 
	 * @param phone
	 *            手机号码
	 * @param smscode
	 *            手机验证码
	 * @param request  HttpServletRequest
	 * @return String
	 */
	@RequestMapping("/validatePhone")
	@ResponseBody
	public String validatePhone(String phone, String smscode,
			HttpServletRequest request) {
		Userbasicsinfo u = (Userbasicsinfo) request.getSession().getAttribute(
				Constant.SESSION_USER);
		Userbasicsinfo user = userbasicsinfoService.queryUserById(u.getId());
		// 可能出现用户长时间没处理而session过期
		if (user == null) {
			return "redirect:/visitor/to-login";
		}

		String checkCode = myindexService.verifyPhone(phone, smscode, request);
		if (checkCode != "1") {
			return "0";
		}

		userbasicsinfoService.update(user);
		return "1";
	}

	/**
	 * 用户注册成功
	 * @param request HttpServletRequest
	 */
	@RequestMapping("regis")
	@ResponseBody
	public void regis(HttpServletRequest request) {
		Userbasicsinfo userbasicsinfo = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		Userbasicsinfo user = userbasicsinfoService
				.queryUserById(userbasicsinfo.getId());
		request.setAttribute("u", user);
		request.setAttribute(Constant.SESSION_USER, user);

	}

	/**
	 * 修改邮箱页面
	 * 
	 * @param id 用户id
	 * @param request HttpServletRequest
	 * @return String
	 */
	@RequestMapping("/forwardUptEmail")
	public String forwardUptEmail(String id, HttpServletRequest request) {
		if (Validate.emptyStringValidate(id)) {
			Userbasicsinfo user = userbasicsinfoService.queryUserById(Long
					.parseLong(StringUtil.correctPassword(id)));
			Object newEmail = request.getSession().getAttribute("newEmail");
			if (newEmail != null && !"".equals(newEmail)) {
				user.getUserrelationinfo().setEmail(newEmail.toString());
			}
			user.getUserrelationinfo().setEmailisPass(1);
			userbasicsinfoService.update(user);
			request.getSession().setAttribute("session_user", user);
			request.setAttribute("url", "/member_index/selfInfo.htm?index=0_4");
			return "WEB-INF/views/success";
		} else {
			return "WEB-INF/views/index";
		}
	}

	@RequestMapping("central.htm")
	public String callcentral(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) request.getSession()
				.getAttribute("map");
		request.getSession().removeAttribute("map");
		request.setAttribute("map", map);
		return "WEB-INF/views/hc9/member/trade/central_news";
	}

	/** 注册宝付 */
	@RequestMapping("/ipsRegistration")
	public String ipsRegistration(HttpServletRequest request,String cardId,String name, String type) {
		return baoFuLoansignService.ipsRegistrationService(request,cardId,name);
	}
	
	/** 用户授权的 */
	@RequestMapping("callcentralInAccredit.htm")
	public String callcentralInAccredit(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) request.getSession()
				.getAttribute("map");
		request.getSession().removeAttribute("map");
		request.setAttribute("map", map);
		return "WEB-INF/views/central_inAccredit";
	}

	/** 用户授权接口(页面) */
	@RequestMapping("ipsInAccreditUser.htm")
	@ResponseBody
	public String ipsInAccreditUser(HttpServletRequest request, String type) {
		return baoFuLoansignService.ipsInAccreditUserService(request, type);
	}
}