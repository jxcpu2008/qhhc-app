package com.hc9.web.main.controller.activity.year2016;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
/** 2016年5月母亲节活动相关入口类 */
@RequestMapping({ "monthersDayactivity", "/" })
@Controller
public class Month05MothersDayController {
	
	@RequestMapping("/mothersDay.htm")
	public String tofeedback(HttpServletRequest request) {
		return "/WEB-INF/views/hc9/activity/year2016/month05/mothersDay";
	}
}
