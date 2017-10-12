package com.hc9.web.main.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.vo.pay.ExpensesInfo;

/** 对还款信息资金进行封装(针对借款人) */
@Service
public class BorrowerFundService {

	@Resource
	private LoanManageService loanManageService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	/**
	 * 计算得到借款人还款的本金、利息、违约金
	 * 
	 * @param repaymentInfo
	 *            还款对象
	 * @return 返回资金对象
	 */
	public ExpensesInfo getBorrowerFund(Repaymentrecord repaymentInfo,
			int isPrivilege) {
		return this.getMonthlyInterest(repaymentInfo, isPrivilege);
	}

	/**
	 * 每月付息到期还本
	 * 
	 * @param repaymentInfo
	 *            还款对象
	 * @return 返回资金对象
	 */
	public ExpensesInfo getMonthlyInterest(Repaymentrecord repaymentInfo,
			int isPrivilege) {
		ExpensesInfo expensesInfo = new ExpensesInfo();
//		 年利率
//		Double interestRate = repaymentInfo.getLoansign().getPrioAwordRate() + repaymentInfo.getLoansign().getPrioAwordRate();
		// 违约金
		Double penalty = 0.00;
		// 当前距离还款日期的天数
		int timeNum = 0;
		try {
			timeNum = DateUtils.differenceDate("yyyy-MM-dd", DateUtils.format("yyyy-MM-dd"), repaymentInfo.getPreRepayDate());
			// 日期格式，放款时间，当前日期
			if (timeNum < 0) { // 逾期还款
				// 逾期违约的金额
				penalty = loanManageService.overdueRepayment(repaymentInfo .getLoansign().getIssueLoan(), Math.abs(timeNum));
				expensesInfo.setState(Constant.STATUES_FOUR);
			}else if(timeNum >0){
				expensesInfo.setState(Constant.STATUES_FIVE);
			}else { // 按时还款
				expensesInfo.setState(Constant.STATUES_TWO);
			}
			expensesInfo.setInterest(repaymentInfo.getPreRepayMoney());
			expensesInfo.setMoney(repaymentInfo.getMoney());
			expensesInfo.setIpsNumber(repaymentInfo.getLoansign() .getUserbasicsinfo().getUserfundinfo().getpIdentNo());
			expensesInfo.setLoanid(repaymentInfo.getLoansign().getId());
			expensesInfo.setManagement(0.00);
			expensesInfo.setPenalty(penalty);
			expensesInfo.setUserId(repaymentInfo.getLoansign() .getUserbasicsinfo().getId());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return expensesInfo;
	}
}
