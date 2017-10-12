package com.hc9.web.main.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.entity.CardImgAudit;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2016.month03.HcPeachActivitiCache;
import com.hc9.web.main.redis.activity.year2016.month05.HcNewerTaskCache;
import com.hc9.web.main.service.CardImgAuditService;
import com.hc9.web.main.service.GeneralizeService;
import com.hc9.web.main.service.LoanSignService;
import com.hc9.web.main.service.MemberCenterService;
import com.hc9.web.main.service.RedEnvelopeDetailService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.activity.year2015.ActivityService;
import com.hc9.web.main.service.activity.year2016.Month05NewerTaskActivityService;
import com.hc9.web.main.service.baofo.BaoFuService;
import com.hc9.web.main.service.borrow.BorrowService;
import com.hc9.web.main.util.CSRFTokenManager;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.GenerateLinkUtils;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StatisticsUtil;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.PageModel;
import com.hc9.web.main.vo.pay.crs;

/** 会员中心首页 */
@Controller
@RequestMapping(value = { "member_index", "/" })
public class MemberCenterController {
	
	private static final Logger logger = Logger.getLogger(MemberCenterController.class);

	@Resource
	private MemberCenterService memberCenterService;

	@Resource
	private LoanSignService loanSignService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	/** 会员推广信息服务层 */
	@Resource
	private GeneralizeService generaLizeService;

	@Resource
	private BorrowService borrowService;

	@Resource
	private BaoFuService baoFuService;
	
	@Resource
	private RedEnvelopeDetailService redEnvelopeDetailService;
	
	@Resource
	private ActivityService activityService;
	
	@Resource
	private CardImgAuditService cardImgAuditService;
	
	@Resource
	private Month05NewerTaskActivityService month05NewerTaskActivityService;
	
