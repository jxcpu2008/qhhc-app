package com.hc9.web.main.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.constant.PayURL;
import com.hc9.web.main.dao.RepayDao;
import com.hc9.web.main.entity.Accountinfo;
import com.hc9.web.main.entity.Accounttype;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Loandynamic;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.RepaymentRecordDetail;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Userfundinfo;
import com.hc9.web.main.entity.VoteIncome;
import com.hc9.web.main.service.IRepaymentService;
import com.hc9.web.main.service.MessagePushService;
import com.hc9.web.main.service.PayLogService;
import com.hc9.web.main.service.smsmail.EmailSendService;
import com.hc9.web.main.service.smsmail.SmsSendService;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.ParseXML;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.RepaymentRequest;
import com.hc9.web.main.vo.pay.AcctTrans;
import com.hc9.web.main.vo.pay.BidInfo4;
import com.hc9.web.main.vo.pay.Payuser;
import com.hc9.web.main.vo.pay.crs;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import freemarker.template.TemplateException;

/**
 * 还款服务类
 * @author Jerry Wong
 *
 */
@Service
public class MyRepaymentService implements IRepaymentService {
	
	private static final Logger logger = Logger.getLogger(MyRepaymentService.class);
	
	@Autowired
	private PayLogService payLogService;
	
	@Autowired
	private SmsSendService smsSendService;
	
	@Autowired
	private EmailSendService emailSendService;
	
	@Autowired
	private MessagePushService messagePushService;
	
	@Autowired
	private RepayDao repayDao;

	/**
	 * 天标申请提前还款
	 */
	@Override
	public void applyInAdvanceRepay(RepaymentRequest repaymentRequest, List<RepaymentRecordDetail> repaymentRecordDetails) throws Exception {
		logger.info("开始提前还款相关计算！");
		
		Costratio settingParams = repaymentRequest.getDefaultFeeConfig();
		// 应付总利息
		double expectInterest = repaymentRequest.getExpectInterest();
		// 实付总利息
		double realInterest = repaymentRequest.getRealInterest();
		// 投资本金
		double investPrincipal = 0.00d;
		// 应收利息
		double calInterest = 0.00d;
		// 投资利息
		double investInterest = 0.00d;
		// 利息管理费率
		double interestManageRate = 0.00d;
		// 利息管理费
		double interestManageFee = 0.00d;
		// 实际应付本息，对应投资者实际应收本息
		double realRepayAmount = 0.00d;
		
		// 遍历当期应还款明细记录
		for (RepaymentRecordDetail repayDetail : repaymentRecordDetails) {
			if (repayDetail.getLoanType() == 1) {	// 投资类型为优先
				// 优先本金
				investPrincipal = repayDetail.getMoney();
				// 优先投资对应应收利息
				calInterest = repayDetail.getPreRepayMoney();
				interestManageRate = settingParams.getPrioInvest();
			} else if (repayDetail.getLoanType() == 2) {	// 投资类型为夹层
				// 优先本金
				investPrincipal = repayDetail.getMiddleMoney();
				// 夹层投资对应应收利息
				calInterest = repayDetail.getMiddlePreRepayMoney();
				interestManageRate = settingParams.getMidInvest();
			} else if (repayDetail.getLoanType() == 3) {	// 投资类型为劣后
				// 优先本金
				investPrincipal = repayDetail.getAfterMoney();
				// 劣后投资对应应收利息
				calInterest = repayDetail.getAfterPreRepayMoney();
				interestManageRate = settingParams.getAfterInvest();
			} else {
				// do nothing
			}
			
			// 计算投资实际获取的利息 = （应收利息 / 应付总利息） * 实付利息
			investInterest = Arith.mul(Arith.div(calInterest, expectInterest), realInterest);
			// 计算应付的利息管理费
			interestManageFee = Arith.mul(investInterest, interestManageRate);
			// 计算投资者实际获得的本息和，即融资人应付给投资人的金额
			realRepayAmount = Arith.add(investPrincipal, investInterest);
			
			if (repayDetail.getLoanType() == 1) {	// 投资类型为优先
				repayDetail.setRealMoney(realRepayAmount);
			} else if (repayDetail.getLoanType() == 2) {	// 投资类型为夹层
				repayDetail.setMiddleRealMoney(realRepayAmount);
			} else if (repayDetail.getLoanType() == 3) {	// 投资类型为劣后
				repayDetail.setAfterRealMoney(realRepayAmount);
			} else {
				// do nothing
			}
			
			repayDetail.setFee(interestManageFee);
			repayDetail.setRepState(2);
		}
		
		logger.info("准备申请提前还款！");
		// 保存提前还款申请记录
		repayDao.saveInAdvanceRepayApply(repaymentRecordDetails);
		// 设置还款记录的还款状态为提前还款中
		repayDao.setRepaymentRecordInAdvanceRepayState(repaymentRequest.getRepayRecordId());
		logger.info("申请提前还款成功！");
	
		// just for test use
//		List<RepaymentRecordDetail> testRecords = repayDao.getApplyInadvanceRecords(repaymentRequest.getRepayRecordId());
//		JSONObject testObject = repay(repaymentRequest, testRecords);
//		logger.info("申请提前还款模拟测试还款操作，即向宝付发送报文开始真正的还款转账，处理结果的json字符串为：" + testObject.toString());
	}
	
