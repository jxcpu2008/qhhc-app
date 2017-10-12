package com.hc9.web.main.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/** 嗒嗒巴士代金券信息表 */
@Entity
@Table(name = "dadabuscashcertificate")
public class DadaBusCashCertificate implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	/** 主键id，自增 */
	private Long id;
	
	/** 用户id */
	private Long userId;
	
	/** 标id */
	private Long loanId;
	
	/** 投资记录id */
	private Long loanRecordId;
	
	/** 发送给嗒嗒巴士的订单号 */
	private String orderNo;
	
	/** 金额 */
	private Double money;
	
	/** 手机号 */
	private String mobilePhone;
	
	/** 生效开始时间 */
	private String startTime;
	
	/** 生效结束时间 */
	private String endTime;
	
	/** 优惠券编码 */
	private String couponCode;
	
	/** 优惠券价格（分） */
	private Long price;
	
	/** 状态：1、已发放，待嗒嗒巴士确认；3、嗒嗒巴士确认成功； */
	private String status;
	
	/** 发放时间 */
	private String createTime;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "userId", length = 20)
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "loanId", length = 20)
	public Long getLoanId() {
		return loanId;
	}

	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}

	@Column(name = "loanRecordId", length = 20)
	public Long getLoanRecordId() {
		return loanRecordId;
	}

	public void setLoanRecordId(Long loanRecordId) {
		this.loanRecordId = loanRecordId;
	}

	@Column(name = "orderNo", length = 20)
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	@Column(name = "money", precision = 10, scale = 2)
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	@Column(name = "mobilePhone", length = 32)
	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	@Column(name = "startTime", length = 32)
	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	@Column(name = "endTime", length = 32)
	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	@Column(name = "couponCode", length = 32)
	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	@Column(name = "price", length = 20)
	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	@Column(name = "status", length = 1)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "createTime", length = 32)
	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}