	/***** new *******/
	/**
	 * 会员中心首页基本信息
	 * 
	 * @param request
	 *            请求
	 * @return 返回.jsp
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@CheckLoginOnMethod
	@RequestMapping("/member_center.htm")
	public String memberCenter(HttpServletRequest request) throws Exception {
		// 取到登录用户sesssion
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
		user = userbasicsinfoService.queryUserById(user.getId());
		Long userId = user.getId();
		if ("".equals(user.getName())) {
			user.setName("您好，您还没填写真实姓名");
		}
		request.getSession().setAttribute("session_user", user);
		
		// 获取红包过期消息
		Long msgId = Long.valueOf(0);
		String msgContent = "";
		Object object = memberCenterService.queryHongBaoExpireRemindMessage(userId);
		if (object != null) {
			Object[] arr = (Object[]) object;
			msgId = StatisticsUtil.getLongFromBigInteger(arr[0]);
			msgContent = StatisticsUtil.getStringFromObject(arr[1]);
			logger.debug("消息id[" + msgId + "] 消息内容[" + msgContent + "]");
		}
		request.setAttribute("msgId", msgId);
		request.setAttribute("msgContent", msgContent);
		
		// 获取资金信息
		boolean hasRecharged = memberCenterService.hasRecharged(user);
		// 投资概况 本月(1)，下一个月(2),本年(3)
		List arrylist = new ArrayList();
		// 待回款
		List arrlist = new ArrayList();
		if(hasRecharged) {
			arrylist = memberCenterService.queryInvestStatisticInfo(userId);
			arrlist = memberCenterService.queryBackMoneyStatisticInfo(userId);
		} else {
			Object[] investArr = new Object[]{0, 0.00};
			for (int i = 1; i < 5; i++) {
				arrylist.add(investArr);
			}
			
			for (int i = 1; i < 4; i++) {
				arrlist.add(investArr);
			}
		}
		request.setAttribute("history", arrylist);
		request.setAttribute("back", arrlist);
		// 获取用户的还款和回款信息
		List backAlsoRepaymentsList = new ArrayList();
		List payAlsoRepaymentList = new ArrayList();
		if(hasRecharged) {
//			backAlsoRepaymentsList = memberCenterService.queryRepaymentBackList(user.getId());
			backAlsoRepaymentsList = memberCenterService.repaymentBackListByType(userId,1,0);
			payAlsoRepaymentList = memberCenterService.repaymentBackListByType(userId,2,0);
		}
		request.setAttribute("backAlsoRepayments", backAlsoRepaymentsList);
		request.setAttribute("payAlsoRepayments", payAlsoRepaymentList);
		getComData(request, hasRecharged);
		if(hasRecharged){
			queryMoneyOfUser(request);
		}
		
		
		// "理财师" 标准提醒
		request.setAttribute("cashRewardPrompt", activityService.cashRewardStandard(userId));
		
		// 根据参数判断避免和开通宝付框同时弹出
		String[] index = request.getParameter("index").split("_");
		Integer lastNum = Integer.valueOf(index[index.length-1].toString());
		if (HcPeachActivitiCache.validCurrentDate(new Date()) == 0 && lastNum != 3) {
			Integer goldLottery = HcPeachActivitiCache.getPermanentLotteryChance(userId);
			boolean goldPechIsRemind = activityService.remindGoldPech(userId);  // 获取金桃活动是否包含自己注册中奖记录
			Integer isAuthIps = user.getIsAuthIps();
			if (isAuthIps == null) {
				isAuthIps = 0;
			}
			String cardImg = user.getUserrelationinfo().getCardImg();
			String currentDate = DateFormatUtil.dateToString(new Date(), "yyyy-MM-dd");
			CardImgAudit cardImgAudit = cardImgAuditService.getCardImgAudit(userId);
			
			Integer goldRemind = HcPeachActivitiCache.getUserRemind(userId, currentDate, "1");
			if (goldLottery > 0 && goldRemind == 0) {
				// "金桃朵朵" 剩余抽奖提醒
				request.setAttribute("peachNum", goldLottery);
				HcPeachActivitiCache.setUserRemind(userId, currentDate, "1");
			}
			String pMerbillNo = user.getpMerBillNo();
			Integer realAuthRemind = HcPeachActivitiCache.getUserRemind(userId, currentDate, "2");
			if (goldPechIsRemind && (StringUtil.isBlank(cardImg)||StringUtil.isBlank(pMerbillNo)) && realAuthRemind == 0) {
				// "金桃朵朵" 未认证提醒
				request.setAttribute("realAuth", "true");
				HcPeachActivitiCache.setUserRemind(userId, currentDate, "2");
			}
			Integer isAuthIpsRemind = HcPeachActivitiCache.getUserRemind(userId, currentDate, "3");
			if (goldPechIsRemind && (!StringUtil.isBlank(cardImg) && !StringUtil.isBlank(pMerbillNo)) && isAuthIps != 1 && isAuthIpsRemind == 0) {
				// "金桃朵朵" 认证未授权提醒
				request.setAttribute("isAuthIps", "true");
				HcPeachActivitiCache.setUserRemind(userId, currentDate, "3");
			}
			
			if (cardImgAudit != null) {
				String auditStr = "";
				// 审核通过
				Integer auditRemindPass = HcPeachActivitiCache.getUserAuditRemind(userId,1);
				if (cardImgAudit.getCardImgState() == 1 && auditRemindPass == 1) {
					auditStr = "pass";
					HcPeachActivitiCache.setUserAuditRemind(userId,1,"0");  // 将审核通过设置为0（已提醒）
				}
				// 审核未通过
				Integer auditRemindNoPass = HcPeachActivitiCache.getUserAuditRemind(userId,2);
				if (cardImgAudit.getCardImgState() == 2 && auditRemindNoPass == 1) {
					auditStr = "nopass";
					HcPeachActivitiCache.setUserAuditRemind(userId,2,"0");  // 将审核未通过设置为0（已提醒）
					HcPeachActivitiCache.setUserAuditRemind(userId, 1,"1");
					request.setAttribute("noPassRemark", cardImgAudit.getCardImgRemark());
				}
				request.setAttribute("auditReminds", auditStr);
			}
		}
		climbActivityLoginTip(userId, request);  // 登顶活动
		birthdayMessageReminds(user, request); // 生日礼包及消息推送
		return "/WEB-INF/views/hc9/member/memberCenter";
	}

	/** 爬山活动相关奖品提示 */
	private void climbActivityLoginTip(Long userId, HttpServletRequest request) {
		String key = "STR:HC9:CLIMB:TOP:LOGIN:TIP:" + userId;
		String value = RedisHelper.get(key);
		if(StringUtil.isNotBlank(value)) {
			int result = Integer.valueOf(value);
			if(result == 10 || result == 13 || result == 14 || result == 15 || result == 16) {
				String isExistKey = "STR:HC9:CLIMB:TOP:LOGIN:TIP:FLAG:" + userId;
				if(!RedisHelper.isKeyExist(isExistKey)) {
					request.setAttribute("climbActivityResult", result);
					RedisHelper.set(isExistKey, "1");
				}
			}
		}
	}
	
