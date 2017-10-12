package com.hc9.web.main.service.baofo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.constant.PayURL;
import com.hc9.web.main.dao.BankCardDao;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Generalize;
import com.hc9.web.main.entity.RegBonus;
import com.hc9.web.main.entity.UserBank;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.BonusService;
import com.hc9.web.main.service.PayLogService;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.DateUtil;
import com.hc9.web.main.util.ParseXML;

@Service
public class BankCardService {

	@Resource
	private HibernateSupport dao;
	
	@Resource
	private BankCardDao bankCardDao;
	
	@Resource
	private PayLogService payLogService;
	
	@Resource
	private BonusService bonusService;
	
	/** 绑定银行卡时调用宝付接口发送手机短信验证码 */
    public Map<String, Object> sendSmsCodeForBindCard(Long userId) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	
    	Userbasicsinfo user = dao.get(Userbasicsinfo.class, userId);
    	String code = "";
    	String msg = "";
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("requestParams", user.getpMerBillNo()));
			nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(user.getpMerBillNo()+ "~|~"+ ParameterIps.getMerchantKey())));
			payLogService.savePayLog(user.getUserName() + "发送手机验证码",user.getId(), 23);
			String result = CommonUtil.excuteRequest(PayURL.SENDBINDCODEURL,nvps);
			Document doc = DocumentHelper.parseText(result);
			Element rootElt = doc.getRootElement(); // 获取根节点
			// 拿到crs节点下的子节点code值
			String xmlcode = rootElt.elementTextTrim("code");
			String xmlmsg = rootElt.elementTextTrim("msg");
			String sign = rootElt.elementTextTrim("sign");
			String Md5sign = CommonUtil.MD5(xmlcode + "~|~" + xmlmsg + "~|~"+ ParameterIps.getMerchantKey());
			if (sign.equals(Md5sign)) {
				if (xmlcode.equals("CSD000")) {
					code = "0";
					msg = "发送短信成功！";
				} else {
					code = "2";
					msg = "宝付发送短信失败！";
				}
			} else {
				code = "2";
				msg = "数字签名不通过！";
			}
		} catch (Exception e) {
			LOG.error("用户 " + userId + " 绑定银行卡时调用宝付接口发送短信失败！" , e);
			code = "2";
			msg = "发送短息失败！";
		}
		
		map.put("code", code);
		map.put("msg", msg);
		return map;
    }
	
	 /** 绑定银行卡接口 */
    public Map<String, Object> bindBankCard(Long userId, String bankCardNo,  String validCode, 
    		String bankName, String proValue, String cityValue, String bankAddress) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	String resultCode = "1";
    	String resultMsg = "处理失败！";
    	Userbasicsinfo user = dao.get(Userbasicsinfo.class, userId);
    	String mobilePhone = user.getUserrelationinfo().getPhone();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		String bankCardId = "";
		try {
			UserBank userBank = new UserBank();
			boolean isbank = bankCardDao.getUserBank(bankCardNo);
			if(isbank) {
				userBank.setBank_name(bankName);
				userBank.setPro_value(proValue);
				userBank.setCity_value(cityValue);
				userBank.setBank_address(bankAddress);
				userBank.setMobile(mobilePhone);
				userBank.setBank_no(bankCardNo);
				userBank.setValidate_code(validCode);
				userBank.setState(0);
				userBank.setUserbasicsinfo(user);
				userBank.setBank_no(bankCardNo);
				Serializable seria = bankCardDao.saveUserBankSeria(userBank); // 保存银行卡
				bankCardId = seria.toString();
			} else {
				resultCode = "1";
				resultMsg = "该银行卡已经被使用，绑定失败！";
				map.put("code", resultCode);
				map.put("msg", resultMsg);
				return map;
			}
			String opBankCardXml = ParseXML.bindBankCardXml(userBank);
			String aesOpBankCardXml = CommonUtil.aesEncryptKey16(opBankCardXml,ParameterIps.getMerchantKey());
			LOG.error("绑卡传递给宝付的加密报文：" + aesOpBankCardXml);
			payLogService.savePayLog(opBankCardXml, user.getId(), 1, 22, bankCardId + userBank.getBank_no(), 0.00,0.00, 0.00); // 保存xml报文
			nvps.add(new BasicNameValuePair("requestParams", aesOpBankCardXml));
			nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(aesOpBankCardXml + "~|~"+ ParameterIps.getMerchantKey())));
			String result = CommonUtil.excuteRequest(PayURL.BINDBANKCARDURL, nvps);
			Document doc = DocumentHelper.parseText(result);
			Element rootElt = doc.getRootElement(); // 获取根节点
			// 拿到crs节点下的子节点code值
			String code = rootElt.elementTextTrim("code");
			String msg = rootElt.elementTextTrim("msg");
			String sign = rootElt.elementTextTrim("sign");
			String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getMerchantKey());
			UserBank userbBank = bankCardDao.getUserBankById(Long.valueOf(bankCardId));
			resultMsg = msg;
			if (sign.equals(Md5sign)) {
				if (code.equals("CSD000")) {
					String bfCardId = rootElt.elementTextTrim("card_id");
					if(bfCardId != null && bfCardId.trim().length() > 0) {
						if (userbBank.getState() != 1) {
							String sql = "update userbank set state=1,bfCardId=? where id=?";
							dao.executeSql(sql, bfCardId, bankCardId);
							String currentTime = DateUtil.format("yyyy-MM-dd HH:mm:ss");
							sql = "update paylog set pay_status=1,update_time=? where order_sn=?";
							dao.executeSql(sql, currentTime, bankCardId + userbBank.getBank_no());
							// 添加注册奖励
							if (null == bonusService.queryRegBonus(user.getId())) {  
								Generalize generalize=bonusService.queryGeneralize(user.getId());
								if(generalize!=null){
									long genuid = generalize.getGenuid();
									Userbasicsinfo referrer = dao.get(Userbasicsinfo.class, genuid);
									RegBonus regBonus = new RegBonus();
									regBonus.setUserbasicsinfo(user);
									regBonus.setReferrer(referrer);
									regBonus.setReleaseStatus(0);
									Costratio cos = bankCardDao.findCostratio();
									regBonus.setBouns(null != cos.getRegBonu()
											&& !"".equals(cos.getRegBonu()) ? cos.getRegBonu() : 0);
									dao.save(regBonus); // 保存信息
								}
							}
							resultCode = "0";
							resultMsg = "添加银行卡成功！";
					    	LOG.error("银行卡绑定成功！");
						}
					} else {
						LOG.error("宝付绑定银行卡过程中返回card_id为空！");
					}
				} else if(code.equals("CSD333")) {
					resultCode = "1";
				} else if(code.equals("BD004")) {
					resultCode = "1";
				} else {
					if(userbBank.getState() != 1) {
						userbBank.setState(-1);
						dao.update(userbBank);
						String orderSn = bankCardId + userBank.getBank_no();
						payLogService.updatePayLog(orderSn, -1);
						LOG.error("添加银行卡失败");
					}
				}
			}
		} catch (Exception e) {
			LOG.error("银行卡绑定或结果过程中出错！", e);
			resultCode = "1";
			resultMsg = "处理失败！";
		}
		map.put("code", resultCode);
		map.put("msg", resultMsg);
    	return map;
    }
}
