package com.hc9.web.main.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.annotation.CheckLogin;
import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.RedEnvelopeDetail;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.IndexDataCache;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.redis.activity.year2016.month01.HcMonkeyActivitiCache;
import com.hc9.web.main.redis.sys.vo.LoansignVo;
import com.hc9.web.main.redis.sys.web.WebCacheManagerUtil;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.ContractService;
import com.hc9.web.main.service.LoanManageService;
import com.hc9.web.main.service.LoanSignService;
import com.hc9.web.main.service.RedEnvelopeDetailService;
import com.hc9.web.main.service.RepayMentServices;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.activity.ActivityCommonService;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.FileUtil;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StatisticsUtil;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoanContract;
import com.hc9.web.main.vo.PageModel;
import com.hc9.web.main.vo.RepaymentRecord;

/**
 * 获取标的详细信息
 * 
 * @author RanQiBing 2014-04-11
 * 
 */
@Controller
@CheckLogin(value = CheckLogin.WEB)
@RequestMapping("/loaninfo")
public class LoanInfoController {
	
	private static final Logger logger = Logger.getLogger(LoanInfoController.class);

	@Resource
	private LoanManageService loanManageService;

	@Resource
	private LoanSignService loanSignService;

	@Resource
	private RepayMentServices repayMentServices;

	@Resource
	private UserbasicsinfoService userinfoService;

	@Resource
	private ContractService contractService;
	
	@Resource
	private RedEnvelopeDetailService redEnvelopeDetailService;

	@Resource
	private CacheManagerService cacheManagerService;
	
	@Resource
    private ActivityCommonService activityCommonService;
	
	/** 融资管理-记录 */
	@RequestMapping("/getLoanSignList.htm")
	public String getLoanSignList(HttpServletRequest request, Integer no,
			String loanName, String beginTime, String endTime, Integer state,
			Integer search) {
		PageModel page = new PageModel();
		if (null == no) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page = loanSignService.queryLoansignList(request, page, loanName,
				beginTime, endTime, search, state);
		List<Object[]> loanObject = new ArrayList<Object[]>();
		for (int i = 0; i < page.getList().size(); i++) {
			Object[] loan = (Object[]) page.getList().get(i);
			Object[] ob = new Object[7];
			ob[0] = loan[0]; // ID
			ob[1] = loan[1]; // 项目名称
			ob[2] = loan[2]; // 项目状态
			ob[3] = loan[3]; // 总额
			ob[4] = loan[4]; // 发布时间
			ob[5] = loan[5]; // 应还金额
			ob[6] = loan[6]; // 融资金额
			loanObject.add(ob);
		}
		page.setList(loanObject);
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/loan/loanManageList";
	}

	/**
	 * 还款列表（清单）
	 * 
	 * @param request
	 * @param loanId
	 *            标的ID
	 * @return
	 */
	@RequestMapping("repaymentList.htm")
	public String repaymentList(HttpServletRequest request, Integer no,
			String loanName, String beginTime, String endTime, Integer search) {
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page = repayMentServices.repaymentList(request, page, loanName,
				beginTime, endTime, search);
		List<Object[]> probases2 = new ArrayList<Object[]>();
		for (int i = 0; i < page.getList().size(); i++) {
			Object[] rey = (Object[]) page.getList().get(i);
			Object[] ob = new Object[10];
			ob[0] = rey[0]; // ID
			ob[1] = rey[1]; // 项目名
			ob[2] = rey[2]; // 预计还款日期
			ob[3] = rey[3]; // 期数
			ob[4] = rey[4]; // 应还金额
			ob[5] = rey[5]; // 还款状态
			ob[6] = rey[6]; // 实际还款日期
			Object obj = rey[7]; // 判断是否为逾期还款的状态
			if (obj != null) {
				// 1-逾期还款2-正常还款
				ob[7] = Integer.valueOf(obj.toString()) > 0 ? 2 : 1;
			}
			ob[8] = rey[8]; // 项目ID
			ob[9] = repayMentServices.processRepaymentId(Long.valueOf(rey[8]
					.toString())); // 表示可进行还款操作的id，以期数为顺序
			probases2.add(ob);
		}
		page.setList(probases2);
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/loan/repaymentList";
	}
	
