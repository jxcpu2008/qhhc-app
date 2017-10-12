package com.hc9.web.main.service.activity.year2016;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2016.month05.HcNewerTaskCache;
import com.hc9.web.main.service.activity.ActivityCommonService;

/** 5月份活动 */
@Service
public class Month05NewerTaskActivityService {
	@Resource
	private ActivityCommonService activityCommonService;
	
	@Resource
	private HibernateSupport dao;
	
	/** 任务一红包领取 */
	public Map<String, String> taskOneReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if(HcNewerTaskCache.isNewerRegisterInActivityArea(userId)) {
			String receiveKey = "STR:HC9:TASK:ONE:RECEIVED:FLAG:" + userId;
			if(!RedisHelper.isKeyExist(receiveKey)) {
				activityCommonService.saveRedEnvelope(userId, 5, 100, 2, 2);
				activityCommonService.saveRedEnvelope(userId, 5, 100, 2, 2);
				activityCommonService.saveRedEnvelope(userId, 30, 3000, 2, 2);
				activityCommonService.saveRedEnvelope(userId, 60, 8000, 2, 2);
				RedisHelper.set(receiveKey, "1");
				
				/** 系统所有已经获取注册送100元红包钥匙的总人数 */
				String sysTotalNumKey = "STR:HC9:NEWER:REGISTER:RED:TOTAL:KEY";
				RedisHelper.incrBy(sysTotalNumKey, 1);
				
				HcNewerTaskCache.setTaskOneCompleteTime(userId);
				
				resultMap.put("completeTaskNum", "" + HcNewerTaskCache.getUserTaskCompleteInfo(userId));
				resultMap.put("code", "0");
				resultMap.put("msg", "领取成功！");
				return resultMap;
			}
		}
		resultMap.put("code", "-2");
		resultMap.put("msg", "领取失败！");
		return resultMap;
	}
	
	/** 任务二红包领取 */
	public Map<String, String> taskTwoReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if(HcNewerTaskCache.isNewerRegisterInActivityArea(userId)) {
			String userSelfKey = "STR:HC9:FIRST:RECHARGE:RED:KEY:" + userId;
			if(RedisHelper.isKeyExist(userSelfKey)) {
				String receiveKey = "STR:HC9:TASK:TWO:RECEIVED:FLAG:" + userId;
				if(!RedisHelper.isKeyExist(receiveKey)) {
					activityCommonService.saveRedEnvelope(userId, 5, 100, 10, 2);
					RedisHelper.set(receiveKey, "1");
					
					/** 系统所有已经获取首次充值送5元红包钥匙的总人数 */
					String sysTotalNumKey = "STR:HC9:FIRST:RECHARGE:RED:TOTAL:KEY";
					RedisHelper.incrBy(sysTotalNumKey, 1);
					
					HcNewerTaskCache.setTaskTwoCompleteTime(userId);
					resultMap.put("completeTaskNum", "" + HcNewerTaskCache.getUserTaskCompleteInfo(userId));
					resultMap.put("code", "0");
					resultMap.put("msg", "领取成功！");
					return resultMap;
				}
			}
		}
		resultMap.put("code", "-2");
		resultMap.put("msg", "领取失败！");
		return resultMap;
	}
	
	/** 任务三红包领取 */
	public Map<String, String> taskThreeReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if(HcNewerTaskCache.isNewerRegisterInActivityArea(userId)) {
			String userSelfKey = "STR:HC9:FIRST:INVEST:TASK:THREE:" + userId;
			if(RedisHelper.isKeyExist(userSelfKey)) {
				String receiveKey = "STR:HC9:TASK:THREE:RECEIVED:FLAG:" + userId;
				if(!RedisHelper.isKeyExist(receiveKey)) {
					activityCommonService.saveRedEnvelope(userId, 5, 100, 1, 2);
					activityCommonService.saveRedEnvelope(userId, 10, 1000, 1, 2);
					RedisHelper.set(receiveKey, "1");
					
					/** 系统所有已经获取任务三对应钥匙的总人数 */
					String totalNumKey = "STR:HC9:FIRST:INVEST:TASK:THREE:TOTAL";
					RedisHelper.incrBy(totalNumKey, 1);
					
					HcNewerTaskCache.setTaskThreeCompleteTime(userId);
					
					resultMap.put("completeTaskNum", "" + HcNewerTaskCache.getUserTaskCompleteInfo(userId));
					resultMap.put("code", "0");
					resultMap.put("msg", "领取成功！");
					return resultMap;
				}
			}
		}
		resultMap.put("code", "-2");
		resultMap.put("msg", "领取失败！");
		return resultMap;
	}
	
	/** 任务四红包领取 */
	public Map<String, String> taskFourReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if(HcNewerTaskCache.isNewerRegisterInActivityArea(userId)) {
			String userSelfKey = "STR:HC9:FIRST:INVEST:TASK:FOUR:" + userId;
			if(RedisHelper.isKeyExist(userSelfKey)) {
				String receiveKey = "STR:HC9:TASK:FOUR:RECEIVED:FLAG:" + userId;
				if(!RedisHelper.isKeyExist(receiveKey)) {
					activityCommonService.saveRedEnvelope(userId, 30, 3000, 1, 2);
					RedisHelper.set(receiveKey, "1");
					
					/** 系统所有已经获取任务四对应钥匙的总人数 */
					String totalNumKey = "STR:HC9:FIRST:INVEST:TASK:FOUR:TOTAL";
					RedisHelper.incrBy(totalNumKey, 1);
					
					HcNewerTaskCache.setTaskFourCompleteTime(userId);
					resultMap.put("completeTaskNum", "" + HcNewerTaskCache.getUserTaskCompleteInfo(userId));
					resultMap.put("code", "0");
					resultMap.put("msg", "领取成功！");
					return resultMap;
				}
			}
		}
		resultMap.put("code", "-2");
		resultMap.put("msg", "领取失败！");
		return resultMap;
	}
	
	/** 任务五红包领取 */
	public Map<String, String> taskFiveReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if(HcNewerTaskCache.isNewerRegisterInActivityArea(userId)) {
			String userSelfKey = "STR:HC9:FIRST:INVEST:TASK:FIVE:" + userId;
			if(RedisHelper.isKeyExist(userSelfKey)) {
				String receiveKey = "STR:HC9:TASK:FIVE:RECEIVED:FLAG:" + userId;
				if(!RedisHelper.isKeyExist(receiveKey)) {
					activityCommonService.saveRedEnvelope(userId, 60, 8000, 1, 2);
					RedisHelper.set(receiveKey, "1");
					
					/** 系统所有已经获取任务五对应钥匙的总人数 */
					String totalNumKey = "STR:HC9:FIRST:INVEST:TASK:FIVE:TOTAL";
					RedisHelper.incrBy(totalNumKey, 1);
					
					HcNewerTaskCache.setTaskFiveCompleteTime(userId);
					resultMap.put("completeTaskNum", "" + HcNewerTaskCache.getUserTaskCompleteInfo(userId));
					resultMap.put("code", "0");
					resultMap.put("msg", "领取成功！");
					return resultMap;
				}
			}
		}
		resultMap.put("code", "-2");
		resultMap.put("msg", "领取失败！");
		return resultMap;
	}
	
	/** 任务六红包领取 */
	public Map<String, String> taskSixReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if(HcNewerTaskCache.isNewerRegisterInActivityArea(userId)) {
			String userSelfKey = "STR:HC9:WE:CHAT:ATTENTION:RED:KEY:" + userId;
			if(RedisHelper.isKeyExist(userSelfKey)) {
				String receiveKey = "STR:HC9:TASK:SIX:RECEIVED:FLAG:" + userId;
				if(!RedisHelper.isKeyExist(receiveKey)) {
					activityCommonService.saveRedEnvelope(userId, 10, 1000, 9, 2);
					RedisHelper.set(receiveKey, "1");
					
					/** 关注微信号送红包钥匙的总人数 */
					String sysTotalNumKey = "STR:HC9:WE:CHAT:ATTENTION:RED:TOTAL:KEY";
					RedisHelper.incrBy(sysTotalNumKey, 1);
					HcNewerTaskCache.setTaskSixCompleteTime(userId);
					
					resultMap.put("completeTaskNum", "" + HcNewerTaskCache.getUserTaskCompleteInfo(userId));
					resultMap.put("code", "0");
					resultMap.put("msg", "领取成功！");
					return resultMap;
				}
			}
		}
		resultMap.put("code", "-2");
		resultMap.put("msg", "领取失败！");
		return resultMap;
	}
	
	/*** 终极大奖相关领取 */
	public Map<String, String> finalBigPrizeReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		if(HcNewerTaskCache.isUserFinishAllTask(userId)) {
			String receiveKey = "STR:HC9:BIG:PRIZE:FINAL:RED:KEY:" + userId;
			if(!RedisHelper.isKeyExist(receiveKey)) {
				activityCommonService.saveInterestIncreaseCard(userId, 0.003, 3000, 10, 1);
				activityCommonService.saveInterestIncreaseCard(userId, 0.008, 8000, 10, 1);
				activityCommonService.saveWithdrawCard(userId, 2.0, 6, 1);
				RedisHelper.set(receiveKey, "1");
				HcNewerTaskCache.updateCollectAllKeyNum(userId);
				HcNewerTaskCache.setTaskSevenReceiveTime(userId);
				HcNewerTaskCache.setTaskSevenCompleteTime(userId);
				resultMap.put("code", "0");
				resultMap.put("msg", "领取成功！");
				return resultMap;
			}
		} else {
			int completeTaskNum = HcNewerTaskCache.getUserTaskCompleteInfo(userId);
			int unCompleteTaskNum = 6 - completeTaskNum;
			if(completeTaskNum > 0 && unCompleteTaskNum > 0) {
				String msg = "已集齐" + completeTaskNum + "把钥匙，还差" 
						+ unCompleteTaskNum + "把钥匙";
				resultMap.put("code", "-3");
				resultMap.put("msg", msg);
				return resultMap;
			}
		}
		resultMap.put("code", "-2");
		resultMap.put("msg", "领取失败！");
		return resultMap;
	}
	
	/** 生日礼物领取 */
	public Map<String, String> birthdayGiftReceive(long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		String receiveKey = "STR:HC9:RECEIVE:BIRTHDAY:GIFT:FLAG:"  + userId;
		if(!RedisHelper.isKeyExist(receiveKey)) {
			activityCommonService.saveWithdrawCard(userId, 2.0, 5, 1);
			activityCommonService.saveInterestIncreaseCard(userId, 0.005, 10000, 9, 1);
			activityCommonService.saveRedEnvelope(userId, 60, 8000, 11, 11);
			RedisHelper.setWithExpireTime(receiveKey, "1", 25 * 60 * 60);
			resultMap.put("code", "0");
			resultMap.put("msg", "成功领取生日礼物！");
		} else {
			resultMap.put("code", "1");
			resultMap.put("msg", "已领取");
		}
		return resultMap;
	}
}
