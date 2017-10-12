package com.hc9.web.main.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.pomo.web.page.model.Page;
import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.smsmail.EmailService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.GenerateLinkUtils;

import freemarker.template.TemplateException;

/** 会员中心 */
@Service
public class MyindexService {

	@Resource
	private HibernateSupport dao;

	@Resource
	private EmailService emailService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;


	/**
	 * 根据用户id查询该用户的短信验证码
	 * 
	 * @param uid
	 *            用户id
	 * @return List
	 */
	public List queryValicodeByUserId(long uid) {
		return dao
				.findBySql(
						"SELECT smsoverTime, smsCode from validcodeinfo where user_id = ?",
						uid);
	}

	/**
	 * 登陆日志查询
	 * 
	 * @param page
	 *            Page
	 * @param uid
	 *            会员id
	 * @return List
	 */
	public List queryLog(Page page, long uid) {
		return dao
				.pageListByHql(
						page,
						"SELECT a.id,a.logintime,a.ip FROM Userloginlog a WHERE a.userbasicsinfo.id=?",
						false, uid);
	}

	/**
	 * 手机认证--重载，针对手机app
	 * 
	 * @param phone
	 *            手机号码添加用户id手机端没有session
	 * @param smscode
	 *            手机验证码
	 * @param request
	 *            HttpServletRequest
	 * @return String
	 */
	public String verifyPhone(String phone, String smscode, Long uid,
			HttpServletRequest request) {
		/*
		 * Userbasicsinfo u = queryUserinfo(((Userbasicsinfo)
		 * request.getSession() .getAttribute(Constant.SESSION_USER)).getId());
		 */
		Userbasicsinfo u = userbasicsinfoService.queryUserById(uid);

		List valicode = queryValicodeByUserId(u.getId());
		Object[] valicodes = (Object[]) ((List) valicode).get(0);
		if (smscode.equals(valicodes[1])) {
			u.getUserrelationinfo().setPhone(phone);
			userbasicsinfoService.update(u);
			request.getSession().setAttribute(Constant.SESSION_USER, u);
			return "1";
		} else {
			return "0";
		}
	}

	/**
	 * 手机认证
	 * 
	 * @param phone
	 *            手机号码添加用户id手机端没有session
	 * @param smscode
	 *            手机验证码
	 * @param request
	 *            HttpServletRequest
	 * @return String
	 */
	public String verifyPhone(String phone, String smscode,
			HttpServletRequest request) {
		Userbasicsinfo u = userbasicsinfoService
				.queryUserById(((Userbasicsinfo) request.getSession()
						.getAttribute(Constant.SESSION_USER)).getId());
		List valicode = queryValicodeByUserId(u.getId());
		Object[] valicodes = (Object[]) ((List) valicode).get(0);
		if (smscode.equals(valicodes[1])) {
			u.getUserrelationinfo().setPhone(phone);
			userbasicsinfoService.update(u);
			request.getSession().setAttribute(Constant.SESSION_USER, u);
			return "1";
		} else {
			return "0";
		}
	}

	/**
	 * 发送修改邮箱的邮件
	 * 
	 * @param u
	 *            用户
	 * @param request
	 *            HttpServletRequest
	 * @throws IOException
	 *             异常
	 * @throws TemplateException
	 *             异常
	 */
	public void sendResetEmail(Userbasicsinfo u, String email,
			HttpServletRequest request) throws IOException, TemplateException {
		String emailActiveUrl = GenerateLinkUtils.generateUptEmailLink(u, request);
		Map<String, String> map = new HashMap<String, String>();
		if (u.getCardStatus() == 2) {
			map.put("name", u.getName());
		} else {
			map.put("name", u.getUserName());
		}
		map.put("emailActiveUrl", emailActiveUrl);
		String[] msg = emailService.getEmailResources("bind-email.ftl",
				map);
		// 发送邮件链接地址
		emailService.sendEmail(msg[0], msg[1], email);
	}

