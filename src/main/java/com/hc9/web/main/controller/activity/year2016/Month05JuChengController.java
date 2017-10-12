package com.hc9.web.main.controller.activity.year2016;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.normal.RandomUtils;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.activity.year2016.Month05JuChengService;
import com.hc9.web.main.util.CSRFTokenManager;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;

/** 2016年5月聚橙网活动相关入口类 */
@RequestMapping({ "juchengactivity"})
@Controller
public class Month05JuChengController {
	@Resource
	private Month05JuChengService month05JuChengService;
	
	@Resource
	private UserbasicsinfoService userbasicsinfoService;
	
	/** 跳转到五六月用户回馈活动页面 */
	@RequestMapping("/activity.htm")
	public String tofeedback(HttpServletRequest request) {
		month05JuChengService.queryJuChengUserMoneyInfo(request);
		String code = RandomUtils.getNumberString(4);
		request.getSession().setAttribute("user_login", code);
		request.getSession().setAttribute("csrf", CSRFTokenManager.getTokenForSession(request.getSession()));
		long userId = 100155;
		Userbasicsinfo genuser = userbasicsinfoService.queryUserById(userId);
		// 将推广人保存到session中
		request.getSession().setAttribute("generuser", genuser);
		return "/WEB-INF/views/hc9/activity/year2016/month05/polyOrangeNetwork";
	}
	
	/** 聚橙网用户投资后查询是否获取到演唱会门票数 */
	@RequestMapping(value="/queryJuChengPrizeNum.htm", method = RequestMethod.POST)
	@ResponseBody
	public String queryJuChengPrizeNum(HttpServletRequest request, String investOrderNum) {
		Map<String, String> resultMap = new HashMap<String, String>();
		String prizeNum = "0";
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			try {
				Thread.sleep(2000);
				String orderKey = "STR:HC9:JUCHENG:INVEST:PRIZENUM:" + investOrderNum;
				prizeNum = RedisHelper.get(orderKey);
				if(StringUtil.isBlank(prizeNum)) {
					prizeNum = "0";
				}
			} catch(Exception e) {
				LOG.error("聚橙网用户投资后查询是否获取到演唱会门票数过程中出错：", e);
			}
		}
		resultMap.put("prizeNum", prizeNum);
		String jsonStr = JsonUtil.toJsonStr(resultMap);
		return jsonStr;
	}
}