package com.hc9.web.main.vo;

import java.io.Serializable;

/**
 * 还款记录Value Object
 * @author Administrator
 *
 */
public class RepaymentRecord implements Serializable {

	private static final long serialVersionUID = -4182096774136960778L;

	// 还款记录主键id
	private long repayRecordId;
	
	// 当期还款记录主键id
	private long currentRepayRecordId;
	
	// 借款期限，区分标的类型，天标对应天数，月标对应期数
	private int loanPeriods;
	
	// 借款金额
	private double loanAmount;
	
	// 标的名称
	private String loanName;
	
	// 放款时间
	private String creditTime;
	
	// 标的类型
	private int loanType;
	
	// 预计还款时间
	private String preRepayDate;
	
	// 当期还款期数
	private int repayPeriod;
	
	// 还款金额
	private double repayAmount;
	
	// 还款状态
	private int repayState;
	
	// 还款时间
	private String repayTime;
	
	// 是否逾期
	private int overdue;
	
	// 标的主键id
	private long loanId;
	
	// 是否显示立即还款按钮-只针对天标，天标需要在满足允许提前还款阀值基础之上才能进行提前还款
	private boolean enableInstantRepay;
	
	private boolean inadvanceRepayPermit;
	
	private int inadvanceRepayPermitThreshold;
	
	// 实际借款天数，只对天标有效
	private int realDays;
	
	// 逾期天数
	private int overdueDays;
	
	// 收取平台服务费状态 1-收取  2-不收取
	private int feeState;
	
	// 还款方式1-按月 2-按季度 3-天标还款
	private int refundWay;

	public long getRepayRecordId() {
		return repayRecordId;
	}

	public void setRepayRecordId(long repayRecordId) {
		this.repayRecordId = repayRecordId;
	}

	public String getLoanName() {
		return loanName;
	}

	public void setLoanName(String loanName) {
		this.loanName = loanName;
	}

	public String getCreditTime() {
		return creditTime;
	}

	public void setCreditTime(String creditTime) {
		this.creditTime = creditTime;
	}

	public String getPreRepayDate() {
		return preRepayDate;
	}

	public void setPreRepayDate(String preRepayDate) {
		this.preRepayDate = preRepayDate;
	}

	public int getRepayPeriod() {
		return repayPeriod;
	}

	public void setRepayPeriod(int repayPeriod) {
		this.repayPeriod = repayPeriod;
	}

	public double getRepayAmount() {
		return repayAmount;
	}

	public void setRepayAmount(double repayAmount) {
		this.repayAmount = repayAmount;
	}

	public int getRepayState() {
		return repayState;
	}

	public void setRepayState(int repayState) {
		this.repayState = repayState;
	}

	public String getRepayTime() {
		return repayTime;
	}

	public void setRepayTime(String repayTime) {
		this.repayTime = repayTime;
	}

	public int getOverdue() {
		return overdue;
	}

	public void setOverdue(int overdue) {
		this.overdue = overdue;
	}

	public long getLoanId() {
		return loanId;
	}

	public void setLoanId(long loanId) {
		this.loanId = loanId;
	}

	public long getCurrentRepayRecordId() {
		return currentRepayRecordId;
	}

	public void setCurrentRepayRecordId(long currentRepayRecordId) {
		this.currentRepayRecordId = currentRepayRecordId;
	}

	public int getLoanType() {
		return loanType;
	}

	public void setLoanType(int loanType) {
		this.loanType = loanType;
	}

	public boolean isInadvanceRepayPermit() {
		return inadvanceRepayPermit;
	}

	public void setInadvanceRepayPermit(boolean inadvanceRepayPermit) {
		this.inadvanceRepayPermit = inadvanceRepayPermit;
	}

	public int getInadvanceRepayPermitThreshold() {
		return inadvanceRepayPermitThreshold;
	}

	public void setInadvanceRepayPermitThreshold(int inadvanceRepayPermitThreshold) {
		this.inadvanceRepayPermitThreshold = inadvanceRepayPermitThreshold;
	}

	public boolean isEnableInstantRepay() {
		return enableInstantRepay;
	}

	public void setEnableInstantRepay(boolean enableInstantRepay) {
		this.enableInstantRepay = enableInstantRepay;
	}

	public int getRealDays() {
		return realDays;
	}

	public void setRealDays(int realDays) {
		this.realDays = realDays;
	}

	public int getLoanPeriods() {
		return loanPeriods;
	}

	public void setLoanPeriods(int loanPeriods) {
		this.loanPeriods = loanPeriods;
	}

	public double getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(double loanAmount) {
		this.loanAmount = loanAmount;
	}

	public int getOverdueDays() {
		return overdueDays;
	}

	public void setOverdueDays(int overdueDays) {
		this.overdueDays = overdueDays;
	}

	public int getFeeState() {
		return feeState;
	}

	public void setFeeState(int feeState) {
		this.feeState = feeState;
	}

	public int getRefundWay() {
		return refundWay;
	}

	public void setRefundWay(int refundWay) {
		this.refundWay = refundWay;
	}
}