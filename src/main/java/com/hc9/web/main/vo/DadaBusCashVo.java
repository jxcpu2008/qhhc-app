package com.hc9.web.main.vo;
 
/** 嗒嗒巴士代金券值对象 */
public class DadaBusCashVo {
	/** 订单号 */
	private String orderNo;
	
	/** 手机号码 */
	private String mobilePhone;
	
	/** 开始生效时间 */
	private String startTime;
	
	/** 生效截止时间 */
	private String endTime;
	
	/** 代金券编码 */
	private String couponCode;
	
	/** 代金券面额 */
	private Double money;
	
	/** 发放状态 */
	private String status;
	
	/** 投资人姓名 */
	private String investName;
	
	/** 项目名称 */
	private String loanSignName;
	
	/** 该笔投资金额 */
	private Double tenderMoney;
	
	/** 投资时间 */
	private String tenderTime;
	
	/** 注册时间 */
	private String registerTime;
	
	/** 渠道来源 */
	private String channelSource;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInvestName() {
		return investName;
	}

	public void setInvestName(String investName) {
		this.investName = investName;
	}

	public String getLoanSignName() {
		return loanSignName;
	}

	public void setLoanSignName(String loanSignName) {
		this.loanSignName = loanSignName;
	}

	public Double getTenderMoney() {
		return tenderMoney;
	}

	public void setTenderMoney(Double tenderMoney) {
		this.tenderMoney = tenderMoney;
	}

	public String getTenderTime() {
		return tenderTime;
	}

	public void setTenderTime(String tenderTime) {
		this.tenderTime = tenderTime;
	}

	public String getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(String registerTime) {
		this.registerTime = registerTime;
	}

	public String getChannelSource() {
		return channelSource;
	}

	public void setChannelSource(String channelSource) {
		this.channelSource = channelSource;
	}
}