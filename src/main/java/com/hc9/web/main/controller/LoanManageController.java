package com.hc9.web.main.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hc9.web.main.common.annotation.CheckLogin;
import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.RepaymentRecordDetail;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.IRepaymentService;
import com.hc9.web.main.service.LoanManageService;
import com.hc9.web.main.service.LoanSignRepayMentRecordService;
import com.hc9.web.main.service.LoanSignService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.vo.RepaymentRequest;

/** 借款人的借款标管理 */
@Controller
@CheckLogin(value = CheckLogin.WEB)
@RequestMapping("/loanManage")
public class LoanManageController {
	
	private static final Logger logger = Logger.getLogger(LoanManageController.class);
	
	@Resource
	private LoanSignService loanSignService;
	
	@Resource
	private LoanSignRepayMentRecordService repayMentRecordService;
	
	@Resource
	private CacheManagerService cacheManagerService;
	
	@Resource
	private LoanManageService loanManageService;
	
	@Autowired
	private IRepaymentService repayService;
	
	/** 项目分批还款和分红 */
	@CheckLoginOnMethod
	@RequestMapping("shareLoanRepayMent.htm")
	public String shareLoanRepayMent(Long repayId, Double repayMoney, HttpServletRequest request){
		LOG.error("开始进行" + repayId + "的还款操作！");
		Repaymentrecord repaymentrecord = loanSignService.getRepaymentId(repayId);
		String result = repayMentRecordService.shareLoanRepayMent(request, repayId, repayMoney);
		Long loanId = repaymentrecord.getLoansign().getId();
		/** 还款后更新redis缓存相关信息 */
		repayMentRecordService.updateUserRedisDataAfterRepayMent(loanId, repayId);
		//投资更新标详情信息
		cacheManagerService.updateLoanDetailRelCache("" + loanId);
		loanSignService.getLoanLoandynamic();
		return result;
	}
	
	@CheckLoginOnMethod
	@RequestMapping("repay.htm")
	public String repay(HttpServletRequest request, RepaymentRequest repaymentRequest) {
		// 获取登陆用户信息
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
		repaymentRequest.setLoginUser(user);
		
		// 费用比例记录
		Costratio costratio = loanSignService.queryCostratio();
		repaymentRequest.setDefaultFeeConfig(costratio);
		
		// 获取标的对象
		Loansign loan = loanSignService.getLoansignById(String.valueOf(repaymentRequest.getLoanId()));
		repaymentRequest.setLoan(loan);
		
		List<Repaymentrecord> repayRecords = loanManageService.getRepayRecords(repaymentRequest.getLoanId());
		List<Loanrecord> investRecords = loanManageService.getLoanRecordList(repaymentRequest.getLoanId());
		List<RepaymentRecordDetail> repaymentRecordDetails = loanManageService.getRepaymentRecordDetails(repaymentRequest.getRepayRecordId());
		
		int loanType = repaymentRequest.getLoanType();
		String rtnPage = "WEB-INF/views/repaySuccess";
		String applyRtnPage = "WEB-INF/views/applyInadvanceRepaySuccess";
		JSONObject json = null;
		try {
			int overdueDays = repaymentRequest.getOverdueDays();
			String repayFlag = repaymentRequest.getRepayFlag();
			
			if (loanType == 2) {	// 月标
				if (repayFlag.equals("applyRepay")) {	// 多月标提前还款
					repaymentRequest.setRepayAction("inadvanceRepay");
					repayService.applyInAdvanceRepay(repaymentRequest, repayRecords, investRecords);
					return applyRtnPage;
				} else if (repayFlag.equals("repay")) {
					if (overdueDays > 0) {	// 逾期还款
						repaymentRequest.setRepayAction("overdueRepay");
						json = repayService.overdueRepay(overdueDays, repaymentRequest, repaymentRecordDetails);
					} else {	// 正常还款
						repaymentRequest.setRepayAction("repay");
						json = repayService.repay(repaymentRequest, repaymentRecordDetails);
					}
				} else {
					logger.warn("还款标志值为[" + repayFlag + "]不符合系统规范，取值异常，请检查！");
					rtnPage = "WEB-INF/views/repayFail";
				}
			} else if (loanType == 3) {	// 天标
				if (overdueDays < 0) {	// 提前还款
					repaymentRequest.setRepayAction("inadvanceRepay");
					repayService.applyInAdvanceRepay(repaymentRequest, repaymentRecordDetails);
					return applyRtnPage;
				} else if (overdueDays == 0) {	// 正常还款
					repaymentRequest.setRepayAction("repay");
					json = repayService.repay(repaymentRequest, repaymentRecordDetails);
				} else {	// 逾期还款
					repaymentRequest.setRepayAction("overdueRepay");
					json = repayService.overdueRepay(overdueDays, repaymentRequest, repaymentRecordDetails);
				}
			} else {
				logger.warn("标的类型值为[" + loanType + "]不符合系统规范，取值异常，请检查！");
				rtnPage = "WEB-INF/views/repayFail";
			}

			if (json.getInt("code") == 100) {
				request.setAttribute("loanName", repaymentRequest.getLoanName());
				request.setAttribute("repayPeriod", repaymentRequest.getRepayPeriod());
				request.setAttribute("repayAmount", repaymentRequest.getRepayAmount());
			} else {
				request.setAttribute("errorMsg", json.getString("msg"));
				rtnPage = "WEB-INF/views/repayFail";
			}
		} catch (Exception e) {
			rtnPage = "WEB-INF/views/repayFail";
		}
		
		return rtnPage;
	}
}