package com.hc9.web.main.controller.activity.year2016;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2016.month05.HcNewerTaskCache;
import com.hc9.web.main.service.activity.year2016.Month05NewerTaskActivityService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.JsonUtil;

/** 新手任务相关入口类 */
@RequestMapping({ "neweractivity", "/" })
@Controller
public class Month05NewerTaskController {
	@Resource
	private Month05NewerTaskActivityService newerTaskActivityService;
	
	/** 跳转到五六月用户回馈活动页面 */
	@RequestMapping("/totask.htm")
	public String tofeedback(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		long userId = -1;
		if(user != null) {
			userId = user.getId();
		}
		
		/** 获取收集到所有钥匙的新手的人数 */
		getCollectAllKeyNum(request);
		
		/** 获取任务一相关信息 */
		getTaskOneRelateInfo(request, userId);
		
		/** 获取任务二相关信息 */
		getTaskTwoRelateInfo(request, userId);
		
		/** 获取任务三相关信息 */
		getTaskThreeRelateInfo(request, userId);
		
		/** 获取任务四相关信息 */
		getTaskFourRelateInfo(request, userId);
		
		/** 获取任务五相关信息 */
		getTaskFiveRelateInfo(request, userId);
		
		/** 获取任务六相关信息 */
		getTaskSixRelateInfo(request, userId);
		
		/** 用户已完成任务相关 */
		getUserTaskCompleteInfo(request, userId);
		
		/** 大礼相关状态 */
		getFinalBigPrizeStatus(request, userId);
		return "/WEB-INF/views/hc9/activity/year2016/month05/newertask";
	}
	