	/**
	 * 多月标申请提前还款
	 */
	@Override
	public void applyInAdvanceRepay(RepaymentRequest repaymentRequest, List<Repaymentrecord> repayRecords, List<Loanrecord> investRecords) throws Exception {
		logger.info("开始多月标的提前还款相关计算！");

		Costratio settingParams = repaymentRequest.getDefaultFeeConfig();
		// 实付总利息
		double realInterest = repaymentRequest.getRealInterest();
		
		// 已还金额
		double repayedAmount = 0.0000d;
		// 当期的还款记录
		Repaymentrecord currentRepayRecord = null;
		
		List<Repaymentrecord> records = new ArrayList<Repaymentrecord>();
		if (repayRecords != null && repayRecords.size() > 0) {
			for (Repaymentrecord repayRecord : repayRecords) {
				if (repaymentRequest.getRepayPeriod() > repayRecord.getPeriods().intValue()) {	// 融资用户已经偿还过的记录不算
					repayedAmount += repayRecord.getRealMoney();
				} else if (repaymentRequest.getRepayPeriod() == repayRecord.getPeriods().intValue()) {
					repayRecord.setRepayState(6);
					currentRepayRecord = repayRecord;
				} else {
					repayRecord.setRepayState(6);
					records.add(repayRecord);
				}
			}
		}
		
		// 融资用户应偿还的利息应该排除已经偿还过的利息
		if (repayedAmount > 0) {
			realInterest = realInterest - repayedAmount;
		}
		
		// 计息期数，因为是多月标的提前还款，故计息期限为当期期限+1
		int calPeriods = repaymentRequest.getRepayPeriod() + 1;
		// 优先本金
		double priorityPrincipal = repaymentRequest.getLoan().getPriority();
		double middlePrincipal = repaymentRequest.getLoan().getMiddle();
		double afterPrincipal = repaymentRequest.getLoan().getAfter();
		// 优先年化利率
		double priorityInterestRate4Year = repaymentRequest.getLoan().getPrioRate() + repaymentRequest.getLoan().getPrioAwordRate();
		// 优先每期利率
		double priorityInterestRate = 0.00d;
		// 夹层每期利率
		double middleInterestRate = 0.00d;
		// 还款方式
		int refundWay = repaymentRequest.getLoan().getRefunway();
		if (refundWay == 1) {	// 按月付息到期还本
			priorityInterestRate = Arith.round(Arith.div(priorityInterestRate4Year, 12), 8);
			middleInterestRate  = Arith.round(Arith.div(repaymentRequest.getLoan().getMidRate(), 12), 8);
		} else if (refundWay == 2) {	// 按季付息到期还本
			priorityInterestRate = Arith.round(Arith.div(priorityInterestRate4Year, 4), 8);
			middleInterestRate  = Arith.round(Arith.div(repaymentRequest.getLoan().getMidRate(), 4), 8);
		} else {
		}
		
		// 优先利息
		double priorityInterest = Arith.mul(priorityPrincipal, priorityInterestRate) * calPeriods;
		// 优先本息和
		double priorityAmount = Arith.add(priorityPrincipal, priorityInterest);
		// 夹层利息
		double middleInterest = Arith.mul(middlePrincipal, middleInterestRate) * calPeriods;
		// 夹层本息和
		double middleAmount = Arith.add(middlePrincipal, middleInterest);
		// 劣后利息
		double afterInterest = Arith.sub(realInterest, Arith.add(priorityInterest, middleInterest));
		double afterAmount = Arith.add(afterPrincipal, afterInterest);
		
		// 更新当期的还款记录
		currentRepayRecord.setMoney(priorityPrincipal);
		currentRepayRecord.setRealMoney(priorityAmount);
		currentRepayRecord.setMiddleMoney(middlePrincipal);
		currentRepayRecord.setMiddleRealMoney(middleAmount);
		currentRepayRecord.setAfterMoney(afterPrincipal);
		currentRepayRecord.setAfterRealMoney(afterAmount);
		records.add(currentRepayRecord);
		
		// 投资本金
		double investPrincipal = 0.00d;
		// 投资利息
		double investInterest = 0.00d;
		// 利息管理费率
		double interestManageRate = 0.00d;
		// 利息管理费
		double interestManageFee = 0.00d;
		// 实际应付本息，对应投资者实际应收本息
		double realRepayAmount = 0.00d;
		// 投资类型
		int investType = 0;
		
		List<RepaymentRecordDetail> repaymentRecordDetails = new ArrayList<RepaymentRecordDetail>();
		if (investRecords != null && investRecords.size() > 0) {
			for (Loanrecord investRecord : investRecords) {
				RepaymentRecordDetail repayDetail = new RepaymentRecordDetail();
				
				investType = investRecord.getSubType();
				// 投资本金
				investPrincipal = investRecord.getTenderMoney();
				
				if (investType == 1) {	// 投资类型为优先
					// 计算优先投资获取的利息
					investInterest = Arith.mul(Arith.div(investPrincipal, priorityPrincipal), priorityInterest);
					// 计算投资者实际获得的本息和，即融资人应付给投资人的本息总额
					realRepayAmount = Arith.add(investPrincipal, investInterest);
					interestManageRate = settingParams.getPrioInvest();
					
					repayDetail.setMoney(investPrincipal);
					repayDetail.setPreRepayMoney(investInterest);
					repayDetail.setRealMoney(realRepayAmount);
				} else if (investType == 2) {	// 投资类型为夹层
					// 计算夹层投资获取的利息
					investInterest = Arith.mul(Arith.div(investPrincipal, middlePrincipal), middleInterest);
					// 计算投资者实际获得的本息和，即融资人应付给投资人的本息总额
					realRepayAmount = Arith.add(investPrincipal, investInterest);
					interestManageRate = settingParams.getMidInvest();
					
					repayDetail.setMiddleMoney(investPrincipal);
					repayDetail.setMiddlePreRepayMoney(investInterest);
					repayDetail.setMiddleRealMoney(realRepayAmount);
				} else if (investType == 3) {	// 投资类型为劣后
					// 计算劣后投资获取的利息
					investInterest = Arith.mul(Arith.div(investPrincipal, afterPrincipal), afterInterest);
					// 计算投资者实际获得的本息和，即融资人应付给投资人的本息总额
					realRepayAmount = Arith.add(investPrincipal, investInterest);
					interestManageRate = settingParams.getAfterInvest();
					
					repayDetail.setAfterMoney(investPrincipal);
					repayDetail.setAfterPreRepayMoney(investInterest);
					repayDetail.setAfterRealMoney(realRepayAmount);
				} else {
				}
				
				// 计算应付的利息管理费
				interestManageFee = Arith.mul(investInterest, interestManageRate);
				
				repayDetail.setUserbasicsinfo(investRecord.getUserbasicsinfo());
				repayDetail.setFee(interestManageFee);
				repayDetail.setRepaymentrecord(currentRepayRecord);
				// 申请提前还款
				repayDetail.setRepState(2);
				repayDetail.setLoanType(investType);
				repayDetail.setLoanrecord(investRecord);
				repaymentRecordDetails.add(repayDetail);
			}
		}
		
		logger.info("准备申请提前还款！");
		// 保存提前还款申请记录
		repayDao.saveInAdvanceRepayApply(repaymentRecordDetails);
		// 更新还款记录
		repayDao.updateRepaymentRecords(records);
		logger.info("申请提前还款成功！");
		
		// just for test use
//		List<RepaymentRecordDetail> testRecords = repayDao.getApplyInadvanceRecords(repaymentRequest.getRepayRecordId());
//		JSONObject testObject = repay(repaymentRequest, testRecords);
//		logger.info("申请提前还款模拟测试还款操作，即向宝付发送报文开始真正的还款转账，处理结果的json字符串为：" + testObject.toString());
	}
	
