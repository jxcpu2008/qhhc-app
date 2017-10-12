package com.hc9.web.main.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Userrelationinfo;
import com.hc9.web.main.entity.Validcodeinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.RedisUtil;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.FindPassWordService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.ValidcodeInfoService;
import com.hc9.web.main.util.GenerateLinkUtils;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoginRelVo;

import freemarker.template.TemplateException;

/** 忘记密码专用控制层 */
@Controller
@RequestMapping("/find_password")
public class FindPassWordController {

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private FindPassWordService findPassWordService;

	@Resource
	private ValidcodeInfoService validcodeInfoService;
	
	@Resource
	private CacheManagerService cacheManagerService;

	/** 初始化找回密码页面 */
	@RequestMapping("/init.do")
	public String indexone(HttpServletRequest request) {
		return "WEB-INF/views/hc9/findpassword";
	}

	/** 通过手机发送忘记密码的验证吗 
	 * @param phone 用户的手机号码
	 * @return 1 成功 2 电话号码不存在
	 */
	@ResponseBody
	@RequestMapping("/findphone.do")
	public int sendPhoneToFind(String phone, HttpServletRequest request) {
		Userrelationinfo userrelat = findPassWordService.queryUserlationBysome(
				phone, 1);
		if (null == userrelat || StringUtil.isBlank(userrelat.getPhone())) {
			return 2;
		}

		Userbasicsinfo user = userbasicsinfoService.queryUserById(userrelat.getId());
		String sessionId = request.getSession().getId();
		String sessionIdkey = "INT:HC9:SMS:REG:CODE:GEN:NUM:" + sessionId;
		if(RedisUtil.validCodeGenNum(sessionIdkey, phone)) {
			int result = findPassWordService.sendsesCodel(user, phone, request);
			RedisHelper.incrBy(sessionIdkey, 1);
			RedisUtil.increasePhoneValidCodeTotalNum(phone);
			return result;
		}
		// 发送短信
		return -1;
	}

	/** 通过用户和验证码判断验证码是否正确，并跳转到响应的页面 
	 * @param id 用户编号
	 * @param smsCode 短信验证码
	 * @param request 请求
	 * @return 1.成功  2请重新发送短新验证码 3验证码输入错误！ 4找回密码失败！5.该手机号码不存在！
	 */
	@RequestMapping("/checksmsCode")
	@ResponseBody
	public int checksmsCode(String phone, String smsCode,String captcha, HttpServletRequest request) {
		
		Userrelationinfo userrelat = findPassWordService.queryUserlationBysome(phone, 1);
			if (null != userrelat&&!StringUtil.isBlank(userrelat.getPhone())) {
				Userbasicsinfo userbasics = userbasicsinfoService.queryUserById(userrelat.getId());
				Validcodeinfo valid = validcodeInfoService
						.getValidcodeinfoByUid(userbasics.getId());
				if(null != valid && null != valid.getSmsCode()){
				if (valid.getSmsCode().equals(smsCode)) {
					request.getSession().setAttribute("updateuser", userbasics);
					return 1;
				} else {
					return 3;
				}
				}else{
					return 2;
				}
			}
		return 5;
	}
	
	@RequestMapping("/toUpdate")
	public String toUpdate(HttpServletRequest request){
		return "WEB-INF/views/hc9/updatepassword";
	}
	
	/** 通过用户邮箱找回密码 
	 * @param email 邮箱
	 * @return 1.成功 2 发送太频繁 3 邮箱不存在 4 发送失败
	 */
	@ResponseBody
	@RequestMapping("/findemail.do")
	public int sendemailToFind(String email, HttpServletRequest request) {
		Userrelationinfo userrelat = findPassWordService.queryUserlationBysome(email, 2);
		if (null == userrelat || StringUtil.isBlank(userrelat.getEmail())) {
			return 3;
		}

		Userbasicsinfo user = userbasicsinfoService.queryUserById(userrelat.getId());
		// 激活邮件
		try {
			return findPassWordService.sendEmailCodel(user, email, request);
		} catch (IOException e) {
			LOG.error("通过用户邮箱找回密码过程中IO异常!", e);
			return 4;
		} catch (TemplateException e) {
			LOG.error("通过用户邮箱找回密码过程中模板异常!", e);
			return 4;
		}

	}