	/**
	 * 还款列表（清单）
	 * @param request
	 * @param no
	 * @param loanName
	 * @param beginTime
	 * @param endTime
	 * @param search
	 * @return
	 */
	@RequestMapping("myRepaymentList.htm")
	public String myRepaymentList(HttpServletRequest request, Integer no, String loanName, String beginTime, String endTime, Integer search) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
		double balance = user.getUserfundinfo().getCashBalance();
		
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page = repayMentServices.myRepaymentList(request, page, loanName, beginTime, endTime, search);
		
		Costratio feeParams = loanSignService.queryCostratio();
		
		// 提前还款
		Date now = new Date();
		SimpleDateFormat mySimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar myCalendar = Calendar.getInstance();
		List<RepaymentRecord> records = new ArrayList<RepaymentRecord>();
		for (int i = 0; i < page.getList().size(); i++) {
			Object[] obj = (Object[]) page.getList().get(i);
			RepaymentRecord vo = new RepaymentRecord();
			vo.setRepayRecordId(StatisticsUtil.getLongFromBigInteger(obj[0]));
			vo.setLoanName(StatisticsUtil.getStringFromObject(obj[1]));
			
			// 借款期限，注意与还款记录的对应期数相区别
			vo.setLoanPeriods(StatisticsUtil.getIntegerFromObject(obj[2]));
			
			// 借款金额
			vo.setLoanAmount(StatisticsUtil.getDoubleFromBigdecimal((BigDecimal) obj[3]));
			
			// 标的放款时间
			String creditTime = StatisticsUtil.getStringFromObject(obj[4]);
			vo.setCreditTime(creditTime);
			
			// 标的类型
			int loanType = StatisticsUtil.getIntegerFromObject(obj[5]);
			vo.setLoanType(loanType);
			
			vo.setFeeState(StatisticsUtil.getIntegerFromObject(obj[6]));
			vo.setRefundWay(StatisticsUtil.getIntegerFromObject(obj[7]));
			
			// 允许提前还款标志
			int inadvanceRepayPermit = StatisticsUtil.getIntegerFromObject(obj[8]);
			if (inadvanceRepayPermit == 1) {
				vo.setInadvanceRepayPermit(Boolean.TRUE);
			} else {
				vo.setInadvanceRepayPermit(Boolean.FALSE);
			}
			
			// 天标提前还款允许阀值
			int inadvanceRepayPermitThreshold = StatisticsUtil.getIntegerFromObject(obj[9]);
			vo.setInadvanceRepayPermitThreshold(inadvanceRepayPermitThreshold);
			
			long loanId = StatisticsUtil.getLongFromBigInteger(obj[16]);
			vo.setLoanId(loanId);
			
			// 预计还款日期
			String repayDateStr = StatisticsUtil.getStringFromObject(obj[10]);
			vo.setPreRepayDate(repayDateStr);
			try {
				Date repayDate = mySimpleDateFormat.parse(repayDateStr);
				myCalendar.setTime(now);
				int nowDays = myCalendar.get(Calendar.DAY_OF_YEAR);
				myCalendar.setTime(repayDate);
				int repayDays = myCalendar.get(Calendar.DAY_OF_YEAR);
				int overdueDays = nowDays - repayDays;
				
				vo.setOverdueDays(overdueDays);
			} catch (ParseException e) {
				logger.error("针对标的：[" + loanId + "]的预计还款日期处理失败！", e);
			}
			
			vo.setRepayPeriod(StatisticsUtil.getIntegerFromObject(obj[11]));
			vo.setRepayAmount(StatisticsUtil.getDoubleFromBigdecimal((BigDecimal) obj[12]));
			vo.setRepayState(StatisticsUtil.getIntegerFromObject(obj[13]));
			vo.setRepayTime(StatisticsUtil.getStringFromObject(obj[14]));
			
			// 正常按时还款的笔数
			long normalCount = StatisticsUtil.getLongFromBigInteger(obj[15]);
			// 只要有一笔正常按时的还款，则表示正常还款，否则，是逾期还款
			int overdue = normalCount > 0 ? 2 : 1;
			vo.setOverdue(overdue);
			
			// 天标才需要考虑允许提前还款阀值
			if (loanType == 3) {
				try {
					// 放款日期
					Date creditDate = mySimpleDateFormat.parse(creditTime);
					// 每次都是和现在的日期进行比较
					myCalendar.setTime(now);
					int days = myCalendar.get(Calendar.DAY_OF_YEAR);
					myCalendar.setTime(creditDate);
					int creditDays = myCalendar.get(Calendar.DAY_OF_YEAR);
					if (creditDays + inadvanceRepayPermitThreshold < days) {
						vo.setEnableInstantRepay(Boolean.TRUE);
					}
					
					// 实际借款天数，用于天标提前还款计算实际利息
					int realDays = days - creditDays;
					vo.setRealDays(realDays);
				} catch (ParseException e) {
					logger.error("针对标的：[" + loanId + "]的放款时间处理失败！", e);
				}
			}
			
			// 获取可进行还款的还款记录id（项目可分多期进行还款，项目还款需要按顺序进行）
			long currentRepayRecordId = repayMentServices.processRepaymentId(Long.valueOf(loanId));
			vo.setCurrentRepayRecordId(currentRepayRecordId);
			records.add(vo);
		}
		
