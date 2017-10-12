package com.hc9.web.main.redis.activity.year2016.month05;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.service.activity.ActivityCommonService;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.StringUtil;

/** 聚橙网活动 */
public class HcJuChengCache {
	
	/** 聚橙网投资免费送门票活动：1、单笔投资满15000元免费送票一张；2、单笔投资满40000元免费送票两张；
	 *  @param userId 投资用户id
	 *  @param investType 投资类型：投资类型 1 优先 2 夹层 3劣后
	 *  @param investMoney 投资金额
	 *  @param loanRecordId 投资记录id
	 *  @param loanType 标类型：1-店铺  2-项目 3-天标 4-债权转让
	 *  @param remonth 回购期限:如果是天标的话为天数，项目的话为月数
	 *  */
	public static void giveTicketForFirstTime(long userId, int investType, double investMoney, 
			long loanRecordId, int loanType, int remonth, String phone, String investOrderNum, 
			long loanId, String loanName, ActivityCommonService activityCommonService) {
		try {
			if(isJuChengActivity(new Date())) {
				/** 用户是否已经获取过演唱会门票 */
				String key = "STR:HC9:JUCHENG:YCH:TICKET:" + userId;
				if(!RedisHelper.isKeyExist(key)) {
					/** 用户只可认购优先与夹层来参加活动，劣后不参与， 25天以上的标的参加活动 */
					if(investType == 1 || investType == 2) {
						boolean flag = true;
						if(loanType == 3 && remonth < 25) {
							flag = false;
						}
						if(flag) {
							if(investMoney >= 10000) {
								int prizeNum = 1;
								if(investMoney >= 30000) {
									prizeNum = 2;
								}
								prizeNum = hasAbleTickt(prizeNum);
								if(prizeNum > 0) {
									/** 一个用户只能有一次送票的机会 */
									if(!activityCommonService.isUserGetPrizeByType(userId, 17)) {
										activityCommonService.savePrizedetail(userId, 17, "聚橙网演唱会门票", 
												prizeNum, loanRecordId);
										activityCommonService.saveAcivityMoney(userId, phone, Double.valueOf(prizeNum), 
												16, investMoney, loanRecordId, loanId, loanName);
										RedisHelper.set(key, "1");
										if(StringUtil.isNotBlank(investOrderNum)) {
											String orderKey = "STR:HC9:JUCHENG:INVEST:PRIZENUM:" + investOrderNum;
											RedisHelper.setWithExpireTime(orderKey, "" + prizeNum, 60 * 60);
										}
										increaseGivenTicktNum(prizeNum);
										updateJuChengPrizeList(userId, phone, prizeNum);
									}
								}
							}
						}
					}
				}
			}
		} catch(Exception e) {
			LOG.error("聚橙网投资送门票活动新增用户活动期间现金奖励报错！", e);
		}
	}
	
	/** 判断是否有可用的剩余门票 */
	public static int hasAbleTickt(int prizeNum) {
		int result = 0;
		int totalTicket = 0;
		String key = "STR:HC9:JU:CHENG:TICKET:ALL";
		String num = RedisHelper.get(key);
		if(StringUtil.isNotBlank(num)) {
			totalTicket = Integer.valueOf(num); 
		}
		if(totalTicket >= 42) {
			result = 0;
		} else {
			int leftNum = 42 - totalTicket;
			if(leftNum < prizeNum) {
				result = leftNum;
			} else {
				result = prizeNum;
			}
		}
		return result;
	}
	
	/** 剩余门票张数 */
	public static int getLeftTicketNum() {
		int letTicketNum = 42;
		String key = "STR:HC9:JU:CHENG:TICKET:ALL";
		String num = RedisHelper.get(key);
		if(StringUtil.isNotBlank(num)) {
			letTicketNum = 42 - Integer.valueOf(num); 
		}
		if(letTicketNum < 0) {
			letTicketNum = 0;
		}
		return letTicketNum;
	}
	
	/** 门票发放成功后新增票数 */
	public static long increaseGivenTicktNum(int prizeNum) {
		String key = "STR:HC9:JU:CHENG:TICKET:ALL";
		long totalNum = RedisHelper.incrBy(key, prizeNum);
		return totalNum;
	}
	
	/** 更新聚橙网演唱会门票获奖列表 */
	public static void updateJuChengPrizeList(long userId, String phone, int prizeNum) {
		List<PrizeVo> resultList = new ArrayList<PrizeVo>();
		String key = "STR:HC9:JU:CHENG:PRIZE:LIST";
		String json = RedisHelper.get(key);
		if(StringUtil.isNotBlank(json)) {
			resultList = JsonUtil.jsonToList(json, PrizeVo.class);
		}
		PrizeVo vo = new PrizeVo();
		vo.setUserId(userId);
		vo.setPhone(phone);
		vo.setPrizeNum(prizeNum);
		resultList.add(vo);
		json = JsonUtil.toJsonStr(resultList);
		RedisHelper.set(key, json);
	}
	
	/** 获取聚橙网活动2016年5月10日-2016年6月8日活动开始时间 */
	public static String getJuChengActivityBeginDate() {
		String beginDateKey = "STR:HC9:JU:CHENG:BEGIN:DATE";
		String beginDate = "";
		try {
			beginDate = RedisHelper.get(beginDateKey);
			if(StringUtil.isBlank(beginDate)) {
				beginDate = "2016-05-10 00:00:00";
				RedisHelper.set(beginDateKey, beginDate);
			}
		} catch(Exception e) {
			LOG.error("获取聚橙网活动开始时间报错,使用默认开始时间2016-05-10 00:00:00！", e);
			beginDate = "2016-05-10 00:00:00";
		}
		return beginDate;
	}
	
	/** 获取聚橙网活动2016年5月9日-2016年6月8日活动结束时间 */
	public static String getJuChengActivityEndDate() {
		String endDateKey = "STR:HC9:JU:CHENG:END:DATE";
		String endDate = "";
		try {
			endDate = RedisHelper.get(endDateKey);
			if(StringUtil.isBlank(endDate)) {
				endDate = "2016-06-08 23:59:59";
				RedisHelper.set(endDateKey, endDate);
			}
		} catch(Exception e) {
			LOG.error("获取聚橙网活动的结束时间报错,使用默认结束时间2016-06-08 23:59:59！", e);
			endDate = "2016-06-08 23:59:59";
		}
		return endDate;
	}
	
	/** 判断是否是聚橙网活动2016年5月9日-2016年6月8日活动期间 */
	public static boolean isJuChengActivity(Date currentDate) {
		boolean result = true;
		/** 活动开始时间 */
		String beginDateStr = getJuChengActivityBeginDate();
		
		/** 活动结束时间 */
		String endDateStr = getJuChengActivityEndDate();

		Date beginDate = DateFormatUtil.stringToDate(beginDateStr, "yyyy-MM-dd HH:mm:ss");
		Date endDate = DateFormatUtil.stringToDate(endDateStr, "yyyy-MM-dd HH:mm:ss");
		
		/** 当前时间早于活动开始时间 */
		if(currentDate.before(beginDate)) {
			result = false;
		}
		
		if(endDate.before(currentDate)) {
			result = false;
		}
		return result;
	}
}