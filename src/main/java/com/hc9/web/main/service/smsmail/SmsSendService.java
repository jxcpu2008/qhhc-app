package com.hc9.web.main.service.smsmail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.SmsEmailCache;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.pay.Payuser;

/** 短信发送服务 */
@Service
public class SmsSendService {
	@Resource
	SmsService smsService;
	
	@Resource
	UserbasicsinfoService userbasicsinfoService;
	
	/** 在投资人的回款金额到达宝付账户时进行短信通知 */
	public void sendReturnMoneySmsNotify(final List<Payuser> userList,final String loansignName) {
		if(userList != null && userList.size() > 0) {
			String status = SmsEmailCache.getSmsEmailSwitchStatus("invest_return_money_sms");
			if("1".equals(status)) {
				new Thread(
					new Runnable() {
						public void run() {
							Map<String, String> map = new HashMap<String, String>();
							int trigger=Integer.valueOf(SmsEmailCache.getSmsTriggerChannel());
							Map<String , Double> repaymentUserMap = new HashMap<String, Double>();
							for(Payuser payuser : userList) {
								String userId = payuser.getId();//投资人的用户id
								Double returnmoney = payuser.getAmount();//个人得到的钱
								if(!repaymentUserMap.containsKey(userId)) {
									repaymentUserMap.put(userId, returnmoney);
								} else {
									Double investMoney = repaymentUserMap.get(userId);
									Double totalMoney = Arith.add(investMoney, returnmoney);
									repaymentUserMap.put(userId, totalMoney);
								}
							}
							for(Map.Entry<String , Double> entry : repaymentUserMap.entrySet()) {
								String userId = entry.getKey();//投资人的用户id
								Double returnmoney = entry.getValue();//个人得到的钱
								map.clear();
								try {
									Userbasicsinfo investUser = userbasicsinfoService.queryUserById(Long.valueOf(userId));
									String userName = investUser.getName();
									if(StringUtil.isNotBlank(userName)) {
										String phone = investUser.getUserrelationinfo().getPhone();
										if(StringUtil.isNotBlank(userName)) {
											map.put("userName", userName);
											map.put("returnMoney", "" + returnmoney);
											map.put("loansignName", loansignName);
											String content = smsService.getSmsResources("returnmoney/return-money-notify.flt", map);
											
											//处理UTF-8字符串读取字节的问题,截掉第一个字节
											byte[] contentByte=content.getBytes();
											byte[] temp = new byte[contentByte.length-1];
											for(int i=1;i<contentByte.length;i++){
												temp[i-1]=contentByte[i];
											}
											content=new String(temp);
											LOG.error("回款通知待发送短息内容：" + content);
											smsService.chooseSmsChannel(trigger, content, phone);
										}
									}
								} catch(Exception e) {
									LOG.error("给用户发送回款失败：", e);
								}
							}
						} 
					}
				).start();
			}
		}
	}
	
}
