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

import com.hc9.commons.log.LOG;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2016.month04.HcClimbTopActivityCache;
import com.hc9.web.main.redis.activity.year2016.month04.HcOpenCardActivityCache;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.activity.year2016.Month04ActivityService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.OpenCardVo;

/** 四月活动相关入口类 */
@RequestMapping({ "april", "/" })
@Controller
public class Month04ActivityController {
	@Resource
	private Month04ActivityService month04ActivityService;
	
	@Resource
	private UserbasicsinfoService userbasicsinfoService;
	
	/** 跳转到登顶活动页面 */
	@RequestMapping("/toclimbtop.htm")
	public String toClimbTop(HttpServletRequest request,Integer sit) {
		/** 登顶活动相关逻辑 */
		climbTopActivity(request);
		
		/*** 抽奖活动相关逻辑 */
		lotteryActivity(request);
		request.setAttribute("sit", sit);
		return "/WEB-INF/views/hc9/activity/climbTopFilpBoard";
	}
	
	/** 跳转到抢标排位活动页面 */
	@RequestMapping("/grabQualifying.htm")
	public String grabQualifying(HttpServletRequest request) {
		return "/WEB-INF/views/hc9/activity/grabQualifying";
	}
	
	/*** 登顶活动相关逻辑 */
	public void climbTopActivity(HttpServletRequest request) {
		Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		double floorNum = 0;
		if(userbasic != null) {
			long userId = userbasic.getId();
			long totalInvest = HcClimbTopActivityCache.getUserTotalInvest(userId);
			long subNetMoney = 0;
			if(totalInvest > 0) {
				if(totalInvest < 1000) {
					floorNum = 0.5;
					subNetMoney = 1000 - totalInvest;
				} else if(totalInvest == 1000) {
					floorNum = 1;
					subNetMoney = 10000 - totalInvest;
				} else if(totalInvest > 1000 && totalInvest < 10000) {
					floorNum = 1.5;
					subNetMoney = 10000 - totalInvest;
				} else if(totalInvest == 10000) {
					floorNum = 2;
					subNetMoney = 100000 - totalInvest;
				} else if(totalInvest > 10000 && totalInvest < 100000) {
					floorNum = 2.5;
					subNetMoney = 100000 - totalInvest;
				} else if(totalInvest == 100000) {
					floorNum = 3;
					subNetMoney = 500000 - totalInvest;
				} else if(totalInvest > 100000 && totalInvest < 500000) {
					floorNum = 3.5;
					subNetMoney = 500000 - totalInvest;
				} else if(totalInvest == 500000) {
					floorNum = 4;
					subNetMoney = 2000000 - totalInvest;
				} else if(totalInvest > 500000 && totalInvest < 2000000) {
					floorNum = 4.5;
					subNetMoney = 2000000 - totalInvest;
				} else if(totalInvest >= 2000000) {
					floorNum = 5;
				}
			}
			request.setAttribute("totalInvest", totalInvest);
			request.setAttribute("subNetMoney", subNetMoney);
		}
		computeClimbTopInvestPersonNum(request);
		request.setAttribute("floorNum", floorNum);
	}
	
