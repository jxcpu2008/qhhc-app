package com.hc9.web.main.service.smsmail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Validcodeinfo;
import com.hc9.web.main.service.ValidcodeInfoService;
import com.hc9.web.main.service.smsmail.wdsms.IndustryWdSms;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.FreeMarkerUtil;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.SmsMessagePojo;

import freemarker.template.TemplateException;

/**
 * 短信服务
 * 
 * @author frank
 * 
 */
@Service
public class SmsService {

	/**
	 * BaseSmsService
	 */
	@Resource
	BaseSmsService baseSmsService;
	
	@Resource
	MwTriggerSmsService mwTriggerSmsService;
	
	@Resource
	HyTriggerSmsService hyTriggerSmsService;
	
	@Resource
	IndustryWdSms industryWdSmsService;
	
	@Resource
	EmayMarketingSmsService emayMarketingSmsService;
	
	@Resource
	MwMarketingSmsService mwMarketingSmsService;

	@Resource
	ValidcodeInfoService validcodeInfoService;

	@Resource
	private HibernateSupport dao;
	/**
	 * 得到短信资源
	 * 
	 * @param modelName
	 *            资源名称
	 * @param map
	 *            待填充字符
	 * @return 填充后的短信文本
	 * @throws IOException
	 *             文件读取异常
	 * @throws TemplateException
	 *             文件解析异常
	 */
	public String getSmsResources(String modelName, Map map)
			throws IOException, TemplateException {

		return FreeMarkerUtil.execute("config/marker/sms/" + modelName,
				Constant.CHARSET_DEFAULT, map);

	}
	
	
	/**
	 * 得到短信资源 手机APP
	 * 
	 * @param modelName
	 *            资源名称
	 * @param map
	 *            待填充字符
	 * @return 填充后的短信文本
	 * @throws IOException
	 *             文件读取异常
	 * @throws TemplateException
	 *             文件解析异常
	 */
	public String getAppSmsResources(String modelName, Map map)
			throws IOException, TemplateException {
		return FreeMarkerUtil.execute("config/marker/appsms/" + modelName,
				Constant.CHARSET_DEFAULT, map);

	}
	

	/**
	 * 发送短信 支持短信群发
	 * 
	 * @param content
	 *            内容
	 * @param telNos
	 *            接收号码
	 * @return 短信发送状态[是否成功，返回值，失败信息]
	 * @throws Exception
	 *             异常
	 */
	public Integer sendSMS(String content, String... telNos) throws Exception {
		return baseSmsService.sendSMS(content, telNos);
	}

	/**
	 * 发送验证码
	 * 
	 * @param userbasicsinfo
	 *            当前操作用户信息
	 * @return 短信发送状态[是否成功，返回值，失败信息]
	 * @throws Exception
	 */
	public Integer sendCode(Userbasicsinfo userbasicsinfo,
			Validcodeinfo validcode, String urlcase, String phone)
			throws Exception {
		// 获取验证码
		String numberCode = StringUtil.getvalidcode();
		Map<String, String> map = new HashMap<String, String>();

		if (null == userbasicsinfo.getUserName()) {
			map.put("user", userbasicsinfo.getNickname());
		} else {
			map.put("user", userbasicsinfo.getUserName());
		}
		map.put("code", numberCode);
		String content = this.getSmsResources("check-code.ftl", map);

		// 判断当前操作用户是否发送过短信
		if (null != validcode) {
			if (null == validcode.getSmsCode()) {
				int sms = baseSmsService.sendSMS(content, phone);
				if (sms==0) {
					validcode.setSmsCode(numberCode);
					validcode.setSmsPhone(phone);
					validcode.setSmsagainTime(System.currentTimeMillis()
							+ Constant.MILLISECONDS);
					validcode.setSmsoverTime(Constant.MILLISECONDS);
					validcodeInfoService.update(validcode);
				}
				return sms;
			} else {
				// 再次发送短信的时间是否小于当前时间
				if (validcode.getSmsagainTime() > System.currentTimeMillis()
						+ Constant.MILLISECONDS) {
					return 1;
				} else {
					int sms;
					if (urlcase.equals("bid")) {
						sms = baseSmsService.sendSMS(content, phone);
						if (sms==0) {
							validcode.setSmsCode(numberCode);
							validcode.setSmsPhone(phone);
							validcode.setSmsagainTime(System
									.currentTimeMillis()
									+ Constant.MILLISECONDS);
							validcode.setSmsoverTime(Constant.MILLISECONDS);
							validcodeInfoService.update(validcode);
						}
					} else {
						sms = baseSmsService.sendSMS(content, userbasicsinfo
								.getUserrelationinfo().getPhone());
						if (sms==0) {
							validcode.setSmsCode(numberCode);
							validcode.setSmsPhone(userbasicsinfo
									.getUserrelationinfo().getPhone());
							validcode.setSmsagainTime(System
									.currentTimeMillis()
									+ Constant.MILLISECONDS);
							validcode.setSmsoverTime(Constant.MILLISECONDS);
							validcodeInfoService.update(validcode);
						}
					}

					return sms;
				}
			}
		} else {
			int sms = baseSmsService.sendSMS(content, userbasicsinfo
					.getUserrelationinfo().getPhone());
			if (sms==0) {
				Validcodeinfo vali = new Validcodeinfo();
				vali.setSmsCode(numberCode);
				vali.setSmsPhone(userbasicsinfo.getUserrelationinfo()
						.getPhone());
				vali.setUserbasicsinfo(userbasicsinfo);
				vali.setSmsagainTime(System.currentTimeMillis()
						+ Constant.MILLISECONDS);
				vali.setSmsoverTime(Constant.MILLISECONDS);
				dao.save(vali);
			}
			return sms;
		}
	}
	
