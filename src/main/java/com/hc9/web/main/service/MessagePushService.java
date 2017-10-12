package com.hc9.web.main.service;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.common.messagepush.AndroidNotification;
import com.hc9.web.main.common.messagepush.PushClient;
import com.hc9.web.main.common.messagepush.android.AndroidCustomizedcast;
import com.hc9.web.main.common.messagepush.ios.IOSCustomizedcast;
import com.hc9.web.main.entity.AppMessagePush;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Withdraw;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.vo.pay.Payuser;

/**
 * 消息推送service类
 * @author Jerry Wong
 * 
 */
@Service
public class MessagePushService {
	
	private static final Logger logger = Logger.getLogger(MessagePushService.class);
	
	private final static String INSERT_SQL = " insert into usermessage(context, isPush, receivetime, title, user_id) values (?, ?, ?, ?, ?) ";
	
	@Value("${push.env.mode}")
	private String mode;
	
	@Value("${push.env.test.phone}")
	private String testPhone;
	
	@Value("${push.ios.appKey}")
	private String iosAppKey;
	
	@Value("${push.andorid.appKey}")
	private String andoridAppKey;
	
	@Value("${push.ios.appMasterSecret}")
	private String iosAppMasterSecret;
	
	@Value("${push.andorid.appMasterSecret}")
	private String andoridAppMasterSecret;
	
	@Value("${push.aliasType}")
	private String aliasType;
	
	@Value("${push.moneyReturn.notification}")
	private String moneyReturnMsg;
	
	@Value("${push.investSuccessMsg.notification}")
	private String investSuccessMsg;
	
	@Value("${push.withdrawSuccess.notification}")
	private String withdrawSuccessMsg;
	
	@Autowired
	private HibernateSupport dao;
	
	@Autowired
	private PushClient client;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 自定义播(customizedcast)-alias
	 */
	public void pushMessage(AppMessagePush message, String aliasType, String phone, Map<String, String> customizedFields) throws Exception {
		
		// 构建ios消息
		IOSCustomizedcast iosMsg = new IOSCustomizedcast(iosAppKey, iosAppMasterSecret);
		iosMsg.setAlert(message.getContent());
 		iosMsg.setBadge(0);
 		iosMsg.setSound("default");
 		
 		if (mode.equals("product")) {
 			logger.debug("ios消息推送生产模式");
 			iosMsg.setAlias(phone, aliasType);
 			iosMsg.setProductionMode();
 		} else {
 			logger.debug("ios消息推送测试模式");
 			iosMsg.setAlias(testPhone, aliasType);
 			iosMsg.setTestMode();
 		}
 		
 		for (Entry<String, String> entry : customizedFields.entrySet()) {
 			iosMsg.setCustomizedField(entry.getKey(), entry.getValue());
 		}
 		iosMsg.setDescription(message.getDescription());
		
 		// 构建andorid消息
		AndroidCustomizedcast andoridMsg = new AndroidCustomizedcast(andoridAppKey, andoridAppMasterSecret);
		// 通知栏提示文字
 		andoridMsg.setTicker(message.getContent());
 		// 通知标题
 		andoridMsg.setTitle(customizedFields.get("title"));
 		// 通知文字描述
 		andoridMsg.setText(message.getContent());
		andoridMsg.goAppAfterOpen();
		andoridMsg.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);
		
		if (mode.equals("product")) {
			logger.debug("andorid消息推送生产模式");
			andoridMsg.setAlias(phone, aliasType);
 			andoridMsg.setProductionMode();
 		} else {
 			logger.debug("andorid消息推送测试模式");
 			andoridMsg.setAlias(testPhone, aliasType);
 			andoridMsg.setTestMode();
 		}
		
		for (Entry<String, String> entry : customizedFields.entrySet()) {
			andoridMsg.setExtraField(entry.getKey(), entry.getValue());
 		}
		andoridMsg.setDescription(message.getDescription());
		
		// 目前没有区分ios还是android，所以两个平台都推，可能存在推送失败的情况，只要有一个平台推送成功，则认为消息推送成功
		boolean ios = false;
		if (client.send(iosMsg)) {
			ios = true;
		}
		boolean andorid = false;
		if (client.send(andoridMsg)) {
			andorid = true;
		}
		if (ios || andorid) {	// 只要有一个平台推送成功，则认为消息推送成功
			message.setStatus(1);
		} else {	// 否则，消息推送失败
			message.setStatus(2);
		}
		