		page.setList(records);
		request.setAttribute("page", page);
		request.setAttribute("feeParams", feeParams);
		request.setAttribute("balance", balance);
		return "WEB-INF/views/hc9/member/loan/myRepaymentList";
	}

	/**
	 * 
	 * 还款详情数据
	 * 
	 * */
	@RequestMapping("/repayment.htm")
	@ResponseBody
	public JSONObject repayment(HttpServletRequest request, Long loanId, Long id) {
		DecimalFormat df = new DecimalFormat("0.00");
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		user = userinfoService.queryUserById(user.getId());
		Loansign ls = loanSignService.getloansign(request, loanId.toString());
		Repaymentrecord repayment = loanSignService.getrepaymentrecordByid(id);
		// 得到利率信息;
		Costratio costratio = loanSignService.queryCostratio();
		double needMoney = loanManageService.getTotalNeedRepaymentMoney(repayment, costratio);
		
		if (ls.getFeeState() == 2) {
			needMoney = Arith.add(needMoney, repayment.getCompanyPreFee());
		}
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("needMoney", Double.valueOf(df.format(needMoney))); // 还款总额
		reMap.put("name", ls.getName()); // 项目名
		reMap.put("rid", id); // 还款id
		reMap.put("issueLoan", ls.getIssueLoan()); // 项目金额
		reMap.put("refunway", ls.getRefunway()); // 还款类型
		JSONObject json = new JSONObject();
		json.element("repayment", reMap);
		return json;
	}
	
	/**
	 * 提前还款
	 * @param request
	 * @param repayAmount
	 * @param loanAmount
	 * @param loanDays
	 * @param realDays
	 * @return
	 */
	@RequestMapping("/inadvanceRepay.htm")
	@ResponseBody
	public JSONObject inadvanceRepay(double repayAmount, double loanAmount, int loanDays, int realDays) {
		DecimalFormat df = new DecimalFormat("0.00");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		// 应付利息
		double expectInterest = Arith.sub(repayAmount, loanAmount);
		returnMap.put("expectInterest", Double.valueOf(df.format(expectInterest)));
		
		// 实付利息
		double realInterest = Arith.div(Arith.mul(expectInterest, realDays), loanDays);
		returnMap.put("realInterest", Double.valueOf(df.format(realInterest)));
		
		// 实际应还款金额
		double realRepayAmount = Arith.add(loanAmount, realInterest);
		returnMap.put("realRepayAmount", Double.valueOf(df.format(realRepayAmount)));
		
		// 节省利息
		double savedInterest = Arith.sub(expectInterest, realInterest);
		returnMap.put("savedInterest", Double.valueOf(df.format(savedInterest)));
		
		JSONObject json = new JSONObject();
		json.element("repayment", returnMap);
		return json;
	}
	
	/**
	 * 提前还款
	 * @param loanId
	 * @param repayAmount
	 * @param periods
	 * @param loanPeriods
	 * @return
	 */
	@RequestMapping("/inadvanceMonthsRepay.htm")
	@ResponseBody
	public JSONObject inadvanceMonthsRepay(long loanId, double repayAmount, int periods, int loanPeriods) {
		DecimalFormat df = new DecimalFormat("0.00");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		Loansign loan = loanSignService.getLoansignById(String.valueOf(loanId));
		// 年化利率
		double interestRate = loan.getRealRate() - loan.getCompanyFee();
		// 年化利息
		double interest4Year = Arith.round(Arith.mul(loan.getIssueLoan(), interestRate), 4);
		// 每期的利息
		double interest = 0.00d;
		// 还款方式
		int refundWay = loan.getRefunway();
		if (refundWay == 1) {	// 按月付息到期还本
			interest = Arith.round(Arith.div(interest4Year, 12), 2);
		} else if (refundWay == 2) {	// 按季付息到期还本
			interest = Arith.round(Arith.div(interest4Year, 4), 2);
		} else {
		}
		
		// 实付利息
		double realInterest = Arith.mul(interest, periods);
		// 应付利息
		double expectInterest = Arith.mul(interest, loanPeriods);
		// 实付本息
		double realAmount = Arith.add(loan.getIssueLoan(), realInterest);
		// 还款总额 = 实付本息 + 额外多收一期利息
		double realRepayAmount = Arith.add(realAmount, interest);
		
		// 实际还款的本息
		returnMap.put("realAmount", Double.valueOf(df.format(realAmount)));
		// 额外利息，即罚息
		returnMap.put("penaltyInterest", Double.valueOf(df.format(interest)));
		// 实际应还款金额
		returnMap.put("realRepayAmount", Double.valueOf(df.format(realRepayAmount)));
		// 实际应还利息
		double realRepayInterest = Arith.add(realInterest, interest);
		returnMap.put("realRepayInterest", Double.valueOf(df.format(realRepayInterest)));
		
		// 节省利息
		double savedInterest = Arith.sub(Arith.sub(expectInterest, realInterest), interest);
		returnMap.put("savedInterest", Double.valueOf(df.format(savedInterest)));
		
		JSONObject json = new JSONObject();
		json.element("repayment", returnMap);
		return json;
	}
	
	/**
	 * 
	 * @param request
	 * @param repayAmount
	 * @param loanAmount
	 * @param loanDays
	 * @param realDays
	 * @return
	 */
	@RequestMapping("/overdueRepay.htm")
	@ResponseBody
	public JSONObject overdueRepay(double repayAmount, int overdueDays, double overdueRate) {
		DecimalFormat df = new DecimalFormat("0.00");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		// 罚付利息
		double penaltyAmount = Arith.mul(Arith.mul(repayAmount, overdueRate), overdueDays);
		returnMap.put("penaltyAmount", Double.valueOf(df.format(penaltyAmount)));
		
		// 实际应还款金额
		double realRepayAmount = Arith.add(repayAmount, penaltyAmount);
		returnMap.put("realRepayAmount", Double.valueOf(df.format(realRepayAmount)));
		
		JSONObject json = new JSONObject();
		json.element("repayment", returnMap);
		return json;
	}

	/**
	 * 交易记录（清单）
	 * 
	 * @param request
	 * @param loanId
	 *            标的ID
	 * @return
	 */
	@RequestMapping("/tradeRecord.htm")
	public String tradeRecord(HttpServletRequest request, String beginTime,
			String endTime, Integer search, String type, Integer no) {
		PageModel page = new PageModel();
		page.setPageNum(no == null ? 1 : no);
		repayMentServices.tradeRecord(request, page, beginTime, endTime,
				search, type);
		List<Object[]> list = new ArrayList<Object[]>();
		for (int i = 0; i < page.getList().size(); i++) {
			Object[] acc = (Object[]) page.getList().get(i);
			Object[] ob = new Object[7];
			ob[0] = acc[0];
			ob[1] = acc[1]; // 交易类型
			ob[2] = acc[2]; // 交易金额-收入
			ob[3] = acc[3]; // 余额
			ob[4] = acc[4]; // 操作时间
			ob[5] = acc[5]; // 服务费
			ob[6] = acc[6]; // 交易金额-支出
			list.add(ob);
		}
		page.setList(list);
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/trade/tradeList";
	}

	/*********** new *********/
	/**
	 * 项目众持页面
	 * 
	 * @return
	 */
	@RequestMapping("loanList.htm")
	public String projectcrowdfunding(HttpServletRequest request) {
		return "WEB-INF/views/hc9/member/loan/loanList";
	}

	/**
	 * 项目众筹列表
	 * 
	 * @param request
	 * @param loanType
	 * @param city
	 * @param money
	 * @param no
	 * @return
	 */

	@RequestMapping("getLoanList.htm")
	public String getLoanList(HttpServletRequest request, Integer type,
			Integer state, Integer month, Integer rate, Integer no) {
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}

		page.setNumPerPage(8);
		
		if(state == null && month == null && rate == null && type == null) {
			if(page.getPageNum() == 1) {
				/** 我要众持页面相关标列表 */
				PageModel redisPage = SysCacheManagerUtil.getBuyPayLoanListFromRedis();
				if(redisPage == null || redisPage.getList() == null || redisPage.getList().size() < 1) {
					page = loanManageService.getLoanList(type, state, month, rate, page);
					SysCacheManagerUtil.setBuyPayLoanListCache(page);
				} else {
					page = redisPage;
				}
			}else if(page.getPageNum() > 1){
				page = loanManageService.getLoanList(type, state, month, rate, page);
			}
		} else {
			page = loanManageService.getLoanList(type, state, month, rate, page);
		}
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/loan/loantable";
	}

	/**
	 * 众持详情页
	 * 
	 * @param request
	 * @param pId
	 * @param no
	 * @return
	 * @throws java.text.ParseException
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/loansignInfo.htm")
	public String loansignInfo(HttpServletRequest request, String loanId)
			throws java.text.ParseException {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		if (null == loanId || "".equals(loanId)) {
			return null;
		}
		if (user != null) {
			user = userinfoService.queryUserById(user.getId());
			request.getSession().setAttribute(Constant.SESSION_USER, user);
			String userCashBalance = "" + Arith.roundBigDecimal(
					new BigDecimal(Arith.mul(user.getUserfundinfo().getCashBalance(), 1.0)), 2);
			request.setAttribute("userCashBalance", userCashBalance);
		}

		// 项目融资人信息
		String borrwer = "暂无";
		/** 标缓存 */
		boolean cacheFlag = false;
		String borrwerKey = "LST:HC9:LOANSIGN:BORROWER:LOANID:" + loanId;
		LoansignVo loansignVo = WebCacheManagerUtil.getWebLoanSignDetailFromRedis(loanId);
		if(loansignVo == null) {
			Loansign loansign = loanSignService.getLoansignById(loanId);
			cacheManagerService.updateLoansignToRedis(loansign);
			loansignVo = WebCacheManagerUtil.getWebLoanSignDetailFromRedis(loanId);
			if(StringUtil.isNotBlank(loansign.getUserbasicsinfo().getStaffNo())) {
				borrwer = loansign.getUserbasicsinfo().getStaffNo();
			}
			RedisHelper.set(borrwerKey, borrwer);
		} else {
			borrwer = RedisHelper.get(borrwerKey);
			cacheFlag = true;
		}
		
		if(loansignVo == null) {
			return "error-500";
		}
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d = format.parse(loansignVo.getPublishTime().toString());
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.add(Calendar.DATE, loansignVo.getValidity());
			Date temp_date = c.getTime();
			request.setAttribute("timeDown", format.format(temp_date));
		} catch (Exception e) {
			LOG.error("众持明细页面报错！" , e);
		}
		
		// 统计购买人数
		Object investNum = null;
		// 查询众筹附件
		List attachList1 = null;
		List attachList2 = null;
		List attachList3 = null;
		List attachList4 = null;
		List attachList5 = null;
		String investNumKey = "INT:HC9:LOANSIGN:INVEST:NUM:" + loanId;
		String attachList1Key = "LST:HC9:LOANSIGN:ATTACH:LST:1:" + loanId;
		String attachList2Key = "LST:HC9:LOANSIGN:ATTACH:LST:2:" + loanId;
		String attachList3Key = "LST:HC9:LOANSIGN:ATTACH:LST:3:" + loanId;
		String attachList4Key = "LST:HC9:LOANSIGN:ATTACH:LST:4:" + loanId;
		String attachList5Key = "LST:HC9:LOANSIGN:ATTACH:LST:5:" + loanId;
		if(cacheFlag) {
			investNum = RedisHelper.get(investNumKey);
			
			// 查询众筹附件
			attachList1 = IndexDataCache.getList(attachList1Key);// 项目证明
			attachList2 = IndexDataCache.getList(attachList2Key);// 资产包证明
			attachList3 = IndexDataCache.getList(attachList3Key);// 担保证明
			attachList4 = IndexDataCache.getList(attachList4Key);// 保障证明
			attachList5 = IndexDataCache.getList(attachList5Key);// 监管资金证明
		} else {
			// 统计购买人数
			investNum = loanSignService.getTenderCount(loanId);
			// 查询众筹附件
			attachList1 = loanSignService.getAttachMent(loanId, "1");// 项目证明
			attachList2 = loanSignService.getAttachMent(loanId, "2");// 资产包证明
			attachList3 = loanSignService.getAttachMent(loanId, "3");// 担保证明
			attachList4 = loanSignService.getAttachMent(loanId, "4");// 保障证明
			attachList5 = loanSignService.getAttachMent(loanId, "5");// 监管资金证明
			RedisHelper.set(investNumKey, "" + investNum);
			IndexDataCache.set(attachList1Key, attachList1);
			IndexDataCache.set(attachList2Key, attachList2);
			IndexDataCache.set(attachList3Key, attachList3);
			IndexDataCache.set(attachList4Key, attachList4);
			IndexDataCache.set(attachList5Key, attachList5);
		}