	@Override
	public JSONObject overdueRepay(int overdueDays, RepaymentRequest repaymentRequest, List<RepaymentRecordDetail> repaymentRecordDetails) throws Exception {
		logger.info("开始逾期还款相关计算！");
		
		// 拿到逾期利率
		Costratio settingParams = repaymentRequest.getDefaultFeeConfig();
		
		// 投资本金
		double investPrincipal = 0.00d;
		// 投资本金对应的应收利息
		double calInterest = 0.00d;
		// 投资本息和
		double repayAmount = 0.00d;
		// 逾期利息
		double overdueAmount = 0.00d;
		// 实际应付本息，对应投资者实际应收本息
		double realRepayAmount = 0.00d;
		
		// 利息管理费率
		double interestManageRate = 0.00d;
		// 利息管理费
		double interestManageFee = 0.00d;
		
		// 遍历当期应还款明细记录
		for (RepaymentRecordDetail repayDetail : repaymentRecordDetails) {
			if (repayDetail.getLoanType() == 1) {	// 投资类型为优先
				// 优先本金
				investPrincipal = repayDetail.getMoney();
				// 优先投资对应应收利息
				calInterest = repayDetail.getPreRepayMoney();
				interestManageRate = settingParams.getPrioInvest();
			} else if (repayDetail.getLoanType() == 2) {	// 投资类型为夹层
				// 优先本金
				investPrincipal = repayDetail.getMiddleMoney();
				// 夹层投资对应应收利息
				calInterest = repayDetail.getMiddlePreRepayMoney();
				interestManageRate = settingParams.getMidInvest();
			} else if (repayDetail.getLoanType() == 3) {	// 投资类型为劣后
				// 优先本金
				investPrincipal = repayDetail.getAfterMoney();
				// 劣后投资对应应收利息
				calInterest = repayDetail.getAfterPreRepayMoney();
				interestManageRate = settingParams.getAfterInvest();
			} else {
				// do nothing
			}
			
			repayAmount = Arith.add(investPrincipal, calInterest);
			// 计算逾期利息
			overdueAmount = Arith.mul(Arith.mul(repayAmount, settingParams.getOverdueRepayment()), overdueDays);
			repaymentRequest.setOverdueAmount(overdueAmount);
			
			// 计算投资者实际获得的本息和，即融资人应付给投资人的金额
			realRepayAmount = Arith.add(repayAmount, overdueAmount);
			
			// 计算应付的利息管理费
			interestManageFee = Arith.mul(calInterest, interestManageRate);
			
			if (repayDetail.getLoanType() == 1) {	// 投资类型为优先
				repayDetail.setRealMoney(realRepayAmount);
			} else if (repayDetail.getLoanType() == 2) {	// 投资类型为夹层
				repayDetail.setMiddleRealMoney(realRepayAmount);
			} else if (repayDetail.getLoanType() == 3) {	// 投资类型为劣后
				repayDetail.setAfterRealMoney(realRepayAmount);
			} else {
				// do nothing
			}
			
			repayDetail.setFee(interestManageFee);
		}
		
		// 还款
		return repay(repaymentRequest, repaymentRecordDetails);
	}