	/** 获取登顶活动各级的相关投资人数 */
	private void computeClimbTopInvestPersonNum(HttpServletRequest request) {
		String oneKey = "STR:HC9:CLIMB:PERSON:NUM:ONE:LEVEL";
		String twoKey = "STR:HC9:CLIMB:PERSON:NUM:TWO:LEVEL";
		String threeKey = "STR:HC9:CLIMB:PERSON:NUM:THREE:LEVEL";
		String fourKey = "STR:HC9:CLIMB:PERSON:NUM:FOUR:LEVEL";
		String fiveKey = "STR:HC9:CLIMB:PERSON:NUM:FIVE:LEVEL";
		String oneKeyNum = RedisHelper.get(oneKey);
		String twoKeyNum = RedisHelper.get(twoKey);
		String threeKeyNum = RedisHelper.get(threeKey);
		String fourKeyNum = RedisHelper.get(fourKey);
		String fiveKeyNum = RedisHelper.get(fiveKey);
		
		if(StringUtil.isNotBlank(oneKeyNum)) {
			List<Long> list = JsonUtil.jsonToList(oneKeyNum, Long.class);
			request.setAttribute("oneLevelNum", list.size());
		} else {
			request.setAttribute("oneLevelNum", 0);
		}
		
		if(StringUtil.isNotBlank(twoKeyNum)) {
			List<Long> list = JsonUtil.jsonToList(twoKeyNum, Long.class);
			request.setAttribute("twoLevelNum", list.size());
		} else {
			request.setAttribute("twoLevelNum", 0);
		}
		
		if(StringUtil.isNotBlank(threeKeyNum)) {
			List<Long> list = JsonUtil.jsonToList(threeKeyNum, Long.class);
			request.setAttribute("threeLevelNum", list.size());
		} else {
			request.setAttribute("threeLevelNum", 0);
		}
		
		if(StringUtil.isNotBlank(fourKeyNum)) {
			List<Long> list = JsonUtil.jsonToList(fourKeyNum, Long.class);
			request.setAttribute("fourLevelNum", list.size());
		} else {
			request.setAttribute("fourLevelNum", 0);
		}
		
		if(StringUtil.isNotBlank(fiveKeyNum)) {
			List<Long> list = JsonUtil.jsonToList(fiveKeyNum, Long.class);
			request.setAttribute("fiveLevelNum", list.size());
		} else {
			request.setAttribute("fiveLevelNum", 0);
		}
	}
	
	/*** 抽奖活动相关逻辑 */
	public void lotteryActivity(HttpServletRequest request) {
		Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		long openCardNum = 0;
		long totalInvestNum = 0;
		if(userbasic != null) {
			long userId = userbasic.getId();
			openCardNum = HcOpenCardActivityCache.getTotalOpenCardNUm(userId);
			totalInvestNum = HcOpenCardActivityCache.getTotalInvestNum(userId);
		}
		Map<String, String> resultMap = new HashMap<String, String>();
		queryUserLeftLotterLeftNum(userbasic, resultMap);
		request.setAttribute("oneLeveLeftNum", resultMap.get("oneLeveLeftNum"));
		request.setAttribute("twoLeveLeftNum", resultMap.get("twoLeveLeftNum"));
		request.setAttribute("threeLeveLeftNum", resultMap.get("threeLeveLeftNum"));
		request.setAttribute("fourLeveLeftNum", resultMap.get("fourLeveLeftNum"));
		request.setAttribute("openCardNum", openCardNum);
		request.setAttribute("totalInvestNum", totalInvestNum);
		
	}
	