	/*** 任务一红包相关领取 */
	@RequestMapping(value="/taskOneReceive.htm", method = RequestMethod.POST)
	@ResponseBody
	private String taskOneReceive(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = newerTaskActivityService.taskOneReceive(user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/*** 任务二红包相关领取 */
	@RequestMapping(value="/taskTwoReceive.htm", method = RequestMethod.POST)
	@ResponseBody
	private String taskTwoReceive(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = newerTaskActivityService.taskTwoReceive(user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/*** 任务三红包相关领取 */
	@RequestMapping(value="/taskThreeReceive.htm", method = RequestMethod.POST)
	@ResponseBody
	private String taskThreeReceive(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = newerTaskActivityService.taskThreeReceive(user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/*** 任务四红包相关领取 */
	@RequestMapping(value="/taskFourReceive.htm", method = RequestMethod.POST)
	@ResponseBody
	private String taskFourReceive(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = newerTaskActivityService.taskFourReceive(user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/*** 任务五红包相关领取 */
	@RequestMapping(value="/taskFiveReceive.htm", method = RequestMethod.POST)
	@ResponseBody
	private String taskFiveReceive(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = newerTaskActivityService.taskFiveReceive(user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/*** 任务六红包相关领取 */
	@RequestMapping(value="/taskSixReceive.htm", method = RequestMethod.POST)
	@ResponseBody
	private String taskSixReceive(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = newerTaskActivityService.taskSixReceive(user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/*** 终极大奖相关领取 */
	@RequestMapping(value="/finalBigPrizeReceive.htm", method = RequestMethod.POST)
	@ResponseBody
	private String finalBigPrizeReceive(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = newerTaskActivityService.finalBigPrizeReceive(user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/** 获取收集到所有钥匙的新手的人数 */
	private void getCollectAllKeyNum(HttpServletRequest request) {
		long collectAllNum = HcNewerTaskCache.getCollectAllKeyNum();
		request.setAttribute("collectAllNum", collectAllNum);
	}
	
	/** 获取任务一相关信息 */
	private void getTaskOneRelateInfo(HttpServletRequest request, long userId) {
		long taskOneTotalNum = HcNewerTaskCache.getTaskOneReceivedNum();
		if(userId > 0) {
			String taskOneStatus = HcNewerTaskCache.getUserTaskOneStatus(userId);
			request.setAttribute("taskOneStatus", taskOneStatus);
		}
		request.setAttribute("taskOneTotalNum", taskOneTotalNum);
	}
	
	/** 获取任务二相关信息 */
	private void getTaskTwoRelateInfo(HttpServletRequest request, long userId) {
		long taskTwoTotalNum = HcNewerTaskCache.getTaskTwoReceivedNum();
		if(userId > 0) {
			String taskTwoStatus = HcNewerTaskCache.getUserTaskTwoStatus(userId);
			request.setAttribute("taskTwoStatus", taskTwoStatus);
		}
		request.setAttribute("taskTwoTotalNum", taskTwoTotalNum);
	}
	
	/** 获取任务三相关信息 */
	private void getTaskThreeRelateInfo(HttpServletRequest request, long userId) {
		long taskThreeTotalNum = HcNewerTaskCache.getTaskThreeReceivedNum();
		if(userId > 0) {
			String taskThreeStatus = HcNewerTaskCache.getUserTaskThreeStatus(userId);
			request.setAttribute("taskThreeStatus", taskThreeStatus);
		}
		request.setAttribute("taskThreeTotalNum", taskThreeTotalNum);
	}
	
	/** 获取任务四相关信息 */
	private void getTaskFourRelateInfo(HttpServletRequest request, long userId) {
		long taskFourTotalNum = HcNewerTaskCache.getTaskFourReceivedNum();
		if(userId > 0) {
			String taskFourStatus = HcNewerTaskCache.getUserTaskFourStatus(userId);
			request.setAttribute("taskFourStatus", taskFourStatus);
		}
		request.setAttribute("taskFourTotalNum", taskFourTotalNum);
	}
	
	/** 获取任务五相关信息 */
	private void getTaskFiveRelateInfo(HttpServletRequest request, long userId) {
		long taskFiveTotalNum = HcNewerTaskCache.getTaskFiveReceivedNum();
		if(userId > 0) {
			String taskFiveStatus = HcNewerTaskCache.getUserTaskFiveStatus(userId);
			request.setAttribute("taskFiveStatus", taskFiveStatus);
		}
		request.setAttribute("taskFiveTotalNum", taskFiveTotalNum);
	}
	
	/** 获取任务六相关信息 */
	private void getTaskSixRelateInfo(HttpServletRequest request, long userId) {
		long taskSixTotalNum = HcNewerTaskCache.getTaskSixReceivedNum();
		if(userId > 0) {
			String taskSixStatus = HcNewerTaskCache.getUserTaskSixStatus(userId);
			request.setAttribute("taskSixStatus", taskSixStatus);
		}
		request.setAttribute("taskSixTotalNum", taskSixTotalNum);
	}
	
	/** 用户已完成任务相关 */
	private void getUserTaskCompleteInfo(HttpServletRequest request, long userId) {
		if(userId > 0) {
			int completeTaskNum = HcNewerTaskCache.getUserTaskCompleteInfo(userId);
			int unCompleteTaskNum = 6 - completeTaskNum;
			request.setAttribute("completeTaskNum", completeTaskNum);
			request.setAttribute("unCompleteTaskNum", unCompleteTaskNum);
		}
	}
	
	/** 获取大礼相关状态 */
	private void getFinalBigPrizeStatus(HttpServletRequest request, long userId) {
		if(userId > 0) {
			if(HcNewerTaskCache.isNewerRegisterInActivityArea(userId)) {
				if(HcNewerTaskCache.isUserFinishAllTask(userId)) {
					/** 是否已经领取过 */
					String receiveKey = "STR:HC9:BIG:PRIZE:FINAL:RED:KEY:" + userId;
					if(!RedisHelper.isKeyExist(receiveKey)) {
						request.setAttribute("finishAllTaskStatus", "1");
					} else {
						request.setAttribute("finishAllTaskStatus", "2");
					}
				} else {
					request.setAttribute("finishAllTaskStatus", "0");
				}
			} else {
				request.setAttribute("finishAllTaskStatus", "3");
			}			
		} else {
			request.setAttribute("finishAllTaskStatus", "0");
		}
	}
}