package com.hc9.web.main.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hc9.web.main.entity.Helper;
import com.hc9.web.main.service.article.HelperService;

@Controller
@RequestMapping("/helper")
public class HelperController {

	@Resource
	private HelperService helperService;

	@RequestMapping("/queryHelp.htm")
	public String queryHelp(HttpServletRequest request, Integer id) {
		List<Helper> list = helperService.queryHelp(id);
		request.setAttribute("helplist", list);
		return "WEB-INF/views/hc9/help_right";
	}

	/** 帮助中心 */
	@RequestMapping("/toHelper.htm")
	public String aaaa(HttpServletRequest request, String sign) {
		request.setAttribute("sign", sign);
		return "WEB-INF/views/hc9/helper";
	}
}