	/**
	 * 还款
	 */
	@Override
	public JSONObject repay(RepaymentRequest repaymentRequest, List<RepaymentRecordDetail> repaymentRecordDetails) throws Exception {
		double myMoney = 0.00d;
		// 融资人当期累计还款金额
		double totalRepayMoney = 0.00d;
		List<Payuser> investUsers = new ArrayList<Payuser>();
		String repayAction = repaymentRequest.getRepayAction();
		
		for (RepaymentRecordDetail repayDetail : repaymentRecordDetails) {
			if (repayDetail.getLoanType() == 1) {	// 投资类型为优先
				if (repayAction.equals("repay")) {
					myMoney = Arith.add(repayDetail.getMoney(), repayDetail.getPreRepayMoney());
				} else {
					myMoney = repayDetail.getRealMoney();
				}
			} else if (repayDetail.getLoanType() == 2) {	// 投资类型为夹层
				if (repayAction.equals("repay")) {
					myMoney = Arith.add(repayDetail.getMiddleMoney(), repayDetail.getMiddlePreRepayMoney());
				} else {
					myMoney = repayDetail.getMiddleRealMoney();
				}
			} else if (repayDetail.getLoanType() == 3) {	// 投资类型为劣后
				if (repayAction.equals("repay")) {
					myMoney = Arith.add(repayDetail.getAfterMoney(), repayDetail.getAfterPreRepayMoney());
				} else {
					myMoney = repayDetail.getAfterRealMoney();
				}
			} else {
				// do nothing
			}
			
			if (myMoney > 0) {	// 只有确实需要给投资人转账，才进行转账，即转账金额要大于0
				Payuser investUser = new Payuser();
				// 投资的本息和
				investUser.setAmount(Arith.round(myMoney, 2));
				// 用户宝付账号
				investUser.setUser_id(repayDetail.getUserbasicsinfo().getpMerBillNo());
				// 利息管理费
				investUser.setFee(Arith.round(repayDetail.getFee(), 2));
				investUser.setId(String.valueOf(repayDetail.getUserbasicsinfo().getId()));
				// 当期应还款明细记录主键id
				investUser.setrId(String.valueOf(repayDetail.getId()));  
				investUsers.add(investUser);
				
				totalRepayMoney += myMoney;
			}
		}
		
		// 还款订单号
		String orderNum = "HB" + repaymentRequest.getLoanId() + "_" + new Date().getTime();
		
		BidInfo4 bi = new BidInfo4();
		bi.setCus_id(repaymentRequest.getLoanId());
		bi.setCus_name(repaymentRequest.getLoanName());
		bi.setBrw_id(repaymentRequest.getLoginUser().getpMerBillNo());
		bi.setReq_time(String.valueOf(new Date().getTime()));
		bi.setVoucher_id(repaymentRequest.getLoginUser().getpMerBillNo());
		bi.setVoucher_fee(repaymentRequest.getDefaultFeeConfig().getVoucherFee());
		bi.setSpecial(1);
		// 手续费，管理费
		bi.setFee(0.00);
		bi.setPayuser(investUsers);
		bi.setMerchant_id(ParameterIps.getCert());
		bi.setAction_type(4);
		bi.setOrder_id(orderNum);
		
		JSONObject json = new JSONObject();
		json.element("code", 100);
		json.element("msg", "宝付还款受理成功！");
		try {
			// 生成还标请求报文
			String readxml = ParseXML.bidinfo4XML(bi);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("requestParams", readxml));
			nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(readxml + "~|~" + ParameterIps.getMerchantKey())));
			payLogService.savePayLog(readxml, 
					repaymentRequest.getLoginUser().getId(), 
					repaymentRequest.getLoanId(), 
					7, 
					orderNum, 
					bi.getFee(), 
					bi.getFee(),
					totalRepayMoney,
					repaymentRequest.getRepayRecordId());
			
			logger.info("融资人还款金额为：" + repaymentRequest.getRepayAmount());
			logger.info("投资人应收金额为：" + totalRepayMoney);
			
			logger.info("标的[" + repaymentRequest.getLoanId() + "]对应的当期还款记录[" + repaymentRequest.getRepayRecordId() + "]的请求报文为：");
			logger.info(readxml);
			
			String result = CommonUtil.excuteRequest(PayURL.REPAYMRNTTESTURL, nvps);