/*		if (user != null) {
			LoansignCollected collect = loanInfoService.getColloancord(loanId,
					user.getId().toString());
			request.setAttribute("collect", collect);
		}*/
		// 一鸣惊人
		Map<String, String> maxMap = RedisHelper.hgetall("NEWYEAR:INVEST:MONEY:LOANID:MAX:"+loanId);
		// 一锤定音
		Map<String, String> lastMap = RedisHelper.hgetall("NEWYEAR:INVEST:MONEY:LOANID:LAST:"+loanId);
		Integer activSign = HcMonkeyActivitiCache.loanBeforeStillAfterSignNum(loansignVo.getPublishTime());
		request.setAttribute("attachList5", attachList5);
		request.setAttribute("maxMap", maxMap);
		request.setAttribute("lastMap", lastMap);
		request.setAttribute("activSign", activSign);
		

		request.setAttribute("loan", loansignVo);
		request.setAttribute("obj", investNum);
		request.setAttribute("borrw", borrwer);
		request.setAttribute("attachList1", attachList1);
		request.setAttribute("attachList2", attachList2);
		request.setAttribute("attachList3", attachList3);
		request.setAttribute("attachList4", attachList4);
		return "WEB-INF/views/hc9/loanInfo";
	}

	/**
	 * 投资记录
	 * 
	 * @param request
	 * @param page
	 * @param no
	 * @return
	 */
	@RequestMapping("/loanrecordList.htm")
	public String loanrecordList(HttpServletRequest request, PageModel page,
			Integer no, String loanId) {
		page.setNumPerPage(10);
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		
		boolean queryDbFlag = true;
		String loanRecordkey = "LST:HC9:LOANSIGN:DETAIL:BUY:LIST:FIRST:PAGE:" + loanId;
		if(page.getPageNum() == 1) {
			String loanrecordListStr = RedisHelper.get(loanRecordkey);
			if(StringUtil.isNotBlank(loanrecordListStr)) {
				page = JsonUtil.jsonToObject(loanrecordListStr, PageModel.class);
				queryDbFlag = false;
			}
		}
		if(queryDbFlag) {
			page = loanSignService.getLoanrecordList(Long.parseLong(loanId), page);
			if(page.getPageNum() == 1) {
				String loanrecordListStr = JsonUtil.toJsonStr(page);
				RedisHelper.set(loanRecordkey, loanrecordListStr);
			}
		}
		
		LoansignVo loansignVo = WebCacheManagerUtil.getWebLoanSignDetailFromRedis(loanId);
		if(loansignVo == null) {
			loanSignService.getLoansignById(loanId);
			loansignVo = WebCacheManagerUtil.getWebLoanSignDetailFromRedis(loanId);
		}
		request.setAttribute("page", page);
		request.setAttribute("loan", loansignVo);
		return "WEB-INF/views/hc9/loanrecord";
	}

	@RequestMapping("/checkLoan.htm")
	@ResponseBody
	public String checkLoan(HttpServletRequest request, Double proMoney,
			Double midMoney, Double afterMoney, String loanId, Long redenve_id) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		Loansign loan = loanSignService.getLoansign(loanId);
		String msg = "true";
		if (user == null) {
			return "您的登陆已超时，请重新登陆！";
		}
		if (loan.getUserbasicsinfo().getId().equals(user.getId())) {
			msg = "抱歉，你不能投自己的标";
		}
		Double proMidMoney = 0.00;// 优先跟夹层
		Double allMoney = 0.00;
		if (proMoney == null) {
			proMoney = 0.00;
		}
		if (midMoney == null) {
			midMoney = 0.00;
		}
		if (afterMoney == null) {
			afterMoney = 0.00;
		}
		proMidMoney = proMoney + midMoney;
		allMoney = proMoney + midMoney + afterMoney;

		//Integer count = redEnvelopeDetailService.isExistRedEnvelope(user.getId(),proMoney);
		// 返回余额不足
		if (allMoney > user.getUserfundinfo().getCashBalance()) {
			msg = "抱歉，您的余额不足，请先充值！";  
		}
		
		// 获取费用表的信息
		Costratio costratio = loanSignService.queryCostratio();
		// 除去夹层和劣后 -如果优先投资额   > 账户余额
		Double calc_after = user.getUserfundinfo().getCashBalance()-midMoney-afterMoney;
		if (proMoney != 0.0 && calc_after < proMoney && redenve_id != null && costratio.getRedState()==1) {
			RedEnvelopeDetail redEnvelope = redEnvelopeDetailService.getRedEnvelopeDetail(redenve_id);
			if (redEnvelope != null) {  // 如果有使用红包
				Double real_useMoney = calc_after + redEnvelope.getMoney();   // 当前可用金额 + 当前使用红包金额
				if (real_useMoney >= proMoney) {  
					msg = "true";  
				}
			}
		}
		if (loan.getLoansignType().getId() != 5) {
			if (proMidMoney > (loan.getPrioRestMoney() + loan.getMidRestMoney())) {
				msg = "抱歉，投资金额大于优先跟夹层剩余金额总和！";
			}
			if (afterMoney > loan.getAfterRestMoney()) {
				msg = "抱歉，投资金额大于劣后剩余金额！";
			}
		} else {
			if (proMidMoney > loan.getPrioRestMoney()) {
				msg = "抱歉，投资金额大于剩余金额！";
			}
		}
		return msg;
	}
	
	/**
	 * 通过投资金额获取红包和加息券信息
	 * @param request
	 * @param proMoney
	 * @return JSON
	 */
	@CheckLoginOnMethod
	@ResponseBody
	@RequestMapping("/redenvelope.htm")
	public JSONObject redenvelope(HttpServletRequest request, Long loanId, Double proMoney) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		JSONObject json = new JSONObject();
		String packet = "";
		String interest = "";
		if (proMoney != null) {
			Double maxMoney = null;
			Loansign loan = loanSignService.getLoansignById(loanId.toString());
			if(!activityCommonService.isValidForUseRed(loan)) {
				maxMoney = 5.0;
			}
			packet = redEnvelopeDetailService.getRedEnveByCon(user.getId(),proMoney, maxMoney);
			// 加息券
			interest = redEnvelopeDetailService.getInterestByUser(user.getId(),proMoney);
		}
		json.put("packet", packet);
		json.put("interest", interest);
		return json;
	}
	
	/**
	 * 更换红包记录查询
	 * @param request
	 * @param page 
	 * @param no 当前页数
	 * @param priority 优先金额
	 */
	@CheckLoginOnMethod
	@RequestMapping("/changeCoupon.htm")
	public String changeRe(HttpServletRequest request,Integer type,PageModel page,
			Integer no, Double priority, Long loanId) {
		if (no != null) {
			page.setPageNum(no);
		} else {
			page.setPageNum(1);
		}
		page.setNumPerPage(10);
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		if (type == 1) { // 红包列表
			Double maxMoney = null;
			Loansign loan = loanSignService.getLoansignById(loanId.toString());
			if(!activityCommonService.isValidForUseRed(loan)) {
				maxMoney = 5.0;
			}
			redEnvelopeDetailService.changeReList(page , user.getId(), priority, maxMoney);
		} else { // 加息券列表
			redEnvelopeDetailService.interestList(page , user.getId(),priority);
		}
		request.setAttribute("page", page);
		request.setAttribute("type", type);
		return "WEB-INF/views/hc9/member/loan/changeCoupon_list";
	}

	/*********** end new ********/

	// ****个人中心********//
	/**
	 * 我的投资-个人中心
	 * 
	 * @param request
	 * @param no
	 * @param state
	 * @param beginTime
	 * @param endTime
	 * @param timeno
	 * @return
	 */
	@RequestMapping("/getLoanMyRecord.htm")
	public String getLoanMyRecord(HttpServletRequest request, Integer no,
			Integer state, String beginTime, String endTime, Integer timeno) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		Userbasicsinfo userBase = userinfoService.queryUserById(user.getId());
		PageModel page = new PageModel();
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page = loanSignService.getloanrecord(request, page, state, beginTime,
				endTime, timeno, userBase);
		List<Object[]> probases2 = new ArrayList<Object[]>();
		for (int i = 0; i < page.getList().size(); i++) {
			Object[] obj = (Object[]) page.getList().get(i);
			String status = obj[5].toString();
			probases2.add(obj);
			if ("6".equals(status) || "7".equals(status)) { // 如果状态为还款中
				obj[12] = repayMentServices.getRepaymentShareRecord(obj[11]
						.toString());
			}
		}
		page.setList(probases2);
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/loan/loanRecordList";
	}

	/**
	 * 我的收益明细-个人中心
	 * 
	 * @param request
	 * @param page
	 * @param no
	 * @return
	 */
	@RequestMapping("/getLoanIncome.htm")
	public String getLoanIncome(HttpServletRequest request, PageModel page,
			Integer no, Integer timeno, String beginTime, String endTime) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		if (no == null) {
			page.setPageNum(1);
		} else {
			page.setPageNum(no);
		}
		page = loanSignService.getLoanIncome(page, user.getId(), beginTime,
				endTime, timeno);
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/loan/loanIncomeRecord";
	}

	/**
	 * 收益明细-查看详情还款信息
	 * 
	 */
	@RequestMapping("/repayInfo.htm")
	public String getLoanIncome(HttpServletRequest request, Long lrId) {
		request.setAttribute("count", loanSignService.notRepaymentCount(lrId));
		request.setAttribute("lr", loanSignService.getLoanRecord(lrId.toString()));
		request.setAttribute("loanrecords",
				loanSignService.findDetailBylrId(lrId));
		return "WEB-INF/views/hc9/member/loan/IncomefindDetail";
	}

	// ****个人中心 end********//
	/**
	 * 
	 * @param request
	 * @param response
	 * @param loanId
	 *            标id
	 * @param recordId
	 *            投资记录id
	 * @throws Exception
	 */
	@CheckLoginOnMethod
	@RequestMapping("loanCreditContact.htm")
	public void loanCreditContact(HttpServletRequest request,
			HttpServletResponse response, String loanId, String recordId){

		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);

		
		Map data = new HashMap();
		Loansign loansign = loanSignService.getLoansign(loanId);
		Loanrecord loanrecord = loanSignService.getLoanRecordID(recordId);
	
		// 获取环境地址
		String realPath = request.getSession().getServletContext().getRealPath("") + "/download/contract/";
		//合同文件名
		String contractName = loanrecord.getpContractNo();
		//产品类型
		long loanType=loansign.getLoansignType().getId();
		
		if(loanType==5){ //甜城项目
			contractService.setTemplate("contactDownpayTemplate");
		}else{ //普通标
			contractService.setTemplate("contactCommonTemplate");
		}
		//合同数据
		LoanContract loanContract = loanSignService.packageContactData(user,loansign,loanrecord);
		data.put("item", loanContract);
		//生成合同
		boolean hasFile=contractService.genContact(data, realPath, contractName);
		// 下载合同
		if(hasFile){
			try {
				FileUtil.downFile(realPath + "pdf/" + contractName + ".pdf",contractName + ".pdf", response);
			} catch (IOException e) {
				LOG.error("合同下载出错"+e);
			}
		}

	}


}