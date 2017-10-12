package com.hc9.web.main.controller.activity.year2015;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.hc9.commons.log.LOG;
import com.hc9.web.main.entity.PrizeDetail;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.activity.year2015.HcNewyearActivitiCache;
import com.hc9.web.main.service.activity.year2015.NewyearActivityService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.vo.LotteryRank;

/** 新年抽奖活动相关入口 */
@RequestMapping({"newyear"})
@Controller
public class NewyearActivityController {
	
	@Resource
	private NewyearActivityService newyearActService;

	@RequestMapping("/addlottery")
	@ResponseBody   
	public String addLottery(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
		if(user != null) {
			HcNewyearActivitiCache.increasePermanentLotteryChance(user.getId(), 100);
		}
		return "success";
	}
	
	/** 新年抽奖活动页面 */
	@RequestMapping("/lottery.htm")
	public String lotteryNov(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
		if(user != null) {
			/** 查询中奖列表相关数据 */
			List<PrizeDetail> myLottery = newyearActService.queryLotteryListByUserId(user.getId());
			request.setAttribute("myLottery", myLottery);
			request.setAttribute("lotteryNum", HcNewyearActivitiCache.getLotteryChanceNumOfUser(user.getId()));
		} else {
			request.setAttribute("lotteryNum", "0");
		}
		/** 查询中奖列表相关数据 */
		List<LotteryRank> lotteryList = newyearActService.queryLotteryRankList();
		request.setAttribute("lotteryList", lotteryList);
		return "/WEB-INF/views/hc9/activity/newYearLottery";
	}
	
	@RequestMapping("/newyearLottery.htm")
	@ResponseBody
	public JSONObject newyearLottery(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
		/**
		 * 奖品信息：1、IPad MINI；2、Kindle电子书；3、红筹台历
		 *  4、3元红包；5、5元红包 ； 6、10元嗒嗒代金券；7、20元嗒嗒代金券；8、50元嗒嗒代金券； 
		 *  0:系统后台异常；-1:抽奖活动尚未开始; -2:抽奖活动已经结束;-3:无抽奖机会;-4:没登录;-5:首次抽奖且没有抽奖次数;-6:未开通宝付且授权
		 * */
		JSONObject json = new JSONObject();
		int prizeId = 0;
		int lotteryNum = 0;
		try {
			if(user != null) {
				lotteryNum = HcNewyearActivitiCache.getLotteryChanceNumOfUser(user.getId());
				Date now = new Date();
				prizeId = HcNewyearActivitiCache.validCurrentDate(now);
				if(prizeId >= 0) {
					Integer isAuthips = user.getIsAuthIps() != null ? user.getIsAuthIps() : 0;
					if (isAuthips == 1) {
						String currentTime = DateFormatUtil.dateToString(now, "yyyy-MM-dd HH:mm:ss");
						String currentDate = currentTime.substring(0, 10);
						Integer isFristLottery = newyearActService.isTodayFirstLottery(user.getId(),currentDate);
						if (isFristLottery <= 0 && lotteryNum == 0) {
							prizeId = -5;  // 表示首次抽奖且没有抽奖次数
						} else {
							prizeId = getNewyearLotteryResult(user,now,currentDate);
							lotteryNum = HcNewyearActivitiCache.getLotteryChanceNumOfUser(user.getId());
						}
					} else {
						prizeId = -6;    // 未开通宝付且授权
					}
				}
			}else{
				prizeId = -4;  // 表示没登录
			}
		} catch(Exception e) {
			LOG.error("抽奖出现异常：", e);
		}
		json.put("prize", prizeId);
		json.put("lotteryNum", lotteryNum);
		return json;
	}

	/**
	 * 如果符合活动规则且有抽奖次数，开始进行抽奖操作
	 * @param userId
	 * @return 奖品类别id - prizeId
	 */
	private int getNewyearLotteryResult(Userbasicsinfo user,Date now,String currentDate) {
		int prizeId = 0;
		Long userId = user.getId();
		try {
			int userLotteryNum = HcNewyearActivitiCache.getLotteryChanceNumOfUser(userId);
			if(userLotteryNum <= 0) {
				prizeId = -3;
			} else {
				prizeId = newyearActService.getLotteryResult(now, user);
				if(prizeId > 0) {
					int tempNum = HcNewyearActivitiCache.getTemporaryLotteryChance(userId, currentDate);
					if(tempNum > 0) {
						HcNewyearActivitiCache.decreaseTemporaryLotteryChance(userId, currentDate);
					} else {
						HcNewyearActivitiCache.decreasePermanentLotteryChance(userId);
					}
				}
			}
		} catch(Exception e) {
			LOG.error(userId + "抽奖出现错误信息：", e);
		}
		return prizeId;
	}
}