		// 消息推送记录持久化
		dao.save(message);
	}
	
	/**
	 * 在投资人的回款金额到达宝付账户时进行消息推送
	 * @param userList
	 * @param loansignName
	 */
	public void pushReturnMoneyMessage(final List<Payuser> userList, final String loansignName) {
		
		if (userList != null && userList.size() > 0) {
			
			final String[] msg = moneyReturnMsg.split("：");
			final Map<String, String> customizedField = new HashMap<String, String>();
			customizedField.put("page", "userMessage");
			customizedField.put("title", msg[0]);
			customizedField.put("sender", "system");
			
			new Thread(
				new Runnable() {
					public void run() {
						Map<String , Double> repaymentUserMap = new HashMap<String, Double>();
						for (Payuser payUser : userList) {
							String userId = payUser.getId();	// 投资人的用户id
							Double returnmoney = payUser.getAmount();	// 个人得到的钱
							if (!repaymentUserMap.containsKey(userId)) {
								repaymentUserMap.put(userId, returnmoney);
							} else {
								Double investMoney = repaymentUserMap.get(userId);
								Double totalMoney = Arith.add(investMoney, returnmoney);
								repaymentUserMap.put(userId, totalMoney);
							}
						}
						
						for (Map.Entry<String , Double> entry : repaymentUserMap.entrySet()) {
							String userId = entry.getKey();	// 投资人的用户id
							Double returnmoney = entry.getValue();	// 个人得到的钱
							
							Userbasicsinfo investUser = dao.get(Userbasicsinfo.class, Long.valueOf(userId));
							String phone = investUser.getUserrelationinfo().getPhone();
							String userName = investUser.getUserName();
							
							try {
								// 插入用户站内消息表-usermessage
								dao.executeSql(MessagePushService.INSERT_SQL, 
										MessageFormat.format(msg[1], loansignName, returnmoney), 
										1,
										sdf.format(new Date()), 
										msg[0], 
										userId);
								
								// 推送回款提醒消息
								AppMessagePush message = new AppMessagePush();
								// customizedcast-alias
								message.setPushType(6);
								message.setPushNow(1);
								message.setContent(MessageFormat.format(msg[1], loansignName, returnmoney));
								message.setDescription(msg[0]);
								message.setOperator(customizedField.get("sender"));
								message.setTitle(customizedField.get("title"));
								
								pushMessage(message, aliasType, phone, customizedField);
								
							} catch (Exception e) {
								logger.error("给投资人：[" + userName + "]，手机号码：[" + phone + "]的回款提醒消息推送失败！", e);
							}
						}
					} 
				}
			).start();
		}
	}
	
	/**
	 * 投资成功后给投资人推送投资成功消息
	 * @param user
	 * @param loan
	 * @param loanrecord
	 */
	public void pushMessageToInvestorAfterSuccess(Userbasicsinfo user, Loansign loan, Loanrecord loanrecord) {
		
		
		String msgTitle = investSuccessMsg.substring(0, investSuccessMsg.indexOf("："));
		String msgContent = investSuccessMsg.substring(investSuccessMsg.indexOf("：") + 1);
		Map<String, String> customizedField = new HashMap<String, String>();
		customizedField.put("page", "userMessage");
		customizedField.put("title", msgTitle);
		customizedField.put("sender", "system");
		
		// 1、优先；2、夹层；3、劣后；
		int investType = loanrecord.getSubType();	
		String loanType = "优先";
		if(investType == 2) {
			loanType = "夹层";
		} else if(investType == 3) {
			loanType = "劣后";
		}
		
		double investMoney = loanrecord.getTenderMoney();
		DecimalFormat investMoneyFormat = new DecimalFormat("#.0");
		
		// 1-店铺 2-项目 3-天标 4-债权转让
		Integer type = loan.getType();
		String reMonth = loan.getRemonth() + "个月";
		if (type.intValue() == 3) {	// 天标
			reMonth = loan.getRemonth() + "天";
		}
		
		String userName = loanrecord.getUserbasicsinfo().getUserName();
		String phone = loanrecord.getUserbasicsinfo().getUserrelationinfo().getPhone();
		
		try {
			// 插入用户站内消息表-usermessage
			dao.executeSql(MessagePushService.INSERT_SQL, 
					MessageFormat.format(msgContent, loan.getName(), loanType, investMoneyFormat.format(investMoney), reMonth), 
					1,
					sdf.format(new Date()), 
					msgTitle, 
					user.getId());
			
			// 推送回款提醒消息
			AppMessagePush message = new AppMessagePush();
			// customizedcast-alias
			message.setPushType(6);
			message.setPushNow(1);
			message.setContent(MessageFormat.format(msgContent, loan.getName(), loanType, investMoneyFormat.format(investMoney), reMonth));
			message.setDescription(msgTitle);
			message.setOperator(customizedField.get("sender"));
			message.setTitle(customizedField.get("title"));
			
			pushMessage(message, aliasType, phone, customizedField);
		} catch(Exception e) {
			logger.error("给投资人：[" + userName + "]，手机号码：[" + phone + "]的投标成功提醒消息推送失败！", e);
		}
	}
	
	public void pushMessageAfterWithdrawSuccess(Withdraw withdraw) {
		Date now = new Date();
		
		String[] msg = withdrawSuccessMsg.split("：");
		Map<String, String> customizedField = new HashMap<String, String>();
		customizedField.put("page", "userMessage");
		customizedField.put("title", msg[0]);
		customizedField.put("sender", "system");
		
		Double withdrawMoney = withdraw.getAmount();
		String phone = withdraw.getUserbasicsinfo().getUserrelationinfo().getPhone();
		String userName = withdraw.getUserbasicsinfo().getUserName();
		Long userId = withdraw.getUserbasicsinfo().getId();
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(now);
		
		try {
			// 插入用户站内消息表-usermessage
			dao.executeSql(MessagePushService.INSERT_SQL, 
					MessageFormat.format(msg[1], userName, dateStr, withdrawMoney),
					1,
					sdf.format(new Date()), 
					msg[0], 
					userId);
			
			// 推送回款提醒消息
			AppMessagePush message = new AppMessagePush();
			// customizedcast-alias
			message.setPushType(6);
			message.setPushNow(1);
			message.setContent(MessageFormat.format(msg[1], userName, dateStr, withdrawMoney));
			message.setDescription(msg[0]);
			message.setOperator(customizedField.get("sender"));
			message.setTitle(customizedField.get("title"));
			
			pushMessage(message, aliasType, phone, customizedField);
		} catch(Exception e) {
			logger.error("给投资人：[" + userName + "]，手机号码：[" + phone + "]的提现提醒消息推送失败！", e);
		}
	}
}