	/** 5月活动、消息提醒、生日提醒 */ 
	public void birthdayMessageReminds(Userbasicsinfo user,HttpServletRequest request) {
		/** 生日提醒 */
		request.setAttribute("birthdayTipFlag", HcNewerTaskCache.isNeedBirthdayTips(user));
		
		/** 完成新手任务，赢取终极大奖 */
		request.setAttribute("newerFlag",HcNewerTaskCache.memberCenterTaskTipFlag(user.getId()));
		
		/** 用户未完成新手任务登录提醒  */
		request.setAttribute("loginRemindFlag",HcNewerTaskCache.memberCenterTaskTipFlagAfterLogin(user.getId(),request.getSession().getId()));
		
		/** 优惠券提醒 */
		request.getSession().setAttribute("newCouponTipFlag", HcNewerTaskCache.isNewCouponInCome(user.getId()));

		/** 消息提醒 */
		Integer unReadNum = userbasicsinfoService.queryUnReadMsgNum(user.getId().toString());
		request.setAttribute("unReadNum", unReadNum);
	}
	
	/** 余额还款明细 */
	@ResponseBody
	@CheckLoginOnMethod
	@RequestMapping("/queryRepayments.htm")
	public String queryRepayments(HttpServletRequest request, int type) {
		// 取到登录用户sesssion
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		user = userbasicsinfoService.queryUserById(user.getId());
		return JsonUtil.toJsonStr(memberCenterService.repaymentBackListByType(user.getId(),type,1));
	}

	/** 余额查询 */
	@ResponseBody
	@CheckLoginOnMethod
	@RequestMapping("/queryMoneyOfUser.htm")
	public String queryMoneyOfUser(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
		Map<String, String> map = new HashMap<String, String>();
		if(user != null) {
			Double cashBalance = user.getUserfundinfo().getCashBalance();
			if (user.getIsAuthIps()!=null && user.getIsAuthIps() == 1) { // 如果有授权
				//如果有充值记录
				try {
					crs crs = baoFuService.getCasbalance(user.getpMerBillNo());
					cashBalance = crs.getBalance();
					user = userbasicsinfoService.queryUserById(user.getId());
					user.getUserfundinfo().setCashBalance(cashBalance);
					user.getUserfundinfo().setOperationMoney(cashBalance);
					userbasicsinfoService.update(user);
					request.getSession().setAttribute("session_user", user);
					Map<String,Object> totalAssets = getComData(request, true);
					map.put("code", "1");
					map.put("allMoney", totalAssets.get("allMoney").toString());
					map.put("expectMoney", totalAssets.get("expectMoney").toString());
					map.put("expectIncome", totalAssets.get("expectIncome").toString());
					map.put("expectBonus", totalAssets.get("expectBonus").toString());
					map.put("cashBalance", cashBalance.toString());
				} catch(Exception e) {
					map.put("code", "-1");
					LOG.error("查询宝付余额失败！", e);
				}
			}
		} else {
			map.put("code", "0");
		}		
		return JsonUtil.toJsonStr(map);
	}
	
