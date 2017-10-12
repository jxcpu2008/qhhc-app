package com.hc9.web.main.controller;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.hc9.commons.normal.Md5Util;
import com.hc9.web.main.common.annotation.CheckLogin;
import com.hc9.web.main.constant.HostAddress;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.MemberCenterService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.OSSUtil;
import com.hc9.web.main.vo.LoginRelVo;

/** 会员基本信息修改 */
@RequestMapping("/update_info")
@CheckLogin(value = CheckLogin.WEB)
public class UserBaseInfoController {

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private MemberCenterService memberCenterService;
	
	@Resource
	private CacheManagerService cacheManagerService;

	/** 登录用户session */
	public Userbasicsinfo queryUser(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		return user;
	}

	/**
	 * 修改登录密码
	 * 
	 * @param oldPwd 旧密码
	 * @param newPwd 新密码
	 * @param request 请求
	 * @return bool
	 */
	@RequestMapping("/update_pwd")
	@ResponseBody
	public String updatePwd(HttpServletRequest request, String oldPwd,
			String newPwd, String surePwd) {
		try {
			Userbasicsinfo user = queryUser(request);
			// 查询基本信息
			user = userbasicsinfoService.queryUserById(user.getId());
			// 判断旧密码是否为空
			if (oldPwd != null && !oldPwd.trim().equals("")) {
				// 加密旧密码
				oldPwd = Md5Util.execute(oldPwd);
				newPwd = Md5Util.execute(newPwd);
				if (!oldPwd.equals(user.getPassword())) {
					// 判断旧登录密码是否正确
					return "different";
				} else if (surePwd.equals(newPwd)) {
					return "notFit";
				} else {
					// 修改登录密码
					userbasicsinfoService.updatePwd(user, newPwd);
					LoginRelVo loginRelVo = SysCacheManagerUtil.getLoginRelVoById("" + user.getId());
					loginRelVo.setPassword(newPwd);
					cacheManagerService.updateLoginRelVoToRedis(loginRelVo);
					return "true";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "false";
	}

	/**
	 * 上传身份证
	 * 
	 * @param cardId
	 *            头像地址
	 * @param multipartRequest
	 *            multipartRequest
	 * @param response
	 *            response
	 * @throws IOException
	 */
	@RequestMapping("/upload_identity")
	@ResponseBody
	public String uploadHead(MultipartHttpServletRequest multipartRequest,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		Map<String,String> map=OSSUtil.uploadToOss(request, "cards");
		String cardImg = "";
		if (map != null) {
			cardImg = map.get("fileDir");
		}
		return cardImg;
	}

	/** 会员退出登录 */
	@RequestMapping("/login_out")
	public String loginOut(HttpServletRequest request) {
		request.getSession().removeAttribute(Constant.SESSION_USER);
		request.getSession().removeAttribute("comData");
		return "redirect:"+HostAddress.getHostAddress()+"/";	

	}

	/** 修改交易密码 */
	@RequestMapping("/update_paypwd")
	@ResponseBody
	public String updatePayPassword(HttpServletRequest request,
			String newPwd, String surePwd, String code) {
		Userbasicsinfo user = queryUser(request);
		try {
			if (user != null) {
				// 取验证码
				String validate = (String) request.getSession().getAttribute(
						"regCode");
				if (!newPwd.equals(surePwd)) { // 两次密码不匹配
					return "notFit";
				} else if (!code.equals(validate)) { // 验证码不匹配
					return "codeNotFit";
				} else {
					surePwd = Md5Util.execute(surePwd);
					user.setTransPassword(surePwd);
					userbasicsinfoService.update(user); // 修改交易密码
					request.getSession().removeAttribute("regCode");
					return "success";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}
}