	private void updateRemindSMSCount(SmsMessagePojo smsMessagePojo) {
		/** 记录短息提醒次数 */
		Long repaymentrecordId = smsMessagePojo.getRepaymentrecordId();
		String sql = "update repaymentrecord set remindSMSCount=remindSMSCount+1 where id=" + repaymentrecordId;
		dao.executeSql(sql);
	}
	
	private void updateRemindEmailCount(SmsMessagePojo smsMessagePojo) {
		/** 记录短息提醒次数 */
		Long repaymentrecordId = smsMessagePojo.getRepaymentrecordId();
		String sql = "update repaymentrecord set remindEmailCount=remindEmailCount+1 where id=" + repaymentrecordId;
		dao.executeSql(sql);
	}
	
	/**
	 * 选择触发短信通道
	 * @param type
	 * @param content
	 * @param phone
	 * @return
	 */
	public  Integer chooseSmsChannel(int type,String content,String phone){
		LOG.error(" 开始准备发送短信，所使用的渠道类型为： " + type);
		int res=-1;
//		long beginTime = System.currentTimeMillis();
		try {
			if(type == 1) {//亿美
				res=baseSmsService.sendSMS(content, phone);
			} else if(type == 2) {//梦网
				res=mwTriggerSmsService.sendSMSBySameMessage(phone, content);
			} else if(type == 3) {//互亿
				res=hyTriggerSmsService.sendSMS(content, phone);
			} else if(type == 4) {//沃动
				res = industryWdSmsService.sendIndustrySMS(content, phone);
			}
			
		} catch(Exception e) {
			LOG.error("给 " + phone + " 发送短信报错！", e);
		}
//		long sendSeconds = (System.currentTimeMillis() - beginTime) / 1000;
//		String[] arr = phone.split(",");
//		if(sendSeconds > 3 * arr.length) {
//			if(type == 1) {//如果当前短信通道为亿美的话，需要切换成互亿
//				LOG.error("--------短信发送通道从亿美切换成互亿--------");
//				SmsEmailCache.setSmsTriggerSwitch(3);//切换成互亿
//			} else if(type == 3) {//如果当前短信通道为互亿的话，需要切换成亿美
//				LOG.error("--------短信发送通道从互亿切换成亿美--------");
//				SmsEmailCache.setSmsTriggerSwitch(1);//切换成亿美
//			}
//		}
		return res;
	}
	/**
	 * 营销通道选择
	 * @param type
	 * @param content
	 * @param phone
	 * @return
	 */
	public  Integer chooseMarketingSmsChannel(int type,String content,String phone){
		int res=-1;
		if(type==1){
			res=emayMarketingSmsService.sendSMS(content, phone);
		}
		if(type==2){
			res=mwMarketingSmsService.sendSMSBySameMessage(phone, content);
		}
	
		return res;
	}
}
