package com.hc9.web.main.vo;

/** 开通宝付账户相关 */
public class BaoFuAccountVo {
	/** 手机号，快速注册时候的宝付帐号 */
	private String bf_account;
	
	/** 用户真实姓名 */
	private String name;
	
	/** 用户身份证号 */
	private String id_card;
	
	/** 用户编号(唯一)   BIGINT类型 <= 20位 */
	private String user_id;
	
	/** 绑定成功回调地址，接收返回结果 */
	private String return_url;
	
	/** 页面自动跳转地址(不携带绑定结果信息) */
	private String page_url;

	public String getBf_account() {
		return bf_account;
	}

	public void setBf_account(String bf_account) {
		this.bf_account = bf_account;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId_card() {
		return id_card;
	}

	public void setId_card(String id_card) {
		this.id_card = id_card;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getReturn_url() {
		return return_url;
	}

	public void setReturn_url(String return_url) {
		this.return_url = return_url;
	}

	public String getPage_url() {
		return page_url;
	}

	public void setPage_url(String page_url) {
		this.page_url = page_url;
	}
}