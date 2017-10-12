package com.hc9.web.main.controller.activity.year2016;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.Arith;
import com.hc9.web.main.redis.activity.year2016.month05.HcFirstInvestCache;
import com.hc9.web.main.redis.activity.year2016.month05.HcParentCache;
import com.hc9.web.main.redis.activity.year2016.month05.HcWeekSurpriseCache;
import com.hc9.web.main.redis.activity.year2016.month05.WeekVo;
import com.hc9.web.main.service.activity.ActivityCommonService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.JsonUtil;

/** 五月活动相关入口类 */
@RequestMapping({ "activity201605", "/" })
@Controller
public class Month05ActivityController {
	@Resource
	private ActivityCommonService activityCommonService;
	
	/** 跳转到五六月用户回馈活动页面 */
	@RequestMapping("/tofeedback.htm")
	public String tofeedback(HttpServletRequest request) {
		
		/** 平台首投送现金活动相关逻辑 */
		firstInvestActivity(request);
			
		/*** 周周惊喜大放送活动相关逻辑 */
		weekSurpriseActivity(request);
			
		/** 双亲感恩大回馈活动相关逻辑 */
		parentActivity(request);
		
		return "/WEB-INF/views/hc9/activity/year2016/month05/feedback";
	}
	
	/*** 根据指定周数查询周周惊喜大放送相关排行榜 */
	@RequestMapping(value="/getWeekRankList.htm", method = RequestMethod.POST)
	@ResponseBody
	private String getWeekRankList(HttpServletRequest request, int weekNum) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		int nowWeekNum = HcWeekSurpriseCache.getWeekSurpriseWeekNum(new Date());
		if(weekNum > nowWeekNum) {
			resultMap.put("code", "-2");
			resultMap.put("msg", "当前周所在排行榜尚未开始！");
		} else {
			List<WeekVo> list = HcWeekSurpriseCache.getWeekRankList(weekNum);
			double firstWeekYearMoney = 0;
			if(list.size() > 0) {
				String onePhone = "";
				String twoPhone = "";
				String threePhone = "";
				String fourPhone = "";
				for(int i = 0; i < list.size(); i++) {
					WeekVo vo = list.get(i);
					String toInCome = "红包+加息券";
					if(i == 0) {
						onePhone = vo.getPhone();
						firstWeekYearMoney = vo.getWeekYearMoney();
						String money = Arith.round(Arith.mul(firstWeekYearMoney, 0.02), 2).toString();
						toInCome = money + "元";
					} else if(i == 1) {
						twoPhone = vo.getPhone();
						String money = Arith.round(Arith.mul(vo.getWeekYearMoney(), 0.015), 2).toString();
						toInCome = money + "元";
					} else if(i == 2) {
						threePhone = vo.getPhone();
						String money = Arith.round(Arith.mul(vo.getWeekYearMoney(), 0.01), 2).toString();
						toInCome = money + "元";
					} else if(i == 3){
						fourPhone = "若干";
					}
					vo.setToInCome(toInCome);
				}
				request.setAttribute("onePhone", onePhone);
				request.setAttribute("twoPhone", twoPhone);
				request.setAttribute("threePhone", threePhone);
				request.setAttribute("fourPhone", fourPhone);
			}
			
			Userbasicsinfo user = (Userbasicsinfo) request.getSession().
					getAttribute(Constant.SESSION_USER);
			if(user != null) {
				long userId = user.getId();
				long weekMoney = HcWeekSurpriseCache.getWeekInvestMoneyOfUser(userId, weekNum);
				double weekYearMoney = HcWeekSurpriseCache.getWeekYearInvestMoneyOfUser(userId, weekNum);
				double differMoney = firstWeekYearMoney - weekYearMoney;
				resultMap.put("weekMoney", weekMoney);
				resultMap.put("weekYearMoney", weekYearMoney);
				resultMap.put("differMoney", differMoney);
			}
			resultMap.put("weekRankList", list);
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/** 平台首投送现金活动相关逻辑 */
	private void firstInvestActivity(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			/** 用户是否有机会已经使用过首投回馈机会: */
			String chanceFlag = "0";//0表示有机会，1表示无机会
			long userId = user.getId();
			/** 判断是否又机会参与首投 */
			if(HcFirstInvestCache.isUserHasFirstFeedBackChance(userId)) {
				/** 用户是否已经投资过 */
				boolean isFirst = activityCommonService.hasSuccessInvestRecordBefore(userId);
				if(isFirst) {
					chanceFlag = "1";
				}
			} else {
				chanceFlag = "1";
			}
			request.setAttribute("chanceFlag", chanceFlag);
		}
	}
	
