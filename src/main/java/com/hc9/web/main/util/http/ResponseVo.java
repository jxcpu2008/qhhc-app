package com.hc9.web.main.util.http;

/** kft请求返回值对象 */
public class ResponseVo {
	/** 请求编号 */
	private String reqNo;

	/** 订单编号 */
	private String orderNo;

	/** 银行卡校验的结果:0 表示失贤; 1 表示成功. */
	private String status;

	/** 失败详情 */
	private String failureDetails;

	/** 错误码 */
	private String errorCode;

	/** 银行返回时间 */
	private String bankReturnTime;

	public String getReqNo() {
		return reqNo;
	}

	public void setReqNo(String reqNo) {
		this.reqNo = reqNo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFailureDetails() {
		return failureDetails;
	}

	public void setFailureDetails(String failureDetails) {
		this.failureDetails = failureDetails;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getBankReturnTime() {
		return bankReturnTime;
	}

	public void setBankReturnTime(String bankReturnTime) {
		this.bankReturnTime = bankReturnTime;
	}
}