	/**
	 * 发送激活邮件
	 * 
	 * @param u
	 *            用户基本信息
	 * @param request
	 *            HttpServletRequest
	 * @throws IOException
	 *             异常
	 * @throws TemplateException
	 *             异常
	 */
	public void sendEmail(Userbasicsinfo u, HttpServletRequest request)
			throws IOException, TemplateException {
		// 收件人地址
		String address = u.getUserrelationinfo().getEmail();
		String userName = u.getName();

		String url = GenerateLinkUtils.generateActivateLink(u, request);
		Map<String, String> map = new HashMap<String, String>();
		if (userName == null || userName.equals("")) {
			map.put("name", "亲爱的用户");
		} else {
			map.put("name", userName);
		}

		map.put("emailActiveUrl", url);
		String[] msg = emailService.getEmailResources("account-activate.ftl",
				map);
		// 发送邮件链接地址
		emailService.sendEmail(msg[0], msg[1], address);
	}

	public void sendEmailYue(Userbasicsinfo u, HttpServletRequest request,
			String msgc) throws IOException, TemplateException {
		// 收件人地址
		String address = u.getUserrelationinfo().getEmail();
		String userName = u.getName();

		// String url = GenerateLinkUtils.generateActivateLink(u, request);
		Map<String, String> map = new HashMap<String, String>();
		/*
		 * if (userName == null || userName.equals("")) { map.put("name",
		 * "亲爱的用户"); } else { map.put("name", userName); }
		 */
		map.put("tan1", "消息：");
		map.put("tan2", msgc);
		// map.put("emailActiveUrl", url);
		String[] msg = emailService.getEmailResources("yuetan.ftl", map);
		// 发送邮件链接地址
		emailService.sendEmail("约谈", msg[1], address);
	}

	public Userbasicsinfo queryUserinfoemail(String username) {
		String sql = "from Userbasicsinfo u where u.userName=?";
		return (Userbasicsinfo) dao.findObject(sql, username);

	}

	/**
	 * 发送激活邮件
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return String
	 */
	public String replyMail(HttpServletRequest request, String username) {
		// Userbasicsinfo u = (Userbasicsinfo)
		// request.getSession().getAttribute(
		// Constant.SESSION_USER);
		// request.setAttribute("u", queryUserinfo(u.getId()));
		// u = queryUserinfo(u.getId());
		Userbasicsinfo u = queryUserinfoemail(username);
		try {
			// 取得邮箱激活链接再次发送的时间
			// Validcodeinfo validcode = (Validcodeinfo) dao.findObject(
			// "FROM Validcodeinfo v WHERE v.userbasicsinfo.id=?",
			// u.getId());
			Long time = System.currentTimeMillis();
			/*
			 * if (null != validcode.getEmailagaintime() && time <
			 * validcode.getEmailagaintime()) { return "2"; }
			 */
			// 发送激活邮件 并更新链接再次发送时间和失效时间
			sendEmail(u, request);
			// validcode.setEmailagaintime(time + 2 * 60 * 1000L);
			// validcode.setEmailovertime(time + 24 * 60 * 60 * 1000L);
			// dao.update(validcode);
			return "1";
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
	}

	public String sendMailYue(HttpServletRequest request, String email,
			String msg) {

		Userbasicsinfo u = queryUserinfoemail(email);
		try {

			sendEmailYue(u, request, msg);

			return "1";
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
	}

	/**
	 * 完成实名认证
	 * 
	 * @param name
	 *            用户名
	 * @param cardId
	 *            用户身份证
	 * @param request
	 *            HttpServletRequest
	 * @return String
	 */
	public String identityValidateImpl(String name, String cardId,
			HttpServletRequest request) {
		Userbasicsinfo u = (Userbasicsinfo) request.getSession().getAttribute(
				Constant.SESSION_USER);
		u.setName(name);
		u.getUserrelationinfo().setCardId(cardId);
		try {
			userbasicsinfoService.update(u);
			request.getSession().setAttribute(Constant.SESSION_USER, u);
			return "1";
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
	}

}