	/*** 周周惊喜大放送活动相关逻辑 */
	private void weekSurpriseActivity(HttpServletRequest request) {
		int weekNum = HcWeekSurpriseCache.getWeekSurpriseWeekNum(new Date());
		/** 当前周上一周的榜单列表 */
		int lastWeek = 0;
		if(weekNum > 1) {
			lastWeek = weekNum - 1;
		}
		List<WeekVo> laskWeekList = HcWeekSurpriseCache.getWeekRankList(lastWeek);
		if(laskWeekList.size() > 0) {
			String onePhone = "";
			String twoPhone = "";
			String threePhone = "";
			String fourPhone = "";
			for(int i = 0; i < laskWeekList.size(); i++) {
				WeekVo vo = laskWeekList.get(i);
				String toInCome = "红包+加息券";
				if(i == 0) {
					onePhone = vo.getPhone();
					String money = Arith.round(Arith.mul(vo.getWeekYearMoney(), 0.02), 2).toString();
					toInCome = money + "元";
				} else if(i == 1) {
					twoPhone = vo.getPhone();
					String money = Arith.round(Arith.mul(vo.getWeekYearMoney(), 0.015), 2).toString();
					toInCome = money + "元";
				} else if(i == 2) {
					threePhone = vo.getPhone();
					String money = Arith.round(Arith.mul(vo.getWeekYearMoney(), 0.01), 2).toString();
					toInCome = money + "元";
				} else if(i == 3){
					fourPhone = "若干";
				}
				
				vo.setToInCome(toInCome);
			}
			request.setAttribute("onePhone", onePhone);
			request.setAttribute("twoPhone", twoPhone);
			request.setAttribute("threePhone", threePhone);
			request.setAttribute("fourPhone", fourPhone);
		}
		
		double firstWeekYearMoney = 0;
		List<WeekVo> list = HcWeekSurpriseCache.getWeekRankList(weekNum);
		if(list.size() > 0) {
			firstWeekYearMoney = list.get(0).getWeekYearMoney();
		}
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			long userId = user.getId();
			long weekMoney = HcWeekSurpriseCache.getWeekInvestMoneyOfUser(userId, weekNum);
			double weekYearMoney = HcWeekSurpriseCache.getWeekYearInvestMoneyOfUser(userId, weekNum);
			double differMoney = firstWeekYearMoney - weekYearMoney;
			request.setAttribute("weekMoney", weekMoney);
			request.setAttribute("weekYearMoney", weekYearMoney);
			request.setAttribute("differMoney", differMoney);
		}
		request.setAttribute("weekNum", weekNum);
		request.setAttribute("weekRankList", list);
	}
	
	/** 双亲感恩大回馈活动相关逻辑 */
	private void parentActivity(HttpServletRequest request) {
		List<WeekVo> list = HcParentCache.getParentFeedBackRankList();
		double firstMonthYearMoney = 0;
		if(list.size() > 0) {
			WeekVo vo = list.get(0);
			firstMonthYearMoney = vo.getWeekYearMoney();
		}
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			long userId = user.getId();
			double monthYearMoney = HcParentCache.getMonthYearInvestMoneyOfUser(userId);
			double monthDifferMoney = firstMonthYearMoney - monthYearMoney;
			request.setAttribute("monthDifferMoney", monthDifferMoney);
		}

		/** 活动时间范围标识 */
		int parentActivityFlag = HcParentCache.isParentActivity(new Date());
		request.setAttribute("parentActivityFlag", parentActivityFlag);
		request.setAttribute("monthRankList", list);
	}
}