	/** 翻牌抽奖相关逻辑 */
	@RequestMapping(value="/getOpenCardLotteryNum.htm", method = RequestMethod.POST)
	@ResponseBody
	public String getOpenCardLotteryNum(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		long totalNum = 0;
		if(user != null) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOG.error("获取翻牌次数过程中出错！", e);
			}
			long userId = user.getId();
			totalNum = getUnUsedOpenCardNum(userId);
		}
		resultMap.put("totalNum", "" + totalNum);
		String jsonStr = JsonUtil.toJsonStr(resultMap);
		return jsonStr;
	}
	
	/** 获取用户尚未使用的翻牌抽奖次数 */
	private long getUnUsedOpenCardNum(long userId) {
		long totalNum = 0;
		List<OpenCardVo> investList = HcOpenCardActivityCache.getOpenCardInvestList(userId);
		if(investList != null && investList.size() > 0) {
			for(OpenCardVo vo : investList) {
				if(vo.getUseFlag() == 0) {
					totalNum++;
				}
			}
		}
		return totalNum;
	}
	
	/** 翻牌抽奖相关逻辑 */
	@RequestMapping(value="/openCardLottery.htm", method = RequestMethod.POST)
	@ResponseBody
	public String openCardLottery(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			if(HcOpenCardActivityCache.isOpenCardActivity(new Date())) {
				long userId = user.getId();
				long totalNum = HcOpenCardActivityCache.getTotalOpenCardNUm(userId);
				if(totalNum < 1) {
					resultMap.put("code", "-1");
					resultMap.put("msg", "您的抽奖次数为0，快去投资，投资越高奖励越高哦");
				} else {
					totalNum = getUnUsedOpenCardNum(userId);
					if(totalNum > 0) {
						String lotteryType = request.getParameter("lotteryType");
						List<OpenCardVo> investList = HcOpenCardActivityCache.getOpenCardInvestList(userId);
						if(investList == null || investList.size() < 1) {
							resultMap.put("code", "-1");
							resultMap.put("msg", "您的抽奖次数为0，快去投资，投资越高奖励越高哦");
						} else {
							if(user.getUserrelationinfo() == null) {
								user = userbasicsinfoService.queryUserById(user.getId());
								request.getSession().setAttribute(Constant.SESSION_USER, user);
							}
							resultMap = openCardLotteryLogic(user, lotteryType, investList);
							String lotteryResult = resultMap.get("lotteryResult");
							if(Long.valueOf(lotteryResult) > 0) {
								resultMap.put("code", "0");
								resultMap.put("msg", "翻牌抽奖成功！");
							} else {
								String errorFlag = resultMap.get("errorFlag");
								if("1".equals(errorFlag)) {
									resultMap.put("code", "1");
									resultMap.put("msg", "后台异常，抽奖失败！");
								} else {
									String lotteryFlag = resultMap.get("lotteryFlag");
									resultMap.put("code", "-1");
									String msg = "可用抽奖次数为0，抽奖失败！";
									String plusMsg = "当前层级";
									if("1".equals(lotteryFlag)) {
										plusMsg = "友善卡";
									} else if("2".equals(lotteryFlag)) {
										plusMsg = "富强卡";
									} else if("3".equals(lotteryFlag)) {
										plusMsg = "爱国卡";
									} else if("4".equals(lotteryFlag)) {
										plusMsg = "敬业卡";
									}
									
									resultMap.put("msg", plusMsg + msg);
								}
							}
						}
					} else {
						resultMap.put("code", "-1");
						resultMap.put("msg", "您的抽奖次数为0，快去投资，投资越高奖励越高哦");
					}
				}
				totalNum = HcOpenCardActivityCache.getTotalOpenCardNUm(userId);
				resultMap.put("openCardNum", "" + totalNum);
				long totalInvestNum = HcOpenCardActivityCache.getTotalInvestNum(userId);
				resultMap.put("totalInvestNum", "" + totalInvestNum);
			} else {
				resultMap.put("code", "-4");
				resultMap.put("msg", "不在活动期间内，抽奖失败！");
			}
		} else {
			resultMap.put("code", "-2");
			resultMap.put("msg", "尚未登录！");
		}
		queryUserLeftLotterLeftNum(user, resultMap);
		String jsonStr = JsonUtil.toJsonStr(resultMap);
		return jsonStr;
	}
	
	/** 翻牌抽奖逻辑 */
	private Map<String, String> openCardLotteryLogic(Userbasicsinfo user, String lotteryType, 
			List<OpenCardVo> investList) {
		Map<String, String> resultMap = new HashMap<String, String>();
		 /** 同一个用户90秒内不能并发抽奖 */
		 String userIdConcurrentLock = "STR:HC9:APRIL:OPEN:CARD:LOCK:" + user.getId();
		 if(!RedisHelper.isKeyExistSetWithExpire(userIdConcurrentLock, 90)) {
			 if("1".equals(lotteryType)) {
				 resultMap = oneLevelLottery(user, lotteryType, investList);
			 } else if("2".equals(lotteryType)) {
				 resultMap = twoLevelLottery(user, lotteryType, investList);
			 } else if("3".equals(lotteryType)) {
				 resultMap = threeLevelLottery(user, lotteryType, investList);
			 } else if("4".equals(lotteryType)) {
				 resultMap = fourLevelLottery(user, lotteryType, investList);
			 } else {
				 resultMap.put("code", "-3");
				 resultMap.put("msg", "非法请求，抽奖失败！");
			 }
			 RedisHelper.del(userIdConcurrentLock);
		 } else {
			 resultMap.put("code", "-3");
			 resultMap.put("msg", "非法请求，抽奖失败！");
		 }
		 return resultMap;
	}
	
	/** 一级抽奖 */
	private Map<String, String> oneLevelLottery(Userbasicsinfo user, String lotteryType, List<OpenCardVo> investList) {
		Map<String, String> resultMap = new HashMap<String, String>();
		long lotteryResult = 0;
		for(OpenCardVo vo : investList) {
			int useFlag = vo.getUseFlag();
			if(useFlag == 0) {
				double investMoney = vo.getInvestMoney();
				if(investMoney >= 100 && investMoney < 1000) {
					lotteryResult = HcOpenCardActivityCache.oneLevelLottery();
					vo.setUseFlag(1);
					vo.setLotteryResult(lotteryResult);
					break;
				}
			}
		}
		if(lotteryResult > 0) {
			long userId = user.getId();
			try {
				String phone = user.getUserrelationinfo().getPhone();
				month04ActivityService.oneLevelLottery(user, phone, lotteryResult);
			} catch(Exception e) {
				lotteryResult = 0;
				resultMap.put("errorFlag", "1");
				LOG.error("一级抽奖过程中报错，userId:" + userId, e);
			}
			if(lotteryResult > 0) {
				HcOpenCardActivityCache.updateOpenCardInvestList(userId, investList);
			}
		}
		resultMap.put("lotteryResult", "" + lotteryResult);
		resultMap.put("lotteryFlag", "1");
		return resultMap;
	}
	
	/** 二级抽奖 */
	private Map<String, String> twoLevelLottery(Userbasicsinfo user, String lotteryType, 
			List<OpenCardVo> investList) {
		Map<String, String> resultMap = new HashMap<String, String>();
		long lotteryResult = 0;
		for(OpenCardVo vo : investList) {
			int useFlag = vo.getUseFlag();
			if(useFlag == 0) {
				double investMoney = vo.getInvestMoney();
				if(investMoney >= 1000 && investMoney < 10000) {
					lotteryResult = HcOpenCardActivityCache.twoLevelLottery();
					vo.setUseFlag(1);
					vo.setLotteryResult(lotteryResult);
					break;
				}
			}
		}
		if(lotteryResult > 0) {
			long userId = user.getId();
			try {
				String phone = user.getUserrelationinfo().getPhone();
				month04ActivityService.twoLevelLottery(user, phone, lotteryResult);
			} catch(Exception e) {
				lotteryResult = 0;
				resultMap.put("errorFlag", "1");
				LOG.error("二级抽奖过程中报错，userId:" + userId, e);
			}
			if(lotteryResult > 0) {
				HcOpenCardActivityCache.updateOpenCardInvestList(userId, investList);
			}
		}
		resultMap.put("lotteryResult", "" + lotteryResult);
		resultMap.put("lotteryFlag", "2");
		return resultMap;
	}
	
	/** 三级抽奖 */
	private Map<String, String> threeLevelLottery(Userbasicsinfo user, String lotteryType, 
			List<OpenCardVo> investList) {
		Map<String, String> resultMap = new HashMap<String, String>();
		long lotteryResult = 0;
		for(OpenCardVo vo : investList) {
			int useFlag = vo.getUseFlag();
			if(useFlag == 0) {
				double investMoney = vo.getInvestMoney();
				if(investMoney >= 10000 && investMoney < 100000) {
					lotteryResult = HcOpenCardActivityCache.threeLevelLottery();
					vo.setUseFlag(1);
					vo.setLotteryResult(lotteryResult);
					break;
				}
			}
		}
		if(lotteryResult > 0) {
			long userId = user.getId();
			try {
				String phone = user.getUserrelationinfo().getPhone();
				month04ActivityService.threeLevelLottery(user, phone, lotteryResult);
			} catch(Exception e) {
				LOG.error("三级抽奖过程中报错，userId:" + userId, e);
				lotteryResult = 0;
				resultMap.put("errorFlag", "1");
			}
			if(lotteryResult > 0) {
				HcOpenCardActivityCache.updateOpenCardInvestList(userId, investList);
			}
		}
		resultMap.put("lotteryResult", "" + lotteryResult);
		resultMap.put("lotteryFlag", "3");
		return resultMap;
	}
	
	/** 四级抽奖 */
	private Map<String, String> fourLevelLottery(Userbasicsinfo user, String lotteryType, 
			List<OpenCardVo> investList) {
		Map<String, String> resultMap = new HashMap<String, String>();
		long lotteryResult = 0;
		for(OpenCardVo vo : investList) {
			int useFlag = vo.getUseFlag();
			if(useFlag == 0) {
				double investMoney = vo.getInvestMoney();
				if(investMoney >= 100000) {
					lotteryResult = HcOpenCardActivityCache.fourLevelLottery();
					vo.setUseFlag(1);
					vo.setLotteryResult(lotteryResult);
					break;
				}
			}
		}
		if(lotteryResult > 0) {
			long userId = user.getId();
			try {
				String phone = user.getUserrelationinfo().getPhone();
				month04ActivityService.fourLevelLottery(user, phone, lotteryResult);
			} catch(Exception e) {
				LOG.error("四级抽奖过程中报错，userId:" + userId, e);
				resultMap.put("errorFlag", "1");
			}
			if(lotteryResult > 0) {
				HcOpenCardActivityCache.updateOpenCardInvestList(userId, investList);
			}
		}
		resultMap.put("lotteryResult", "" + lotteryResult);
		resultMap.put("lotteryFlag", "4");
		return resultMap;
	}
	
	/** 每种翻牌的抽奖次数信息 */
	private void queryUserLeftLotterLeftNum(Userbasicsinfo user, Map<String, String> resultMap) {
		int oneLeveLeftNum = 0;
		int twoLeveLeftNum = 0;
		int threeLeveLeftNum = 0;
		int fourLeveLeftNum = 0;
		if(user != null) {
			long userId = user.getId();
			List<OpenCardVo> investList = HcOpenCardActivityCache.getOpenCardInvestList(userId);
			if(investList != null && investList.size() > 0) {
				for(OpenCardVo vo : investList) {
					int useFlag = vo.getUseFlag();
					double investMoney = vo.getInvestMoney();
					if(useFlag == 0) {
						if(investMoney >= 100 && investMoney < 1000) {
							oneLeveLeftNum++;
						} else if(investMoney >= 1000 && investMoney < 10000) {
							twoLeveLeftNum++;
						} else if(investMoney >= 10000 && investMoney < 100000) {
							threeLeveLeftNum++;
						} else if(investMoney >= 100000) {
							fourLeveLeftNum++;
						}
					}
				}
			}
		}
		resultMap.put("oneLeveLeftNum", "" + oneLeveLeftNum);
		resultMap.put("twoLeveLeftNum", "" + twoLeveLeftNum);
		resultMap.put("threeLeveLeftNum", "" + threeLeveLeftNum);
		resultMap.put("fourLeveLeftNum", "" + fourLeveLeftNum);
	}
}