package com.hc9.web.main.vo.sms;

public class MO_PACK
{
	/**上行时间*/
	private String strMoTime; 
	/**上行手机号*/
	private String strMobile; 
	/**上行通道号*/
	private String strSpNumber; 
	/**上行扩展子号*/
	private String strExNo; 
	/**预留字段*/
	private String strReserve;  
	 /**上行内容*/
	private String strMessage; 
	
	
	public String getStrMoTime()
	{
		return strMoTime;
	}
	public void setStrMoTime(String strMoTime)
	{
		this.strMoTime = strMoTime;
	}
	public String getStrMobile()
	{
		return strMobile;
	}
	public void setStrMobile(String strMobile)
	{
		this.strMobile = strMobile;
	}
	public String getStrSpNumber()
	{
		return strSpNumber;
	}
	public void setStrSpNumber(String strSpNumber)
	{
		this.strSpNumber = strSpNumber;
	}
	public String getStrExNo()
	{
		return strExNo;
	}
	public void setStrExNo(String strExNo)
	{
		this.strExNo = strExNo;
	}
	public String getStrReserve()
	{
		return strReserve;
	}
	public void setStrReserve(String strReserve)
	{
		this.strReserve = strReserve;
	}
	public String getStrMessage()
	{
		return strMessage;
	}
	public void setStrMessage(String strMessage)
	{
		this.strMessage = strMessage;
	}
	
	
}