	/**
	 * 跳转到交易记录
	 * */
	@CheckLoginOnMethod
	@RequestMapping("/toMoneyRecord.htm")
	public String toMoneyWater(HttpServletRequest request) {
		return "WEB-INF/views/hc9/member/moneyRecord";
	}

	/**
	 * 跳转到账户设置（个人资料设置）
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/selfInfo.htm")
	@CheckLoginOnMethod
	public String selfInfo(HttpServletRequest request) {
		// 查询用户基本信息
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		Userbasicsinfo userbasic = userbasicsinfoService.queryUserById(user
				.getId());
		request.setAttribute("userbasic", userbasic);
		request.getSession().setAttribute("csrf",
				CSRFTokenManager.getTokenForSession(request.getSession()));
		return "/WEB-INF/views/hc9/member/selfInfo";
	}

	/**
	 * 跳转到我的投资
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/myLoanRecord.htm")
	public String myLoanRecord(HttpServletRequest request) {

		return "/WEB-INF/views/hc9/member/myLoanRecord";
	}

	/**
	 * 跳转到我的融资
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/myFinanceRecord.htm")
	public String myFinanceRecord(HttpServletRequest request) {
		Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		userbasic = userbasicsinfoService.queryUserById(userbasic.getId());
		Double credit = borrowService.getBorrowCredit(userbasic.getId());
		Double sum_money = loanSignService.queryFinancSumByUser(userbasic
				.getId());
		request.setAttribute("borrow_base",
				borrowService.getBorrowService(userbasic));
		request.setAttribute("credit", credit);
		request.setAttribute("invest_mon", sum_money);
		request.setAttribute("surplus_mon", credit - sum_money);
		return "/WEB-INF/views/hc9/member/myFinanceRecord";
	}

	/**
	 * 跳转到好友推荐
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/friendRecommend.htm")
	public String friendRecommend(HttpServletRequest request) {
		Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		userbasic = userbasicsinfoService.queryUserById(userbasic.getId());

		String promoteNo = "";
		int flag = userbasic.getUserType();
		if (flag == 2) {
			promoteNo = userbasic.getStaffNo();
			
		} else {
			promoteNo = userbasic.getId().toString();
		}
		request.getSession().setAttribute("session_user", userbasic);
		getComData(request, true);   // 查询个人中心资金信息
		request.getSession().setAttribute(
				"promoteLikn",
				GenerateLinkUtils.getServiceHostnew(request)
						+ "visitor/to-regist?member=" + promoteNo);
		request.setAttribute("promoteNo", promoteNo);
		return "/WEB-INF/views/hc9/member/friendRecommend";
	}

	/**
	 * 跳转到推广奖励
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/promoteReward.htm")
	public String promotionReward(HttpServletRequest request) {
		Double bonuses = generaLizeService.getstayBonuses(request);
		Double paidBonuses = generaLizeService.getpaidBonuses(request);
		request.setAttribute("stayBonuses", bonuses);
		request.setAttribute("paidBonuses", paidBonuses);
		return "/WEB-INF/views/hc9/member/promoteReward";
	}

	/**
	 * 获取用户资金信息
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, Object> getComData(HttpServletRequest request, boolean hasRecharged) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
		Map<String, Object> map = new HashMap<String, Object>();
		if(hasRecharged) {
			// 待收收益
			Object expectIncome = memberCenterService.dueRepay(user.getId());
			// 待收本金
			Object expectMoney = (memberCenterService.toMoney(user.getId()) - memberCenterService.backMoney(user.getId()));
			// 待收佣金
			Object expectBonus = memberCenterService.toBonus(user.getId());
			// 历史收益
			Object hostIncome = memberCenterService.hostIncome(user.getId());
			// 待回款条数
			Object backCount = memberCenterService.backCount(user.getId());
			// 累计投资收益
			Object loanCount = memberCenterService.loanCount(user.getId());
			map.put("expectIncome", expectIncome);
			map.put("expectMoney", expectMoney);
			map.put("expectBonus", expectBonus);
			map.put("hostIncome", hostIncome);
			map.put("backCount", backCount);
			map.put("loanCount", loanCount);
			Double allMoney = Double.parseDouble(expectIncome.toString())
					+ Double.parseDouble(expectMoney.toString())
					+ Double.parseDouble(expectBonus.toString())
					+ user.getUserfundinfo().getCashBalance();
			map.put("allMoney", allMoney);
		} else {
			map.put("expectIncome", 0.00);
			map.put("expectMoney", 0.00);
			map.put("expectBonus", 0.00);
			map.put("hostIncome", 0.00);
			map.put("backCount", 0.00);
			map.put("loanCount", 0.00);
			map.put("allMoney", 0.00);
		}
		
		request.getSession().setAttribute("comData", map);
		return map;
	}

	/********************** 针对投资者 **********/
	/**
	 * 日历投资(当月)
	 * 
	 * @param request
	 * @param year
	 * @param month
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/queryDate.htm")
	@ResponseBody
	public Map<String, List> queryDate(HttpServletRequest request, String year,
			String month) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		Calendar cal = Calendar.getInstance();
		DateFormat ft = new SimpleDateFormat("yyyy-MM");
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = "";
		if (month.length() < 2) {
			month = "0" + month;
			date = year + "-" + month;
		} else {
			date = year + "-" + month;
		}

		try {
			Date dt1 = ft.parse(date); // 前台参数
			Date dt2 = ft.parse(ft.format(cal.getTime()));
			if (dt1.getTime() < dt2.getTime()) {
				return null;
			} else if (dt1.getTime() > dt2.getTime()) {
				date = year + "-" + month + "-01";
			} else {
				date = format.format(cal.getTime());
			}
			// 判断用户是否有充值记录
			boolean hasRecharged = memberCenterService.hasRecharged(user);
			Map<String, List> mapList = new HashMap<String, List>();
			if(hasRecharged) {
				mapList = memberCenterService.queryDate(date, user.getId());
			} else {
				mapList.put("loan", null);
				mapList.put("borrow", null);
			}
			return mapList;

		} catch (ParseException e) {
			LOG.error("日期格式转化错误"+e);
			return null;
		}
	}

	/**
	 * 当天
	 * 
	 * @param request
	 * @param year
	 * @param month
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/nowDate.htm")
	@ResponseBody
	public Object[] nowDate(HttpServletRequest request, String year,
			String month, String day) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		if (day.length() < 2) {
			day = "0" + day;
		}
		if (month.length() < 2) {
			month = "0" + month;
		}
		// 判断用户是否有充值记录
		boolean hasRecharged = memberCenterService.hasRecharged(user);
		Object[] obj = new Object[2];
		if(hasRecharged) {
			String date = year + "-" + month + "-" + day;
			String nowDate = DateFormatUtil.dateToString(new Date(), "yyyy-MM-dd");
			/** 只缓存当天的数据 */
			if(date.equals(nowDate)) {
				Long userId = user.getId();
				String key = "STR:HC9:TODO:TOTAL:NUM:OF:TODAY:" + date + ":" + userId;
				String returnKey = "STR:HC9:TODO:RETURN:NUM:OF:TODAY:" + date + ":" + userId;//待回款 0
				String payKey = "STR:HC9:TODO:PAY:NUM:OF:TODAY:" + date + ":" + userId;//待还款 1
				if(!RedisHelper.isKeyExist(key)) {
					List list = memberCenterService.nowDate(date, user.getId());
					List borrowlist = memberCenterService.nowDateBorrow(date, user.getId());
					obj[0] = list.get(0);
					obj[1] = borrowlist.get(0);
					RedisHelper.setWithExpireTime(returnKey, "" + obj[0], 24 * 60 * 60);
					RedisHelper.setWithExpireTime(payKey, "" + obj[1], 24 * 60 * 60);
					RedisHelper.setWithExpireTime(key, "1", 24 * 60 * 60);
				} else {
					obj[0] = RedisHelper.get(returnKey);
					obj[1] = RedisHelper.get(payKey);
				}
			} else {
				List list = memberCenterService.nowDate(date, user.getId());
				List borrowlist = memberCenterService.nowDateBorrow(date, user.getId());
				obj[0] = list.get(0);
				obj[1] = borrowlist.get(0);
			}
		} else {
			obj[0] = 0;
			obj[1] = 0;
		}
		return obj;
	}

	/********************** 针对投资者end **********/

	/**
	 * 跳转积分记录页面
	 * 
	 * @return
	 */
	@RequestMapping("to-creditNotes")
	@CheckLoginOnMethod
	public String toCreditNotes(HttpServletRequest request, Integer no) {
		Userbasicsinfo u = (Userbasicsinfo) request.getSession().getAttribute(
				"session_user");
		u = userbasicsinfoService.queryUserById(u.getId());
		request.setAttribute("user", u);
		return "WEB-INF/views/member/toCreditNotes";
	}

	/**
	 * 跳转-我的优惠券
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/favorable.htm")
	public String redenvelope(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		Long userId = user.getId();
		String couponFlag = HcNewerTaskCache.isNewCouponInCome(userId);
		if ("1".equals(couponFlag)) {
			HcNewerTaskCache.updateCouponTipFlag(userId);
			request.getSession().removeAttribute("newCouponTipFlag");
		}
		return "/WEB-INF/views/hc9/member/favorable";
	}
	
	/**
	 * 我的红包
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/packet_list")
	public String redenvelopeList(HttpServletRequest request,Integer no) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page.setNumPerPage(15);
		page = redEnvelopeDetailService.getRedEnvelopeList(page, user.getId());
		request.setAttribute("page", page);
		return "/WEB-INF/views/hc9/member/packet_list";
	}
	
	/**
	 * 我的加息券
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/coupon_list")
	public String couponList(HttpServletRequest request,Integer no) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page.setNumPerPage(15);
		page = redEnvelopeDetailService.getCouponList(page, user.getId());
		request.setAttribute("page", page);
		return "/WEB-INF/views/hc9/member/coupon_list";
	}
	
	/**
	 * 我的提现券
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/deposit_list")
	public String depositList(HttpServletRequest request,Integer no) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page.setNumPerPage(15);
		page = redEnvelopeDetailService.getDepositList(page, user.getId());
		request.setAttribute("page", page);
		return "/WEB-INF/views/hc9/member/deposit_list";
	}
	
	/**
	 * 跳转-我的嗒嗒车票
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/tapTapBus.htm")
	public String tapTapBus(HttpServletRequest request) {
		return "/WEB-INF/views/hc9/member/tapTapBus";
	}
	
	/**
	 * 我的嗒嗒车票
	 * @param status 0、已过期1、可使用
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/tapbus_list")
	public String tapbusList(HttpServletRequest request,Integer no,String status) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page.setNumPerPage(8);
		if (StringUtil.isBlank(status)) {
			status = "1";
		}
		page = redEnvelopeDetailService.getTapBusList(page, user.getId(),status);
		request.setAttribute("page", page);
		request.setAttribute("status", status);
		return "/WEB-INF/views/hc9/member/tapbus_list";
	}
	
	/**
	 * 跳转-我的中奖记录
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/monkeyRecord.htm")
	public String monkeyRecord(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		user = userbasicsinfoService.queryUserById(user.getId());
		request.setAttribute("session_user", user);
		getComData(request, true);   // 查询个人中心资金信息
		/*try {
			CardImgAudit cardImgAudit = cardImgAuditService.getCardImgAudit(user.getId());
			if (HcPeachActivitiCache.validCurrentDate(DateFormatUtil.getDateFormat(user.getCreateTime())) == 0) {
				if (cardImgAudit != null) {
					if (cardImgAudit.getCardImgState() == 1) {
						request.setAttribute("showRealAuth", "");
					} else {
						request.setAttribute("showRealAuth", true);
					}
				} else {
					request.setAttribute("showRealAuth", true);
				}
			}
		} catch (ParseException e) {
			LOG.error("中奖纪录出现异常：" + e.getMessage());
		}*/
		return "/WEB-INF/views/hc9/member/monkeyRecord";
	}
	
	/**
	 * “新春猴给力”的中奖记录
	 */
	@CheckLoginOnMethod
	@RequestMapping("/monkey_list")
	public String tapbusList(HttpServletRequest request,Integer no) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page = activityService.queryNewYearMonkeyRecord(page,user.getId());
		request.setAttribute("page", page);
		return "/WEB-INF/views/hc9/member/monkey_list";
	}
	
	/*** 领取生日礼包 */
	@RequestMapping(value="/receiveBirthday.htm", method = RequestMethod.POST)
	@ResponseBody
	public String receiveBirthday(HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if (user != null) {
			resultMap = month05NewerTaskActivityService.birthdayGiftReceive(user.getId());
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/**
	 * 跳转-我的消息箱
	 * 
	 * @return
	 */
	@CheckLoginOnMethod
	@RequestMapping("/messageBox.htm")
	public String message(HttpServletRequest request) {
		return "/WEB-INF/views/hc9/member/messageBox";
	}
	
	/**
	 * 消息推送列表
	 * @param request
	 * @param no
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/messageList.htm")
	@ResponseBody
	public String messageList(HttpServletRequest request, Integer no)
			throws IOException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		if (user == null){
			resultMap.put("code", "-1");
		} else {
			String userId = user.getId().toString();
			PageModel page = new PageModel();
			if (no == null) {
				page.setPageNum(1);
			} else {
				page.setPageNum(no);
			}
			page.setNumPerPage(10);
			page = userbasicsinfoService.queryMessageList(userId, page);
			resultMap.put("code", "0");
			resultMap.put("msgList", page.getList());
		}
		String jsonStr = JsonUtil.toJsonStr(resultMap); 
		return jsonStr;
	}
	
	/**
	 * 通过消息id修改消息读取状态
	 * @param request
	 * @param no
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/updateReadStatus.htm")
	@ResponseBody
	public String updateReadStatus(HttpServletRequest request, String m_id)
			throws IOException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		if (user == null){
			resultMap.put("code", "-1");
		} else {
			String userId = user.getId().toString();
			if(userbasicsinfoService.updateMsgReadStatusById(userId, m_id) > 0){
				resultMap.put("code", "0");
			}
		}
		String jsonStr = JsonUtil.toJsonStr(resultMap); 
		return jsonStr;
	}
	
	@RequestMapping("/updateReadStatusAll.htm")
	@ResponseBody
	public String updateReadStatusAll(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute("session_user");
		if (user == null){
			resultMap.put("code", "-1");
		} else {
			String userId = user.getId().toString();
			if(userbasicsinfoService.updateAllMsgReadStatus(userId) > 0){
				resultMap.put("code", "0");
			}
		}
		String jsonStr = JsonUtil.toJsonStr(resultMap); 
		return jsonStr;
	}
	
	@RequestMapping("/updMsgStatus.htm")
	@ResponseBody
	public String forwardInvestPage(String msgId) {
		Long id = Long.valueOf(msgId);
		memberCenterService.useHongBao(id);
		return "0";
	}
	/***** end new *******/
}
