package com.hc9.web.main.controller.activity.year2015;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.entity.ActivityMonkey;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.IndexDataCache;
import com.hc9.web.main.redis.activity.year2016.month01.HcMonkeyActivitiCache;
import com.hc9.web.main.redis.activity.year2016.month03.HcPeachActivitiCache;
import com.hc9.web.main.service.activity.year2015.ActivityService;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.JsonUtil;

/** 活动相关请求入口类 */
@RequestMapping({ "activity", "/" })
@Controller
public class ActivityController {
	
	@Resource
	private ActivityService activityService;
	
	/** 8月8号至8月31日活动相关  */
	@RequestMapping("/august.htm")
	public String augustActivity(HttpServletRequest request, String tab) {
		return "WEB-INF/views/hc9/activity/august";
	}
	
	/** 
	 * 新春猴给力活动页
	 * */
	@RequestMapping("/monkeyActivity.htm")
	public String monkeyActivity(HttpServletRequest request) {
		int week = HcMonkeyActivitiCache.week() + 1;
		List<Map<String,String>> list = IndexDataCache.getList("NEWYEAR:INVEST:MONKEY:TOTAL");
		if (week == 7) {
			Object obj = activityService.queryHongChouEredarTopThree();
			request.setAttribute("eredar", obj);
		}
		request.setAttribute("week", week);
		request.setAttribute("eredarList", list);
		return "/WEB-INF/views/hc9/activity/monkeyActivity";
	}
	
	@RequestMapping("/monkeyData.htm")
	public String monkeyData(HttpServletRequest request,String weekNum,String week) {
		String listKey = "NEWYEAR:INVEST:MONKEY:WEEK:" + weekNum;
		List<Map<String,String>> list = IndexDataCache.getList(listKey);
		request.setAttribute("list", list);
		request.setAttribute("week", week);
		request.setAttribute("weekNum", weekNum);
		return "/WEB-INF/views/hc9/activity/monkeyActivityData";
	}
	
	/** 
	 * “红筹理财师”活动页
	 * */
	@RequestMapping("/hcPlannerActivity.htm")
	public String plannerActivity(HttpServletRequest request) {
		String plannerKey = "NEWYEAR:INVEST:FINANCIAL:LIST";
		List<Map<String,String>> list = IndexDataCache.getList(plannerKey);
		request.setAttribute("recommendTenders", list);
		return "/WEB-INF/views/hc9/activity/redChipPlannerActivity";
	}
	
	/** 
	 * “春风迎三月，金桃朵朵开”活动页
	 * */
	//@RequestMapping("/peachActivity.htm")
	public String peack(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
		List<Object[]> billBoards = null;  // 获取推荐排行榜
		Integer lotteryNum = 0;  // 剩余抽奖次数
		Integer billNum = 0; // 排名
		Integer reffCount = 0; // 累计推荐人数
		billBoards = activityService.queryGoldPeachBillBoard(null);
		if (user != null && billBoards != null) {
			// 获取金桃抽奖次数
			lotteryNum = HcPeachActivitiCache.getPermanentLotteryChance(user.getId());
			// 获取当前用户的“排名”以及“累计推荐注册人数”
			Object[] personalBillBoard = activityService.queryGoldPeachBillBoard(user.getId()).get(0);
			for (Object[] board : billBoards) {
				if (personalBillBoard[0] != null) {
					billNum++;
					if (board[0].equals(personalBillBoard[0])) {
						reffCount = Integer.valueOf(personalBillBoard[1].toString());
						break;
					}
				}
			}
		}
		request.setAttribute("goldPeachBillBoards", billBoards);
		request.setAttribute("billNum", billNum);
		request.setAttribute("lotteryNum", lotteryNum);
		request.setAttribute("reffCount", reffCount);
		return "/WEB-INF/views/hc9/activity/goldPeachActivity";
	}
	/**
	 * 金桃抽奖
	 * @param request
	 * code : 0、抽奖成功-1、活动未开始或已结束-2、抽奖机会用完
	 * flagMsg ： 0、表示自己注册时送的抽奖次数1、表示好友注册时送的抽奖次数
	 * @return
	 */
	@RequestMapping("/lotteryPeach.htm")
	@ResponseBody
	public String lotteryPeach(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
		Map<String, String> resultMap = new HashMap<String, String>();
		if (user != null) {
			try {
				if (HcPeachActivitiCache.validCurrentDate(new Date()) == 0) {
					Long userId = user.getId();
					Integer lotteryNum = HcPeachActivitiCache.getPermanentLotteryChance(userId);
					if (lotteryNum > 0) {
						ActivityMonkey goldPech = activityService.getLotteryPech(userId);
						if (goldPech != null) {
							String createTime = DateFormatUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
							activityService.updateLotteryPech(goldPech.getId(),createTime); 
							lotteryNum = HcPeachActivitiCache.getPermanentLotteryChance(userId);
							String idStr = goldPech.getUserId().toString();
							String byIdStr = goldPech.getByUser().getId().toString();
							if (idStr.equals(byIdStr)) {
								resultMap.put("flagMsg", "0");
							} else {
								resultMap.put("flagMsg", "1");
							}
							HcPeachActivitiCache.decreasePermanentLotteryChance(userId);
							resultMap.put("code", "0");  // 抽奖成功
						}
						resultMap.put("num", lotteryNum.toString());
					} else {
						resultMap.put("code", "-2");  // 抽奖机会用完
					}
				} else {
					resultMap.put("code", "-1"); // 活动未开始或已结束
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("金桃抽奖出现问题："+e.getMessage());
				resultMap.put("code", "-3");
			}
		}
		String jsonStr = JsonUtil.toJsonStr(resultMap);
		return jsonStr;
	}

	/** 嗒嗒巴士春节抢票banner活动介绍页  */
	@RequestMapping("/taptapActivity.htm")
	public String taptapActivity(HttpServletRequest request) {
		return "WEB-INF/views/hc9/activity/tapTapActivity";
	}
	
	/**
	 * 街拍活动页
	 * */
	@RequestMapping("/streetBeatActivity.htm")
	public String streetBeatActivity(HttpServletRequest request) {
		return "WEB-INF/views/hc9/activity/streetBeat";
	}
}