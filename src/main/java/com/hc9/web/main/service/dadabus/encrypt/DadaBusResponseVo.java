package com.hc9.web.main.service.dadabus.encrypt;

/** 嗒嗒巴士代金券券发放接口返回对象 */
public class DadaBusResponseVo {
	private String ret;
	
	private String msg;
	
	DadaBusCashVo data;

	public String getRet() {
		return ret;
	}

	public void setRet(String ret) {
		this.ret = ret;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public DadaBusCashVo getData() {
		return data;
	}

	public void setData(DadaBusCashVo data) {
		this.data = data;
	}
	
}
