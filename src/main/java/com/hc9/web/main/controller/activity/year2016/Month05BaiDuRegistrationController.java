package com.hc9.web.main.controller.activity.year2016;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hc9.web.main.util.CSRFTokenManager;


/**
 * 百度推广注册活动
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/baiDuRegistration")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Month05BaiDuRegistrationController {
	
	/** 跳转到五月用户注册活动页面 */
	@RequestMapping("/activity.htm")
	public String baiduRegistr(HttpServletRequest request) {
		request.getSession().setAttribute("csrf", CSRFTokenManager.getTokenForSession(request.getSession()));
		return "WEB-INF/views/hc9/activity/year2016/month05/baiDuRegistration";
	}	
}