	/** 点击找回密码的链接 
	 * @param userName 参数
	 * @param request 请求
	 * @return 要返回的页面
	 */
	@RequestMapping("/checkresetpwdlink")
	public String checkResetpwdLink(Long userName, HttpServletRequest request) {
		String msg = "链接错误！";
		String msg1 = "该链接已失效！";
		if (userName != null && !"".equals(userName)) {
			Userbasicsinfo userbasics = userbasicsinfoService
					.queryUserById(userName);

			if (null != userbasics) {
				Validcodeinfo valid = validcodeInfoService
						.getValidcodeinfoByUid(userbasics.getId());
				if (valid != null) {
					long overtime = 0;

					// 获取过期时间
					if (valid.getEmailovertime() != null
							&& !"".equals(valid.getEmailovertime())) {
						overtime = valid.getEmailovertime();
					}

					// 判断链接是否过期
					if ((overtime - System.currentTimeMillis()) > 0) {

						userbasics.setRandomCode(valid.getEmailcode());

						// 判断链接中的令牌是否正确
						if (GenerateLinkUtils.verifyCheckcode(userbasics, request)) {
							request.getSession().setAttribute("updateuser",
									userbasics);
							return "WEB-INF/views/hc9/updatepassword";
						} else {
							// 此链接已失效
							request.setAttribute("msg", msg);
						}
					} else {
						// 此链接已失效
						request.setAttribute("msg", msg1);
					}
				}
			} else {
				// 错误的链接
				request.setAttribute("msg", msg);
			}
		} else {
			// 错误的链接
			request.setAttribute("msg", msg);
		}
		return "WEB-INF/views/hc9/findpassword";
	}

	/** 修改登陆密码 
	 * @param password 密码
	 * @param passwordagain 重复密码
	 * @param request 请求
	 * @param response 返回
	 * @return 输出的提示
	 */
	@ResponseBody
	@RequestMapping("/checkpasswordbylink")
	public String checkpasswordByLink(String password, String passwordagain,
			HttpServletRequest request, HttpServletResponse response) {

		Userbasicsinfo userbasicsinfo = (Userbasicsinfo) request.getSession()
				.getAttribute("updateuser");

		if (userbasicsinfo == null) {
			return "-1";// 非法链接
		}
		if (password == null || passwordagain == null
				|| password.trim().length() == 0
				|| !password.equals(passwordagain)) {
			return "-3";// 密码为空，或者两次密码输入不同！
		}

		String pwd = GenerateLinkUtils.md5(password);
		userbasicsinfoService.updatePwd(userbasicsinfo, pwd);
		LoginRelVo loginRelVo = SysCacheManagerUtil. getLoginRelVoById( "" + userbasicsinfo.getId());
        loginRelVo.setPassword( pwd);
        cacheManagerService.updateLoginRelVoToRedis( loginRelVo);

		Validcodeinfo validcodeinfo = validcodeInfoService
				.getValidcodeinfoByUid(userbasicsinfo.getId());

		if (null != validcodeinfo) {
			validcodeinfo.setEmailagaintime(Long.parseLong("0"));
			validcodeinfo.setEmailovertime(Long.parseLong("0"));
			validcodeinfo.setEmailcode(null);
			validcodeinfo.setSmsagainTime(Long.parseLong("0"));
			validcodeinfo.setSmsCode(null);
			validcodeinfo.setSmsoverTime(Long.parseLong("0"));
			validcodeInfoService.update(validcodeinfo);
		}

		request.getSession().removeAttribute("updateuser");
		return "1";
	}
}
