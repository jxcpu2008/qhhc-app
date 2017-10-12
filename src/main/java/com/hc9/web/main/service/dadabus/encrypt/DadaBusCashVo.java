package com.hc9.web.main.service.dadabus.encrypt;

/** 嗒嗒巴士返回现金券对象 */
public class DadaBusCashVo {
	
	/** 优惠券编码，唯一 */
	private String coupon_code;
	
	/** 优惠券金额，单位分 */
	private Long price;
	
	/** 优惠券起始时间，时间戳 */
	private String start_time;
	
	/** 优惠券截止时间，时间戳 */
	private String end_time;

	public String getCoupon_code() {
		return coupon_code;
	}

	public void setCoupon_code(String coupon_code) {
		this.coupon_code = coupon_code;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
}