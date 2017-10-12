package com.hc9.web.main.service.smsmail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.dao.SmsEmailTimerDao;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.SmsEmailCache;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.EmaiLoansignInfo;
import com.hc9.web.main.vo.pay.Payuser;

/** 邮件发送服务 */
@Service
public class EmailSendService {
	@Resource
	private EmailService emailService;
	
	@Resource
	private SmsEmailTimerDao smsEmailTimerDao;
	
	@Resource
	private UserbasicsinfoService userbasicsinfoService;
	
	/** 在投资人的回款金额到达宝付账户时进行邮件通知 */
	public void sendReturnMoneyEmailNotify(final List<Payuser> userList, final String loansignName) {
		if(userList != null && userList.size() > 0) {
			String status = SmsEmailCache.getSmsEmailSwitchStatus("invest_return_money_email");
			if("1".equals(status)) {
				new Thread(
						new Runnable() {
							public void run() {
								Map<String, Object> map = new HashMap<String, Object>();
								for(Payuser payuser : userList) {
									String userId = payuser.getId();//投资人的用户id
									Double returnMoney = payuser.getAmount();//个人得到的钱
									map.clear();
									Userbasicsinfo investUser = userbasicsinfoService.queryUserById(Long.valueOf(userId));
										String userName = investUser.getName();
										if(StringUtil.isNotBlank(userName)) {
											List<EmaiLoansignInfo> loanList = smsEmailTimerDao.queryNewestThreeLoansign();
											String email = investUser.getUserrelationinfo().getEmail();
											if(StringUtil.isNotBlank(email)) {
												map.put("userName", userName);
												map.put("loansignName", loansignName);
												map.put("returnMoney", "" + returnMoney);
												map.put("loanList", loanList);
												try {//
													String[] msg = emailService.getEmailResources("returnmoney/return-money-notify.flt", map);
													LOG.error("回款通知待发邮箱标题："+msg[0] +"，待发邮箱内容：" + msg[1]);
													emailService.sendAdEmail(msg[0], msg[1], email);
												} catch(Exception e) {
													LOG.error("给用户" + userId + "发送还款通知邮件失败：", e);
												}
											}
										}
									}
								} 
							}
						).start();
			}
		}
	}
}