//			String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><crs><code>CSD000</code><msg>处理成功</msg><sign>e89e3be8e509b8a220c1f3ca3490b089</sign></crs>";
			result = result.replace("\"", "\'");
			crs cr = new crs();
			XStream xss = new XStream(new DomDriver());
			xss.alias(cr.getClass().getSimpleName(), cr.getClass());
			cr = (crs) xss.fromXML(result);
			String Md5sign = CommonUtil.MD5(cr.getCode() + "~|~" + cr.getMsg() + "~|~" + ParameterIps.getDes_algorithm());
			
			if (cr.getSign().equals(Md5sign)) {	// 宝付返回报文签名验证通过
				if (cr.getCode().equals("CSD000")) {	// 响应正常
					logger.info("宝付还款受理成功！");
					
					// 获取流水账类型对象
					Accounttype investType = repayDao.getAccounttype(20L);
					
					List<Accountinfo> accountInfos = new ArrayList<Accountinfo>();
					List<Userfundinfo> userFundInfos = new ArrayList<Userfundinfo>();
					// 投资用户的本息和
					double income = 0.00d;
					// 投资用户的余额
					double investBalance = 0.00d;
					
					for (RepaymentRecordDetail repayDetail : repaymentRecordDetails) {
						// 投资用户的收入流水账
						Accountinfo investAccount = new Accountinfo();
						investAccount.setExpenditure(0.00);
						investAccount.setExplan("投资回款");
						if (repayDetail.getLoanType() == 1) {	// 优先类型投资
							if (repayAction.equals("repay")) {
								income = Arith.add(repayDetail.getMoney(), repayDetail.getPreRepayMoney());
							} else {
								income = repayDetail.getRealMoney();
							}
						} else if (repayDetail.getLoanType() == 2) {	// 夹层投资类型
							if (repayAction.equals("repay")) {
								income = Arith.add(repayDetail.getMiddleMoney(), repayDetail.getMiddlePreRepayMoney());
							} else {
								income = repayDetail.getMiddleRealMoney();
							}
						} else if (repayDetail.getLoanType() == 3) {	// 劣后投资类型
							if (repayAction.equals("repay")) {
								income = Arith.add(repayDetail.getAfterMoney(), repayDetail.getAfterPreRepayMoney());
							} else {
								income = repayDetail.getAfterRealMoney();
							}
						} else {
							// do nothing
						}
						
						if (income > 0) {
							investAccount.setIncome(income);
							investAccount.setFee(repayDetail.getFee());	// 利息管理费
							investAccount.setIpsNumber(orderNum);
							investAccount.setLoansignId(String.valueOf(repaymentRequest.getLoanId()));	// 标的主键id
							investAccount.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
							investAccount.setUserbasicsinfo(repayDetail.getUserbasicsinfo());
							investAccount.setAccounttype(investType);
							
							// 更新投资用户的余额
							investBalance = repayDetail.getUserbasicsinfo().getUserfundinfo().getOperationMoney();
							investBalance = Arith.add(investBalance, income);
							
							investAccount.setMoney(investBalance);
							accountInfos.add(investAccount);
							
							// 更新投资用户资金信息表-userfundinfo表的operation_money字段
							repayDetail.getUserbasicsinfo().getUserfundinfo().setOperationMoney(investBalance);
							userFundInfos.add(repayDetail.getUserbasicsinfo().getUserfundinfo());
						}
					}
					
					// 批量保存投资用户收入流水账
					repayDao.saveAccount(accountInfos);
					
					// 批量更新投资用户资金余额
					repayDao.updateUserFundBalance(userFundInfos);
					
					// 由于每期的还款涉及的投资人数不固定，又提交给宝付的还款报文不可能无限的大，所以某期的还款可能需要分多次向宝付发送还款报文
					// 当所有的还款报文都处理完毕后，继续还款的后续操作
					if (true) {
						// 更新repaymentrecordparticulars表的repState状态
						if (repayAction.equals("inadvanceRepay")) {
							repayDao.setRepaySuccess4RepaymentRecordDetail(5, repaymentRequest.getRepayRecordId(), "inadvanceRepay");
							
							// 更新标的状态为10-提前还款
							repaymentRequest.getLoan().setStatus(10);
							repayDao.setInadvanceRepayed4Loan(repaymentRequest.getLoan());
						} else {
							repayDao.setRepaySuccess4RepaymentRecordDetail(1, repaymentRequest.getRepayRecordId(), null);
						}
						
						double fee = 0.00d;
						List<Repaymentrecord> repayRecords = repayDao.getRepayRecords(repaymentRequest.getLoanId());
						if (repayRecords != null && repayRecords.size() > 0) {
							for (Repaymentrecord record : repayRecords) {
								fee = record.getCompanyPreFee();
								
								record.setCompanyRealFee(record.getCompanyPreFee());
								record.setRepayTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
								record.setpIpsBillNo(orderNum);
								record.setpIpsTime2(DateUtils.format("yyyy-MM-dd"));
								
								if (record.getPeriods() == repaymentRequest.getRepayPeriod()) {
									// 此处要根据实际的还款情况来设置相应的状态，eg：如果是提前还款，则设置还款状态为5
									if (repayAction.equals("inadvanceRepay")) {
										record.setRepayState(5);
									} else if (repayAction.equals("overdueRepay")) {
										record.setRepayState(4);
										// 逾期利息
										record.setOverdueInterest(repaymentRequest.getOverdueAmount());
									} else {
										record.setRepayState(2);
									}
								} else if (record.getPeriods() > repaymentRequest.getRepayPeriod()) {
									record.setRealMoney(Double.valueOf(0));
									record.setMiddleRealMoney(Double.valueOf(0));
									record.setAfterRealMoney(Double.valueOf(0));
									record.setRepayState(5);
								} else {
								}
							}
						}
						repayDao.updateRepaymentRecords(repayRecords);
						
						// TODO 平台收取服务费操作 如果多月标还款，服务费怎么收？
						if (repaymentRequest.getFeeState() == 2) {
							ipsCompanyFee(repaymentRequest.getLoginUser(), repaymentRequest.getLoanId(), Arith.round(fee, 2));
							totalRepayMoney = Arith.add(totalRepayMoney, fee);
							logger.info("额外支付服务费用，融资人还款金额为：" + totalRepayMoney);
						}
						
						// 融资用户的还款流水账
						Accounttype repayType = repayDao.getAccounttype(4L);
						Accountinfo repayAccount = new Accountinfo();
						repayAccount.setExpenditure(totalRepayMoney);
						repayAccount.setExplan("项目还款");
						repayAccount.setIncome(0.00);
						repayAccount.setIpsNumber(orderNum);
						repayAccount.setLoansignId(String.valueOf(repaymentRequest.getLoanId()));	// 标的主键id
						repayAccount.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
						repayAccount.setUserbasicsinfo(repaymentRequest.getLoginUser());
						repayAccount.setAccounttype(repayType);
						repayAccount.setFee(0.00);
						
						// 更新融资用户的余额
						Double balance = repaymentRequest.getLoginUser().getUserfundinfo().getOperationMoney();
						if (balance == null || (balance != null && balance.doubleValue() == 0.0000)) {
							balance = repaymentRequest.getLoginUser().getUserfundinfo().getMoney();
						}
						balance = Arith.sub(balance, totalRepayMoney);
						repayAccount.setMoney(balance);
						
						// 更新融资用户资金信息表-userfundinfo表的operation_money字段
						repaymentRequest.getLoginUser().getUserfundinfo().setOperationMoney(balance);
						
						// 保存融资用户还款流水账
						repayDao.saveAccount(repayAccount);
						
						// 更新融资用户资金余额
						repayDao.updateUserFundBalance(repaymentRequest.getLoginUser().getUserfundinfo());
						
						// 新增一条loandynamic表记录-项目动态记录
						String periods = "第" + repaymentRequest.getRepayPeriod() + "期";
						if (repaymentRequest.getLoanType() == 2) {	//项目
							if (repaymentRequest.getRepayPeriod() == repaymentRequest.getLoanPeriods()) {
								periods = "";
							}
						}
						String realStr = repaymentRequest.getLoanName() + periods + "已于" + DateUtils.format("yyyy-MM-dd");
						String title = "项目{0}还款";
						MessageFormat.format(title, realStr);
						
						Loandynamic loanDynamic = new Loandynamic();
						loanDynamic.setLoanId(repaymentRequest.getLoanId());
						loanDynamic.setUserId(repaymentRequest.getLoginUser().getId());
						loanDynamic.setTitle(MessageFormat.format(title, realStr));
						loanDynamic.setContext(MessageFormat.format(title, realStr));
						loanDynamic.setPublishTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
						loanDynamic.setType(1);
						
						// 保存新增的一条loandynamic记录
						repayDao.saveLoanDynamic(loanDynamic);
						
						// 加息转账
						boolean payBonus = Boolean.FALSE;
						if (repaymentRequest.getRepayAction().equals("inadvanceRepay")) {	// 提前还款，总是考虑加息转账
							payBonus = Boolean.TRUE;
						} else {
							if (repaymentRequest.getRefundWay() == 1) {	// 按月
								// 最后一期才进行加息转账
								if (repaymentRequest.getRepayPeriod() == repaymentRequest.getLoanPeriods()) {
									payBonus = Boolean.TRUE;
								}
							} else if (repaymentRequest.getRefundWay() == 2) {	// 按季度
								// 最后一期才进行加息转账
								if (repaymentRequest.getRepayPeriod() == (repaymentRequest.getLoanPeriods() / 3)) {
									payBonus = Boolean.TRUE;
								}
							} else if (repaymentRequest.getRefundWay() == 3) {	// 天标还款
								payBonus = Boolean.TRUE;
							} else {
								// do nothing
							}
						}
						
						if (payBonus) {
							ipsInterestBonus(repaymentRequest.getLoanId());
						}
					}
					// 回款发送短信通知
					smsSendService.sendReturnMoneySmsNotify(investUsers, repaymentRequest.getLoan().getName());
					
					// 回款发送邮件通知
					emailSendService.sendReturnMoneyEmailNotify(investUsers, repaymentRequest.getLoan().getName());
					
					// 回款推送消息
					messagePushService.pushReturnMoneyMessage(investUsers, repaymentRequest.getLoan().getName());
				} else {
					logger.info("宝付还款受理失败！");
					
					json.element("code", 110);
					json.element("msg", "宝付还款受理失败！");
				}
			} else {
				logger.info("宝付签名校验失败！");
				
				json.element("code", 110);
				json.element("msg", "宝付还款受理失败！");
			}
		} catch (IOException e) {
			logger.error("还款转账过程中读取模板文件出错，请检查！", e);
			
			json.element("code", 103);
			json.element("msg", "系统异常！");
		} catch (TemplateException e) {
			logger.error("还款转账过程中解析模板文件出错，请检查！", e);
			
			json.element("code", 102);
			json.element("msg", "系统异常！");
		} catch (DataAccessException e) {
			logger.error("还款转账过程中操作数据库失败，请检查！", e);
			
			json.element("code", 101);
			json.element("msg", "系统异常！");
			
			throw e;
		} catch (Exception e) {
			logger.error("还款过程发生异常，请检查！", e);
			
			json.element("code", 109);
			json.element("msg", "系统异常！");
			
			throw e;
		}
		
		return json;
	}
	
	/**
	 * 收取平台服务费
	 * @param user
	 * @param loanId
	 * @param companyServiceFee
	 */
	private void ipsCompanyFee(Userbasicsinfo user, long loanId, double companyServiceFee) throws Exception {
		// 收取平台服务费转账订单号
		String orderNum = "FW" + StringUtil.getDateTime(user.getId(), loanId);
		AcctTrans acctTran = new AcctTrans();
		acctTran.setMerchant_id(ParameterIps.getCert());
		acctTran.setOrder_id(orderNum);
		acctTran.setPayer_user_id(user.getpMerBillNo());
		acctTran.setPayee_user_id(ParameterIps.getCert());	// 收款
		acctTran.setPayer_type(0);
		acctTran.setPayee_type(1);	// 收款
		acctTran.setAmount(companyServiceFee);
		acctTran.setFee(0.00);
		acctTran.setFee_taken_on(1);
		acctTran.setReq_time(new Date().getTime());
		
		try {
			String registerXml = ParseXML.accttrans(acctTran);
			
			logger.info("收取平台服务费转账的请求报文为：");
			logger.info(registerXml);
			
			ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("requestParams",registerXml));
			nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(registerXml + "~|~"+ ParameterIps.getMerchantKey())));
			payLogService.savePayLog(registerXml, user.getId(), loanId, 21, orderNum, 0.00, 0.00, companyServiceFee);
			String result = CommonUtil.excuteRequest(PayURL.TRANSFERURL, nvps);
			result = result.replace("\"", "\'");
			crs cr = new crs();
			XStream xss = new XStream(new DomDriver());
			xss.alias(cr.getClass().getSimpleName(), cr.getClass());
			cr = (crs) xss.fromXML(result);
			String Md5sign = CommonUtil.MD5(cr.getCode() + "~|~"+ cr.getMsg() + "~|~"+ ParameterIps.getDes_algorithm());
			
			if (cr.getSign().equals(Md5sign)) {
				if (cr.getCode().equals("CSD000")) {
					logger.info("向用户[" + user.getUserName() + "]收取平台服务费转账受理成功！");
					
					Accounttype serviceFeeType = repayDao.getAccounttype(17L);
					
					// 融资用户的平台服务费付款流水账
					Accountinfo serviceFeeAccount = new Accountinfo();
					serviceFeeAccount.setExpenditure(companyServiceFee);
					serviceFeeAccount.setExplan("平台收取服务费");
					serviceFeeAccount.setIncome(0.00);
					serviceFeeAccount.setIpsNumber(orderNum);
					// 标的主键id
					serviceFeeAccount.setLoansignId(String.valueOf(loanId));
					serviceFeeAccount.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
					serviceFeeAccount.setAccounttype(serviceFeeType);
					serviceFeeAccount.setUserbasicsinfo(user);
					// 保存融资用户的平台服务费付款流水账
					repayDao.saveAccount(serviceFeeAccount);
					
					logger.info("收取平台服务费转账金额为：" + companyServiceFee);
					
					payLogService.updatePayLog(orderNum, Constant.STATUES_ONE);
				} else {
					logger.info("向用户[" + user.getUserName() + "]收取平台服务费转账受理失败，请检查！");
				}
			}
		} catch (IOException e) {
			logger.error("收取平台服务费转账过程中读取模板文件出错，请检查！", e);
		} catch (TemplateException e) {
			logger.error("收取平台服务费转账过程中解析模板文件出错，请检查！", e);
		} catch (DataAccessException exception) {
			logger.error("收取平台服务费转账过程中操作数据库失败，请检查！", exception);
			throw exception;
		} catch (Exception exception) {
			logger.error("收取平台服务费转账过程中发生异常，请检查！", exception);
			throw exception;
		}
	}
	
	public void ipsInterestBonus(long loanId) throws Exception {
		List<VoteIncome> bonusList = repayDao.getInterestBonusList(loanId);
		
		if (bonusList != null && bonusList.size() > 0) {
			
			for (VoteIncome interestBonus : bonusList) {
				Userbasicsinfo user = repayDao.getUserById(interestBonus.getVoterId());
				
				// 加息转账订单号
				String orderNum = "JX" + StringUtil.getDateTime(user.getId(), Long.valueOf(loanId));
    			AcctTrans acctTran = new AcctTrans();
				acctTran.setMerchant_id(ParameterIps.getCert());
				acctTran.setOrder_id(orderNum);
				acctTran.setPayer_user_id(ParameterIps.getCert());
				acctTran.setPayee_user_id(user.getpMerBillNo());	// 收款
				acctTran.setPayer_type(1);
				acctTran.setPayee_type(0);	// 收款
				acctTran.setAmount(interestBonus.getIncomeMoney());
				acctTran.setFee(0.00);
				acctTran.setFee_taken_on(1);
				acctTran.setReq_time(new Date().getTime());
				
				try {
					String registerXml = ParseXML.accttrans(acctTran);
					
					logger.info("加息转账的请求报文为：");
					logger.info(registerXml);
					
					ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("requestParams",registerXml));
					nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(registerXml + "~|~"+ ParameterIps.getMerchantKey())));
					payLogService.savePayLog(registerXml, user.getId(), loanId, 28, orderNum, 0.00, 0.00, interestBonus.getIncomeMoney());
					String result = CommonUtil.excuteRequest(PayURL.TRANSFERURL, nvps);
					result = result.replace("\"", "\'");
					crs cr = new crs();
					XStream xss = new XStream(new DomDriver());
					xss.alias(cr.getClass().getSimpleName(), cr.getClass());
					cr = (crs) xss.fromXML(result);
					String Md5sign = CommonUtil.MD5(cr.getCode() + "~|~"+ cr.getMsg() + "~|~"+ ParameterIps.getDes_algorithm());
					
					if (cr.getSign().equals(Md5sign)) {
						if (cr.getCode().equals("CSD000")) {
							logger.info("给用户[" + user.getUserName() + "]的加息奖励操作受理成功！");
							
							Accounttype serviceFeeType = repayDao.getAccounttype(24L);
							
							// 融资用户的加息奖励流水账
							Accountinfo interestBonusAccount = new Accountinfo();
							interestBonusAccount.setExpenditure(0.00);
							interestBonusAccount.setExplan("加息奖励");
							interestBonusAccount.setIncome(interestBonus.getIncomeMoney());
							interestBonusAccount.setIpsNumber(orderNum);
							interestBonusAccount.setLoansignId(String.valueOf(loanId));	// 标的主键id
							interestBonusAccount.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
							interestBonusAccount.setUserbasicsinfo(user);
							interestBonusAccount.setAccounttype(serviceFeeType);
							interestBonusAccount.setMoney(0.00);
							interestBonusAccount.setFee(0.00);
							// 保存融资用户的加息奖励流水账
							repayDao.saveAccount(interestBonusAccount);
							
							payLogService.updatePayLog(orderNum, Constant.STATUES_ONE);
						} else {
							logger.info("给用户[" + user.getUserName() + "]的加息奖励操作受理失败，请检查！");
						}
					}
				} catch (IOException e) {
					logger.error("加息转账过程中读取模板文件出错，请检查！", e);
				} catch (TemplateException e) {
					logger.error("加息转账过程中解析模板文件出错，请检查！", e);
				} catch (DataAccessException e) {
					logger.error("加息转账过程中操作数据库失败，请检查！", e);
					throw e;
				} catch (Exception e) {
					logger.error("加息转账过程中发生异常，请检查！", e);
					throw e;
				}
			}
		} else {
			logger.info("标的[" + loanId + "]没有匹配的加息记录！");
		}
	}
}