package com.hc9.web.main.service.baofo;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.constant.PayURL;
import com.hc9.web.main.entity.Accountinfo;
import com.hc9.web.main.entity.Bonus;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Generalize;
import com.hc9.web.main.entity.InterestIncreaseCard;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Paylog;
import com.hc9.web.main.entity.Recharge;
import com.hc9.web.main.entity.RegBonus;
import com.hc9.web.main.entity.UserBank;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.VoteIncome;
import com.hc9.web.main.entity.Withdraw;
import com.hc9.web.main.redis.SmsEmailCache;
import com.hc9.web.main.redis.activity.year2016.month02.HcFinancialActivitiCache;
import com.hc9.web.main.redis.activity.year2016.month03.HcPeachActivitiCache;
import com.hc9.web.main.redis.activity.year2016.month05.HcNewerTaskCache;
import com.hc9.web.main.service.BonusService;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.GeneralizeService;
import com.hc9.web.main.service.HccoindetailService;
import com.hc9.web.main.service.InterestIncreaseCardService;
import com.hc9.web.main.service.LoanSignService;
import com.hc9.web.main.service.MemberCenterService;
import com.hc9.web.main.service.PayLogService;
import com.hc9.web.main.service.PlankService;
import com.hc9.web.main.service.ProcessingService;
import com.hc9.web.main.service.RechargesService;
import com.hc9.web.main.service.RedEnvelopeDetailService;
import com.hc9.web.main.service.UserBankService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.VoteincomeService;
import com.hc9.web.main.service.WithdrawServices;
import com.hc9.web.main.service.activity.ActivityAllInOneService;
import com.hc9.web.main.service.smsmail.SmsService;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.DateUtil;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.ParseXML;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.pay.AcctTrans;
import com.hc9.web.main.vo.pay.P2pQuery;
import com.hc9.web.main.vo.pay.RechargeInfo;
import com.hc9.web.main.vo.pay.RegisterInfo;
import com.hc9.web.main.vo.pay.WithdrawalInfo;
import com.hc9.web.main.vo.pay.crs;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

@Service
public class BaoFuLoansignService {
	
	private static final Logger logger = Logger.getLogger(BaoFuLoansignService.class);
	
	@Resource
	private HibernateSupport dao;
	
	@Resource
	private BonusService bonusService;

	@Resource
	private PlankService plankService;

	@Resource
	private PayLogService payLogService;

	@Resource
	private LoanSignService loanSignService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private BaoFuService baoFuService;

	@Resource
	private GeneralizeService generalizeService;
	
	@Resource
	private WithdrawServices withdrawServices;

	@Resource
	private ProcessingService processingservice;
	
	@Resource
	private RechargesService rechargesService;
	
	@Resource
	private UserBankService userBankService;
	
	@Resource
	private SmsService smsService;

	@Resource
	private RedEnvelopeDetailService redEnvelopeDetailService;
	
	@Resource
	private HccoindetailService hccoindetailService;
	
	@Resource
	private VoteincomeService voteincomeService;
	
	@Resource
	private InterestIncreaseCardService increaseCardService;
	
	@Resource
	private MemberCenterService memberCenterService;
	
	@Resource
	private CacheManagerService cacheManagerService;
	
	@Resource
	private HcFinancialActivitiCache hcFinancialActivitiCache;
	
	@Resource
	private BaoFuInvestService baoFuInvestService;
	
	@Resource
	private ActivityAllInOneService activityAllInOneService;
	
	
	/** 投资后更新用户相关缓存信息 */
	public void updateRedisInfoAfterInvest(Long userId, Long loanId) {
        final Long userIdFinal = userId;
        final String loanSignId = "" + loanId;
        try {
        	CacheManagerService.threadPoolExecutor.submit(
                	new Thread() {
                		public void run() {
                			LOG.error("投资更新缓存开始：" + Thread.currentThread());
                			cacheManagerService.updateIndexLoanList();//更新首页众持列表
                			cacheManagerService.updateTotalInvestMoney();//更新累计投资金额
                			memberCenterService.updateInvestStatisticInfo(userIdFinal);//投资概况
                			memberCenterService.updateBackMoneyStatisticInfo(userIdFinal);//更新用户待回款相关信息缓存
                			memberCenterService.repaymentBackList(userIdFinal);//用户的还款和回款还款信息更新
                			cacheManagerService.updateZhongChiPageLoanList();//更新我要众持列表页面
                			cacheManagerService.updateLoanDetailRelCache(loanSignId);//投资更新标详情信息
                			cacheManagerService.updateH5ZhongChiPageLoanList();//更新H5我要众持列表页面
                			cacheManagerService.updateH5HotIntroduceLoanList();//H5热门推荐列表相关缓存更新
                			LOG.error("投资更新缓存结束：" + Thread.currentThread());
                		}
                	}
                );
        	LOG.error("投资成功后更新缓存已请求处理，返回页面！");
        } catch(Exception e) {
        	LOG.error("投资成功后更新缓存出错！", e);
        }
	}
	
	/***
	 * 查询购买记录
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public String ipsLoanInfoLoanHandle(Loanrecord loanRecord ) throws Exception {
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = df.format(new Date());
		Date d1 = df.parse(date);
		Date d2 = df.parse(DateUtil.addDateMinut(loanRecord.getTenderTime(), 10));
		if (d1.getTime() < d2.getTime()) {
			return "5";
		}
		// 获得项目记录
		Loansign loan = loanSignService.getLoansignById(loanRecord.getLoansign().getId().toString());
		Accountinfo account=getAccountinfo(loanRecord.getOrder_id().trim());
		if(account!=null){
			loanRecord.setIsSucceed(Constant.STATUES_ONE);
			loanRecord.setUpdateTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
			updateLoanRecord(loanRecord);
			payLogService.updatePayLog(loanRecord.getOrder_id(),Constant.STATUES_ONE);
			return "1";
		}
		P2pQuery p2pQuery = new P2pQuery(loanRecord.getOrder_id(), 1);
		// 获取费用表的信息
		Costratio costratio = loanSignService.queryCostratio();
		InterestIncreaseCard increaseCard=increaseCardService.getLoanRecordCard(loanRecord.getId());
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		try {
			String loanXml = ParseXML.p2pQueryXml(p2pQuery);
			nvps.add(new BasicNameValuePair("requestParams", loanXml));
			nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(loanXml+ "~|~" + ParameterIps.getMerchantKey())));
			String result = CommonUtil.excuteRequest(PayURL.P2PQUERYTESTURL,nvps);
			LOG.error("项目投资业务查询=" + result);
			Document doc = DocumentHelper.parseText(result);
			Element rootElt = doc.getRootElement(); // 获取根节点
			// 拿到crs节点下的子节点code值
			String code = rootElt.elementTextTrim("code");
			String msg = rootElt.elementTextTrim("msg");
			String sign = rootElt.elementTextTrim("sign");
			// 获取子节点crs下的子节点result
			Iterator iteratorResult = rootElt.elementIterator("result");
			boolean Judge = false; // 判断是否有值
			String state = "0"; // 0-失败 1-成功
			while (iteratorResult.hasNext()) {
				Element itemEle = (Element) iteratorResult.next();
				Iterator iteratorOrder = itemEle.elementIterator("order"); // 获取子节点result下的子节点order
				while (iteratorOrder.hasNext()) {
					Element elementOrder = (Element) iteratorOrder.next();
					state = elementOrder.elementTextTrim("state");
					String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
					if (sign.equals(Md5sign)) {
						if (code.equals("CSD000")) {
							Judge = true;
							if (state.equals("1")) {
								loanRecord.setIsSucceed(Constant.STATUES_ONE);
								loanRecord.setUpdateTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
								// 投资按100元计1分
								Integer product = (int) (loanRecord.getTenderMoney() / 100);
								plankService.saveAutointegralBuyProject(loanRecord.getUserbasicsinfo(),loanRecord.getTenderMoney(),loanRecord.getSubType()); // 保存积分记录
								// 余额查询
								crs cr = baoFuService.getCasbalance(loanRecord.getUserbasicsinfo().getpMerBillNo());
								loanRecord.getUserbasicsinfo().getUserfundinfo().setCashBalance(cr.getBalance()); // 宝付的余额
								loanRecord.getUserbasicsinfo().getUserfundinfo().setOperationMoney(cr.getBalance()); // 宝付的余额
								loanRecord.getUserbasicsinfo().setUserintegral(loanRecord.getUserbasicsinfo().getUserintegral() + product); // 积分计算

								if(account==null){
									// 银行流水
								     account = new Accountinfo();
								     if(loanRecord.getRedEnvelopeMoney()>0){
								    	     account.setExpenditure(Arith.sub(loanRecord.getTenderMoney(), loanRecord.getRedEnvelopeMoney()));
								     }else{
								    		account.setExpenditure(loanRecord.getTenderMoney());
								     }
									if (loanRecord.getLoanType() == 2) {
										account.setExplan("项目购买");
									} else if (loanRecord.getLoanType() == 3) {
										account.setExplan("天标购买");
									}
									account.setIncome(0.00);
									account.setIpsNumber(loanRecord.getOrder_id());
									account.setLoansignId(loan.getId().toString());
									account.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
									account.setUserbasicsinfo(loanRecord.getUserbasicsinfo());
									if (loanRecord.getLoanType() == 2) {
										account.setAccounttype(plankService.accounttype(5L));
									} else if (loanRecord.getLoanType() == 3) {
										account.setAccounttype(plankService.accounttype(15L));
									}
									account.setMoney(cr.getBalance());// 流水记录表
								}
								
								boolean isDetail = false;
								//更新红包
								if(loanRecord.getRedEnvelopeMoney() > 0){
									isDetail = true;
									loan.setRedEnvelopeMoney(Arith.add(loan.getRedEnvelopeMoney(), loanRecord.getRedEnvelopeMoney()));
									redEnvelopeDetailService.updateRed(loanRecord, Constant.STATUES_ONE);
								}
								// 判断是否融资成功
								Double tendMoney = loanSignService.getSumLoanTenderMoney(loan.getId().toString());
								Double subMoney = Arith.sub(loan.getIssueLoan(), tendMoney);
								boolean sendFullSmsFlag = false;//是否发送满标短信
								if (subMoney == 0) {
									loan.setStatus(2); // 融资成功
									loan.setFullTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
									sendFullSmsFlag = true;
								}
								dao.update(loanRecord);
								dao.save(account);
								dao.update(loan);
							    dao.saveOrUpdate(loanRecord.getUserbasicsinfo());
								
								Userbasicsinfo userGen = generalizeService.queryPromoterByPromotedId(loanRecord.getUserbasicsinfo().getId());
								
								//lkl-20150811-添加员工推荐投资红筹币
								if(userGen!=null){
									 if (userGen.getUserType() == 2) {
										   hccoindetailService.saveHccoindetail(loanRecord, costratio.getHccoinRate(), userGen);
									}
								}
								boolean isCardId = false;
								//查询加息劵-lkl-20150825
								if(increaseCard != null){
									isCardId = true;
									increaseCard.setUseFlag(Constant.STATUES_ONE);
									increaseCard.setConsumeTime(loanRecord.getTenderTime());
									increaseCardService.uptIncreaseCard(increaseCard);
								}
								// 保存佣金
								if(userGen!=null){
									 if (userGen.getUserType() == 2) {
										 generalizeService.saveGeneralizemoney(loanRecord,costratio.getBusiness(), userGen.getId(),Constant.STATUES_TWO);
									 }else if(userGen.getUserType() == 4){
										 generalizeService.saveGeneralizemoney(loanRecord,0.015, userGen.getId(),Constant.STATUES_TWO);
									 }else if(userGen.getUserType() == 6){
										 if(userGen.getIsAuthIps() == 1 && loanRecord.getUserbasicsinfo().getIsAuthIps() == 1){
											 // 理财师
											 generalizeService.saveGeneralizemoney(loanRecord,costratio.getFinancial(), userGen.getId(),Constant.STATUES_SIX);
											 financial(userGen,loanRecord.getTenderMoney());
										 }
									 }
								}
								if(loanRecord.getUserbasicsinfo().getUserType()==2){
									generalizeService.saveGeneralizemoney(loanRecord,costratio.getBusiness(), loanRecord.getUserbasicsinfo().getId(),Constant.STATUES_TWO);
								}else if(loanRecord.getUserbasicsinfo().getUserType()==4){
									generalizeService.saveGeneralizemoney(loanRecord,0.015, loanRecord.getUserbasicsinfo().getId(),Constant.STATUES_TWO);
								}
								
								// 判断是否融资成功
								if (sendFullSmsFlag) {
									Map<String, String> map = new HashMap<String, String>();
									map.put("loanNum", loan.getName());
									String content = smsService.getSmsResources("check-fullBid.ftl", map);
									int trigger=Integer.valueOf(SmsEmailCache.getSmsTriggerChannel());
									String[] phones=costratio.getBidPhone().split(",");
									for(int i=0;i<phones.length;i++){
										smsService.chooseSmsChannel(trigger, content, phones[i]);
									}
								}
								
								/** 投资成功给投资人发送短信 */
	                			baoFuInvestService.sendSmsToInvestorAfterSuccess(loanRecord.getUserbasicsinfo(), loan, loanRecord);
								// 更新支付报文信息
								payLogService.updatePayLog(loanRecord.getOrder_id(),Constant.STATUES_ONE);
								LOG.error("宝付项目投资查询处理成功--" + result+ "----->订单号----->"+ loanRecord.getOrder_id());
								
								/** 活动相关逻辑 */
								activityAllInOneService.activityBusinessLogicForInvestQuery(loan, loanRecord, 
										isDetail, isCardId);
							} else if (state.equals("0")) {
								// 剩余金额
								loan.setRestMoney(Arith.add(loan.getRestMoney(),loanRecord.getTenderMoney()));
								if (loanRecord.getSubType() == 1) { // 优先
									if (loanRecord.getIsType() == 0) { // 默认
										loan.setPrioRestMoney(Arith.add(loan.getPrioRestMoney(),loanRecord.getTenderMoney()));
									} else if (loanRecord.getIsType() == 2) { // 夹层转优先
										Double money = Arith.sub(loanRecord.getTenderMoney(),	loanRecord.getSubMoney()); // 购买金额-差额=优先剩余金额
										loan.setMiddle(Arith.add(loan.getMiddle(),loanRecord.getSubMoney())); // 夹层总额+差额
										loan.setMidRestMoney(Arith.add(loan.getMidRestMoney(),loanRecord.getSubMoney())); // 夹层剩余金额+差额
										loan.setPriority(Arith.sub(loan.getPriority(),loanRecord.getSubMoney())); // 优先总额-差额
										loan.setPrioRestMoney(Arith.add(loan.getPrioRestMoney(), money)); // 优先剩余金额
									}
								} else if (loanRecord.getSubType() == 2) { // 夹层
									if (loanRecord.getIsType() == 0) {
										loan.setMidRestMoney(Arith.add(loan.getMidRestMoney(),loanRecord.getTenderMoney()));
									} else if (loanRecord.getIsType() == 1) { // 优先转夹层
										Double money = Arith.sub(loanRecord.getTenderMoney(),loanRecord.getSubMoney()); // 购买金额-差额=夹层剩余金额
										loan.setPriority(Arith.add(loan.getPriority(),loanRecord.getSubMoney())); // 优先总金额+差额
										loan.setPrioRestMoney(Arith.add(loan.getPrioRestMoney(),loanRecord.getSubMoney())); // 优先剩余总金额+差额
										loan.setMiddle(Arith.sub(loan.getMiddle(),loanRecord.getSubMoney())); // 夹层总额-差额
										loan.setMidRestMoney(Arith.add(loan.getMidRestMoney(), money)); // 夹层剩余金额
									}
								} else if (loanRecord.getSubType() == 3) { // 劣后
									loan.setAfterRestMoney(Arith.add(loan.getAfterRestMoney(),loanRecord.getTenderMoney()));
								}
								loanRecord.setIsSucceed(-1);
								loanRecord.setUpdateTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
								if(loanRecord.getRedEnvelopeMoney()>0){
									//更新红包
									redEnvelopeDetailService.updateRed(loanRecord, Constant.STATUES_ZERO);
								}
								
								//更新加息劵-lkl-20150825
								if(increaseCard!=null){
									 increaseCard.setUseFlag(Constant.STATUES_ZERO);
									 increaseCard.setLoanrecordId(null);
									 increaseCardService.uptIncreaseCard(increaseCard);
								}
								dao.update(loanRecord);
								dao.update(loan);
								// 更新支付报文信息
								payLogService.updatePayLog(loanRecord.getOrder_id(), -1);
								LOG.error("宝付项目购买处理失败");
							}
						} else if (code.equals("CSD333")) {
							// 更新支付报文信息
							return "-1";
						} else {
							LOG.error("宝付项目投资查询处理失败--" + result+ "----->订单号----->"+ loanRecord.getOrder_id());
							return "4";
						}
					} else {
						LOG.error("非宝付项目投资查询返回数据--" + result+ "----->订单号----->" + loanRecord.getOrder_id());
						return "3";
					}
				}
			}
			if (!Judge && state.equals("0")) {
				if (loanRecord.getIsSucceed() != -1) {
					// 剩余金额
					loan.setRestMoney(Arith.add(loan.getRestMoney(),loanRecord.getTenderMoney()));
					if (loanRecord.getSubType() == 1) { // 优先
						if (loanRecord.getIsType() == 0) { // 默认
							loan.setPrioRestMoney(Arith.add(loan.getPrioRestMoney(),loanRecord.getTenderMoney()));
						} else if (loanRecord.getIsType() == 2) { // 夹层转优先
							Double money = Arith.sub(loanRecord.getTenderMoney(),loanRecord.getSubMoney()); // 购买金额-差额=优先剩余金额
							loan.setMiddle(Arith.add(loan.getMiddle(),loanRecord.getSubMoney())); // 夹层总额+差额
							loan.setMidRestMoney(Arith.add(loan.getMidRestMoney(),loanRecord.getSubMoney())); // 夹层剩余金额+差额
							loan.setPriority(Arith.sub(loan.getPriority(),loanRecord.getSubMoney())); // 优先总额-差额
							loan.setPrioRestMoney(Arith.add(loan.getPrioRestMoney(), money)); // 优先剩余金额
						}
					} else if (loanRecord.getSubType() == 2) { // 夹层
						if (loanRecord.getIsType() == 0) {
							loan.setMidRestMoney(Arith.add(loan.getMidRestMoney(),loanRecord.getTenderMoney()));
						} else if (loanRecord.getIsType() == 1) { // 优先转夹层
							Double money = Arith.sub(loanRecord.getTenderMoney(),loanRecord.getSubMoney()); // 购买金额-差额=夹层剩余金额
							loan.setPriority(Arith.add(loan.getPriority(),loanRecord.getSubMoney())); // 优先总金额+差额
							loan.setPrioRestMoney(Arith.add(loan.getPrioRestMoney(),loanRecord.getSubMoney())); // 优先剩余总金额+差额
							loan.setMiddle(Arith.sub(loan.getMiddle(),loanRecord.getSubMoney())); // 夹层总额-差额
							loan.setMidRestMoney(Arith.add(loan.getMidRestMoney(), money)); // 夹层剩余金额
						}
					} else if (loanRecord.getSubType() == 3) { // 劣后
						loan.setAfterRestMoney(Arith.add(loan.getAfterRestMoney(),loanRecord.getTenderMoney()));
					}
					loanRecord.setIsSucceed(-1);
					loanRecord.setUpdateTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
					if(loanRecord.getRedEnvelopeMoney()>0){
						//更新红包
						redEnvelopeDetailService.updateRed(loanRecord, Constant.STATUES_ZERO);
					}
					//更新加息劵-lkl-20150825
					if(increaseCard!=null){
						 increaseCard.setUseFlag(Constant.STATUES_ZERO);
						 increaseCard.setLoanrecordId(null);
						 increaseCardService.uptIncreaseCard(increaseCard);
					}
					
					loanSignService.updateLoanRecord(loanRecord, loan);
					// 更新支付报文信息
					payLogService.updatePayLog(loanRecord.getOrder_id(), -1);
					LOG.error("宝付项目购买处理失败");
				}
				return "-1";
			}
			return "1";
		} catch (Exception e) {
			LOG.error("宝付项目投资查询失败----->订单号----->" + loanRecord.getOrder_id()+" 错误： "+e);
			return "2";
		}
	}
	
	/** 理财师活动 **/
	private void financial(Userbasicsinfo user, Double priority) {
		// 现金奖励
		if(HcFinancialActivitiCache.validCurrentDate(new Date()) >= 0) {
			try {
				hcFinancialActivitiCache.activityFinancial(user, priority);
			} catch (Exception e) {
				LOG.error("理财师活动异常----->",e);
			}
		}
	}
	
	/** 判断用户是否在活动期间注册 */
	public boolean isUserRegisterdInActivityTimeArea(Long userId, String beginTime, String endTime){
		String sql="select * from userbasicsinfo where  id=? and DATE_FORMAT(createTime, '%Y-%m-%d')>=DATE_FORMAT('" 
			+ beginTime + "', '%Y-%m-%d')  AND DATE_FORMAT(createTime, '%Y-%m-%d')<=DATE_FORMAT('" 
			+ endTime + "', '%Y-%m-%d') ";
		List<Userbasicsinfo> list = dao.findBySql(sql, Userbasicsinfo.class, userId);
		if(list.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/***
	 * 判断是否第一次投资，新增bonus奖励数据
	 * @param loanrecord
	 */
	public  void saveBonus(Loanrecord loanrecord){
		  boolean isTrue=getLoanRecord(loanrecord.getUserbasicsinfo().getId());
		  if(isTrue){
			     //得到推荐人
			    Userbasicsinfo user=generalizeService.queryPromoterByPromotedId(loanrecord.getUserbasicsinfo().getId());
			 	Bonus bonus=new Bonus();
				bonus.setUserId(loanrecord.getUserbasicsinfo().getId());
				bonus.setUserName(loanrecord.getUserbasicsinfo().getName());
				Double bonusMoney=0.00;  //被推荐人投资奖励
				Double genBonusMoney=0.00;//推荐人投资奖励
				if(loanrecord.getTenderMoney()>=100&&loanrecord.getTenderMoney()<1000){
					bonusMoney=5.00;
				}else if(loanrecord.getTenderMoney()>=1000&&loanrecord.getTenderMoney()<5000){
					bonusMoney=10.00;
					genBonusMoney=5.00;
				}else if(loanrecord.getTenderMoney()>=5000&&loanrecord.getTenderMoney()<10000){
					bonusMoney=25.00;
					genBonusMoney=15.00;
				}else if(loanrecord.getTenderMoney()>=10000&&loanrecord.getTenderMoney()<30000){
					bonusMoney=50.00;
					genBonusMoney=25.00;
				}else if(loanrecord.getTenderMoney()>=30000){
					bonusMoney=100.00;
					genBonusMoney=50.00;
				}
				bonus.setUserAmount(bonusMoney);
				bonus.setUserPhone(loanrecord.getUserbasicsinfo().getUserrelationinfo().getPhone());
				bonus.setUserState(Constant.STATUES_ZERO);
				bonus.setTenderMoney(loanrecord.getTenderMoney());
				bonus.setTenderTime(loanrecord.getTenderTime());
				if(user!=null){
					bonus.setGenUserId(user.getId());
					bonus.setGenUserName(user.getName());
					bonus.setGenUserAmount(genBonusMoney);
					if(genBonusMoney==0){
						//奖励为0
						bonus.setGenUserState(Constant.STATUES_TWO);
					}else{
						bonus.setGenUserState(Constant.STATUES_ZERO);
					}
				}else{
					//无推荐人
					bonus.setGenUserState(Constant.STATUES_THERE);
				}
				dao.save(bonus);
		  }
		  
	}
	
	/***
	 * 添加红包奖励
	 * isRed-是否使用红包 true不使用 false 使用
	 * 1、首次注册投资 必须注册时间在活动期间，投资人与推荐人才同时送红包
	 * 2、单笔投资如果已使用红包则不再赠送红包
	 * @param loanrecord
	 */
	public  void saveRedenvelopedetail(Loanrecord loanrecord,boolean isRed,Costratio costratio){
		  //是否首次注册
		  boolean isTrue=getLoanRecord(loanrecord.getUserbasicsinfo().getId());
		  if(isTrue){
			    boolean userRed=redEnvelopeDetailService.getRedEnvelopeDetails(loanrecord.getUserbasicsinfo().getId(),costratio);
			    if(userRed){
			    	//得到推荐人
					  Userbasicsinfo user=generalizeService.queryPromoterByPromotedId(loanrecord.getUserbasicsinfo().getId());
					  //首次投资红包
					  redEnvelopeDetailService.saveRedEnvelopeDetail(loanrecord, loanrecord.getUserbasicsinfo(), Constant.STATUES_ONE);
					  //推荐人红包
					  if(user!=null){
						  redEnvelopeDetailService.saveRedEnvelopeDetail(loanrecord, user, Constant.STATUES_THERE);
					  }
			    }
		  }else{
			  if(isRed){
				  boolean loanRecordRed=redEnvelopeDetailService.getUserLoanRecordRed(loanrecord.getId(), costratio);
				  if(loanRecordRed){
					  redEnvelopeDetailService.saveRedEnvelopeDetail(loanrecord, loanrecord.getUserbasicsinfo(), Constant.STATUES_ONE);
				  }
			  }
		  }
	}
	
	/***
	 * 判断是否第一次投资，新增bonus奖励数据
	 * @param loanrecord
	 */
	public  void saveUserBonus(Loanrecord loanrecord){
		  boolean isTrue=getLoanRecord(loanrecord.getUserbasicsinfo().getId());
		  if(isTrue){
			  boolean isBonus=getUserBonus(loanrecord.getUserbasicsinfo().getId());
			     //得到推荐人
			    Userbasicsinfo user=generalizeService.queryPromoterByPromotedId(loanrecord.getUserbasicsinfo().getId());
			 	Bonus bonus=new Bonus();
				bonus.setUserId(loanrecord.getUserbasicsinfo().getId());
				bonus.setUserName(loanrecord.getUserbasicsinfo().getName());
				bonus.setUserPhone(loanrecord.getUserbasicsinfo().getUserrelationinfo().getPhone());
				bonus.setUserState(Constant.STATUES_ZERO);
				bonus.setTenderMoney(loanrecord.getTenderMoney());
				bonus.setTenderTime(loanrecord.getTenderTime());
				Double bonusMoney=20.00;  //被推荐人投资奖励
				Double genBonusMoney=10.00;//推荐人投资奖励
				bonus.setUserAmount(bonusMoney);
				if(isBonus){
					if(user!=null){
						bonus.setGenUserId(user.getId());
						bonus.setGenUserName(user.getName());
						bonus.setGenUserAmount(genBonusMoney);
						bonus.setGenUserState(Constant.STATUES_ZERO);
					}else{
						//无推荐人
						bonus.setGenUserState(Constant.STATUES_THERE);
						bonus.setGenUserAmount(0.00);
					}
				}else{
					if(user!=null){
						bonus.setGenUserId(user.getId());
						bonus.setGenUserName(user.getName());
					}
					bonus.setGenUserState(Constant.STATUES_TWO);
					bonus.setGenUserAmount(0.00);
				}
				dao.save(bonus);
		  }
	}
	
	/***
	 * 判断购买用户是否在在活动期间
	 * @param userId
	 * @return
	 */
	public boolean getUserBonus(Long userId){
		  String sql="select * from userbasicsinfo where  id=? and DATE_FORMAT(createTime, '%Y-%m-%d')>=DATE_FORMAT('20150623', '%Y-%m-%d')  AND DATE_FORMAT(createTime, '%Y-%m-%d')<=DATE_FORMAT('20150731', '%Y-%m-%d') ";
	      List<Userbasicsinfo> list=dao.findBySql(sql, Userbasicsinfo.class, userId);
		  	if(list.size()>0){
				return true;
			}else{
				return false;
			}
	}
	
	/***
	 * 判断是否已购买过
	 * @param userId
	 * @return
	 */
	public boolean getLoanRecord(Long userId){
		String sql="select * from loanrecord where userbasicinfo_id=? and isSucceed=1";
		List<Loanrecord> listLoanrecord=dao.findBySql(sql, Loanrecord.class, userId);
		if(listLoanrecord.size()==1){
			return true;
		}else{
			return false;
		}
	}
	
	/***
	 * 公共处理提现业务查询
	 * @param request
	 * @param wId
	 * @return
	 */
	public String returnWithdrawNumService(HttpServletRequest request, String wId){
		Withdraw withdraw = withdrawServices.selWithdraw(wId);
		P2pQuery p2pQuery = new P2pQuery(withdraw.getStrNum(), 6);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		try {
			String withdrawXml = ParseXML.p2pQueryXml(p2pQuery);
			nvps.add(new BasicNameValuePair("requestParams", withdrawXml));
			nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(withdrawXml+ "~|~" + ParameterIps.getMerchantKey())));
			String result = CommonUtil.excuteRequest(PayURL.P2PQUERYTESTURL,nvps);
			System.out.println("提现业务查询=" + result);
			Document doc = DocumentHelper.parseText(result);
			Element rootElt = doc.getRootElement(); // 获取根节点
			System.out.println("根节点：" + rootElt.getName()); // 拿到根节点的名称
			// 拿到crs节点下的子节点code值
			String code = rootElt.elementTextTrim("code");
			String msg = rootElt.elementTextTrim("msg");
			String sign = rootElt.elementTextTrim("sign");
			// 获取子节点crs下的子节点result
			Iterator iteratorResult = rootElt.elementIterator("result");
			boolean Judge = false; // 判断是否有值
			String state = "-1";
			// 遍历result节点下的Response节点
			while (iteratorResult.hasNext()) {
				Element itemEle = (Element) iteratorResult.next();
				Iterator iteratorOrder = itemEle.elementIterator("order"); // 获取子节点result下的子节点order
				while (iteratorOrder.hasNext()) {
					Element elementOrder = (Element) iteratorOrder.next();
					String order_id = elementOrder.elementTextTrim("order_id");
					state = elementOrder.elementTextTrim("state");
					String succ_amount = elementOrder.elementTextTrim("succ_amount");
					String succ_time = elementOrder.elementTextTrim("succ_time");
					String fee = elementOrder.elementTextTrim("fee");
					String baofoo_fee = elementOrder.elementTextTrim("baofoo_fee");
					String fee_taken_on = elementOrder.elementTextTrim("fee_taken_on");
					String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
					Double addFee=0.00;
					if (sign.equals(Md5sign)) {
						if (code.equals("CSD000")) {
							if (state.equals("1")) { // 转账成功
								withdraw.setState(Constant.STATUES_ONE);
								withdraw.setFee(Double.valueOf(baofoo_fee) != null ? Double.valueOf(baofoo_fee) :0.00);
								withdraw.setMer_fee(Double.valueOf(fee) != null ? Double.valueOf(fee) : 0.00);
								if(succ_amount==null){
									succ_amount="0";
								}
								withdraw.setWithdrawAmount(Double.valueOf(succ_amount) != null ? Double.valueOf(succ_amount)  : 0.00);
								withdraw.setApplytime(succ_time);
								withdraw.setCode(code);
								addFee=Arith.add(withdraw.getFee(),withdraw.getMer_fee());
								//withdraw.setFee_taken_on(Integer.valueOf(fee_taken_on));
								withdrawServices.updateWithdrawCardStatusAfterSuccess(
										withdraw.getUserbasicsinfo().getId(), withdraw.getStrNum());
							} else if (state.equals("5")) { // 转账处理中
								withdraw.setState(Constant.STATUES_FIVE);
							} else if (state.equals("0")) { // 初始化
								withdraw.setState(Constant.STATUES_ZERO);
							}
							if (state.equals("1")) {
								Userbasicsinfo obj = userbasicsinfoService.queryUserById(withdraw.getUserbasicsinfo().getId());
								// 余额查询
								crs cr = baoFuService.getCasbalance(obj.getpMerBillNo());
								obj.getUserfundinfo().setCashBalance(cr.getBalance());
								obj.getUserfundinfo().setOperationMoney(cr.getBalance());
								// 流水账
								Accountinfo account = new Accountinfo();
								account.setExpenditure(Double.valueOf(succ_amount));
								account.setExplan("提现");
								account.setIncome(0.00);
								account.setIpsNumber(order_id);
								account.setTime(succ_time);
								account.setUserbasicsinfo(obj);
								account.setAccounttype(plankService.accounttype(7L));
								account.setMoney(cr.getBalance());
								account.setFee(addFee);
								plankService.saveAccount(account);// 保存流水账余额
								userbasicsinfoService.update(obj);
								Judge = true;
							}
							// 更新支付报文信息
							payLogService.updatePayLog(order_id,Integer.valueOf(state),withdraw.getWithdrawAmount(),addFee);

							withdrawServices.uptWithdraw(withdraw);
							LOG.error("宝付支付提现查询处理成功");
						} else {
							LOG.error("宝付提现查询处理失败--" + result
									+ "----->订单号----->" + withdraw.getStrNum());
							return "0"; // 查询失败
						} 
					} else {
						LOG.error("非宝付提现查询返回数据--" + result + "----->订单号----->"
								+ withdraw.getStrNum());
						return "0"; // 查询失败
					}
				}
			}
			if (!Judge && state.equals("-1")) {
				if (withdraw.getState() != -1) {
					withdraw.setFee(0.00);
					withdraw.setState(-1);
					if(withdraw.getFeeState()==1){
						withdraw.getUserbasicsinfo().getUserfundinfo().setWithdrawMoney(Arith.sub(withdraw.getUserbasicsinfo().getUserfundinfo().getWithdrawMoney(), withdraw.getAmount()));
					}
					withdrawServices.uptWithdraw(withdraw);
					// 更新支付报文信息
					payLogService.updatePayLog(withdraw.getStrNum(),Integer.valueOf(state),withdraw.getWithdrawAmount(), withdraw.getFee());
					withdrawServices.updateWithdrawCardStatusAfterFailure(
										withdraw.getUserbasicsinfo().getId(), withdraw.getStrNum());
				}
			}
			return "1"; // 提现成功
		} catch (Exception e) {
			LOG.error("宝付提现查询失败----->订单号----->" + withdraw.getStrNum(),e);
			return "0"; // 查询失败
		}
	}
	
	/***
	 * 宝付提现处理
	 * @param request
	 * @param money
	 * @return
	 */
	public String ipsWithdrawService(HttpServletRequest request, Double money, Long withdrawCardId) {
		// 得到当前用户信息
		Userbasicsinfo userbasics = userbasicsinfoService.queryUserById(((Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER)).getId());
		long userId = userbasics.getId();
		Costratio costratio = loanSignService.queryCostratio();
		Withdraw withdraw = new Withdraw();
		String ordernum = "TX" + StringUtil.getDateTime(userbasics.getId());
		// 手续费
		double fee=0;
		//默认不收取
		withdraw.setFeeState(Constant.STATUES_TWO);
		fee=withdrawServices.takeWithdrawFee(userbasics, money);
		if(fee>0){
			withdraw.setFeeState(Constant.STATUES_ONE);
		}
		withdraw.setStrNum(ordernum);
		withdraw.setAmount(money);
		withdraw.setUserbasicsinfo(userbasics);
		withdraw.setMer_fee(fee);
		withdraw.setState(Constant.STATUES_ZERO);
		/** 提现IPS手续费收取方式 1-平台支付 2-用户支付 */
		int fee_taken_on = costratio.getWithdrawMethod();
		if(fee_taken_on == 2) {
			if(isWithdrawCardValid(userId, "" + withdrawCardId)) {
				fee_taken_on = 1;
			}
		}
		withdraw.setFee_taken_on(fee_taken_on);
		if(HcPeachActivitiCache.validCurrentDate(new Date()) >= 0 && userbasics.getFee() == 0){
			withdraw.setFee_taken_on(1);
		}
		withdraw.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		dao.save(withdraw);

		updateWithdrawCardStatus(userId, "" + withdrawCardId, ordernum);
		
		WithdrawalInfo withrawlInfo = new WithdrawalInfo();
		withrawlInfo.setOrder_id(ordernum);
		withrawlInfo.setUser_id(userbasics.getpMerBillNo());
		withrawlInfo.setAmount(money);
		withrawlInfo.setFee(fee);
		withrawlInfo.setFee_taken_on("" + withdraw.getFee_taken_on());
		if(HcPeachActivitiCache.validCurrentDate(new Date()) >= 0 && userbasics.getFee() == 0){
			withrawlInfo.setFee_taken_on("1");
		}
		withrawlInfo.setMerchant_id(ParameterIps.getCert());
		Map<String, String> map = null;
		try {
			String registerXml = ParseXML.withdrawalXml(withrawlInfo);
			
			logger.debug("registerXml");
			logger.debug(registerXml);
			
			payLogService.savePayLog(registerXml, userbasics.getId(), 3, ordernum, 0,fee, 0.00, money);
			map = RechargeInfoService.withdrawalCall(registerXml,ParameterIps.getMerchantKey());
			map.put("url", PayURL.WITHDRAWALTESTURL);
			request.getSession().setAttribute("map", map);
			return "1";
		} catch (Exception e) {
			LOG.error("宝付提现处理失败",e);
			return "2";
		}
	}
	
	/** 判断用户提现券是否有效 */
	public boolean isWithdrawCardValid(Long userId, String withdrawCardId) {
		boolean result = false;
		String nowDate = DateFormatUtil.dateToString(new Date(), "yyyy-MM-dd");
		String sql = "select * from withdrawcard where useFlag=0 and " + 
				"userId=? and id=? and beginTime<=? and endTime>=?";
		List list = dao.findBySql(sql, userId, withdrawCardId, nowDate, nowDate);
		if(list != null && list.size() > 0) {
			result = true;
		}
		return result;
	}
	
	/** 修改提现券的状态为待确认 */
	public void updateWithdrawCardStatus(Long userId, String withdrawCardId, String ordernum) {
		String consumeTime = DateFormatUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
		String sql = "update withdrawcard set useFlag=2,useOrderNo=?,consumeTime=? "
				+ "where userId=? and id=? and useFlag=0";
		dao.executeSql(sql, ordernum, consumeTime, userId, withdrawCardId);
	}
	
	/***
	 * 宝付充值查询
	 * @param request
	 * @param rId
	 * @param no
	 * @return
	 * @throws Exception
	 */
	public  String ipsRechargeNumService(HttpServletRequest request,String rId,Integer no)throws Exception{
		Recharge  recharge=rechargesService.selRecharge(rId);
	     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     String date=df.format(new Date());
		 Date d1 = df.parse(date);
		 Date d2=df.parse(DateUtil.addDateMinut(recharge.getTime(), 10));
	     if(d1.getTime()<d2.getTime()){
	    	 return "redirect:/recharge/rechargeRecord.htm?no="+no;
	     }
		P2pQuery p2pQuery=new  P2pQuery(recharge.getOrderNum(), 5);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		try{
				String withdrawXml = ParseXML.p2pQueryXml(p2pQuery);
		    	nvps.add(new BasicNameValuePair("requestParams", withdrawXml));
		    	nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(withdrawXml + "~|~" + ParameterIps.getMerchantKey())));
		    	String result = CommonUtil.excuteRequest(PayURL.P2PQUERYTESTURL, nvps);
		    	LOG.error("充值业务查询="+result);
		    	Document doc = DocumentHelper.parseText(result);
		    	Element rootElt = doc.getRootElement(); // 获取根节点
	            // 拿到crs节点下的子节点code值
	            String code = rootElt.elementTextTrim("code"); 
	            String msg =  rootElt.elementTextTrim("msg"); 
	            String sign =  rootElt.elementTextTrim("sign"); 
	            // 获取子节点crs下的子节点result
	            Iterator iteratorResult = rootElt.elementIterator("result"); 
	            boolean Judge=false;  //判断是否有值
	            String state="0";
	            // 遍历result节点下的Response节点
                while (iteratorResult.hasNext()) {
                    Element itemEle = (Element) iteratorResult.next();
                    Iterator iteratorOrder = itemEle.elementIterator("order"); // 获取子节点result下的子节点order
                    while (iteratorOrder.hasNext()) {
	                        Element elementOrder = (Element) iteratorOrder.next();
	                        String order_id = elementOrder.elementTextTrim("order_id"); 
	                        state = elementOrder.elementTextTrim("state");  //1-成功  0-处理中
	                        String succ_amount=elementOrder.elementTextTrim("succ_amount");
	                        String succ_time=elementOrder.elementTextTrim("succ_time");
	                        String fee=elementOrder.elementTextTrim("fee");
	                        String baofoo_fee=elementOrder.elementTextTrim("baofoo_fee");
	                        String fee_taken_on=elementOrder.elementTextTrim("fee_taken_on");
	                        String Md5sign = CommonUtil.MD5(code+ "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
	        				if (sign.equals(Md5sign)) {
	        					   if(code.equals("CSD000")){
										if(state.equals("1")){
		        						    recharge.setFee(Double.valueOf(baofoo_fee)); // 宝付收取费用
			        						recharge.setMer_fee(Double.valueOf(fee)); // 商户收取的手续费
			        						recharge.setIncash_money(Arith.sub(Double.valueOf(succ_amount), Double.valueOf(baofoo_fee))); // 充值结算金额(实际到账)
			        						recharge.setCode(code);
			        						recharge.setStatus(Integer.parseInt(state));
			        						recharge.setSuccTime(succ_time);
			        						recharge.setFee_taken_on(Integer.valueOf(fee_taken_on));
											processingservice.updaterecharge(recharge);
											Userbasicsinfo user = userbasicsinfoService.queryUserById(recharge.getUserbasicsinfo().getId());
											// 流水账
											Accountinfo account = new Accountinfo();
											account.setExpenditure(0.00);
											account.setExplan("充值");
											account.setIncome(Double.valueOf(succ_amount));
											account.setIpsNumber(order_id);
											account.setTime(succ_time);
											account.setUserbasicsinfo(user);
											account.setAccounttype(plankService.accounttype(6L));
											account.setFee(recharge.getFee());
											// 余额查询
											crs cr = baoFuService.getCasbalance(user.getpMerBillNo());
											user.getUserfundinfo().setCashBalance(cr.getBalance());
											user.getUserfundinfo().setOperationMoney(cr.getBalance());
											account.setMoney(cr.getBalance());
											userbasicsinfoService.update(user);
											plankService.saveAccount(account);// 保存流水账余额
											// 更新支付报文信息
											payLogService.updatePayLog(recharge.getOrderNum(),Integer.parseInt(state),recharge.getIncash_money(), recharge.getFee());
											Judge=true;
											LOG.error("宝付充值查询处理成功");
											long userId = recharge.getUserbasicsinfo().getId();
											HcNewerTaskCache.giveFirstRechargeRedenvelopeKey(userId);
										}
	        					   }
	        				}else{
	        					LOG.error("非宝付充值查询返回数据--" +result+ "----->订单号----->"+ recharge.getOrderNum());
	        					return "-1";
	        				}
	                 }
	            }
                if(!Judge&&state.equals("0")){
					   recharge.setStatus(-1);
					   recharge.setFee(0.00);
					   processingservice.updaterecharge(recharge);
					   payLogService.updatePayLog(recharge.getOrderNum(),-1,recharge.getIncash_money(), recharge.getFee());
                }
                return "redirect:/recharge/rechargeRecord.htm?no="+no;
	    }catch (Exception e) {
			  LOG.error("宝付充值查询失败----->订单号----->" +  recharge.getOrderNum(),e);
			 return "-2";
		 }
	}
	
	/***
	 * 充值处理方法
	 * @param request
	 * @param amount
	 * @param additional_info
	 * @return
	 */
	public String ipsRechargeService(HttpServletRequest request, Double amount,String additional_info) {
		// 得到当前用户信息
		Userbasicsinfo userbasics = userbasicsinfoService.queryUserById(((Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER)).getId());
		if(userbasics.getpMerBillNo().equals("")||userbasics.getpMerBillNo()==null){
			return "3";
		}
		Costratio costratio = loanSignService.queryCostratio();
		String ordernum = "CZ" +  StringUtil.getDateTime(userbasics.getId());
		Double fee=Arith.mul(amount, costratio.getRecharge()) ;
		RechargeInfo rechargeInfo = new RechargeInfo();
		rechargeInfo.setMerchant_id(ParameterIps.getCert());
		rechargeInfo.setAmount(amount);
		rechargeInfo.setUser_id(userbasics.getpMerBillNo());
		rechargeInfo.setFee_taken_on(costratio.getRechargeMethod().toString());
		rechargeInfo.setFee(fee);
		rechargeInfo.setAdditional_info(additional_info);
		rechargeInfo.setOrder_id(ordernum);
		
		//生成初始化充值记录
		Recharge recharge =new Recharge();
		recharge.setRechargeAmount(amount);
		recharge.setUserbasicsinfo(userbasics);
		recharge.setOrderNum(ordernum);
		recharge.setStatus(Constant.STATUES_ZERO);
		if(costratio.getRechargeMethod()==1){
			recharge.setMer_fee(0.00);
		}else{
			recharge.setMer_fee(fee);
		}
		recharge.setFee_taken_on(costratio.getRechargeMethod());
		recharge.setAdditional_info(additional_info);
		recharge.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		dao.save(recharge);
		
		Map<String, String> map = null;
		try {
			String registerXml = ParseXML.rechargeXml(rechargeInfo);
			payLogService.savePayLog(registerXml, userbasics.getId(), 2, ordernum,0,fee,0.00,amount);
			map = RechargeInfoService.rechargeCall(registerXml, ParameterIps.getMerchantKey());
			map.put("url", PayURL.RECHARGETESTURL);
			request.getSession().setAttribute("map", map);
			return "1";
		} catch (Exception e) {
			LOG.error("宝付充值模版加载报错----->订单号----->" +  recharge.getOrderNum(),e);
			return "2";
		}
	}
	  
	   public boolean transBonusesState(String orderNum,String loanId,Long userId,Double money,int action){
	    	P2pQuery p2pQuery = new P2pQuery(orderNum, 7);
	    	List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			try {
				String transBonuesXml = ParseXML.p2pQueryXml(p2pQuery);
				nvps.add(new BasicNameValuePair("requestParams", transBonuesXml));
				nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(transBonuesXml	+ "~|~" + ParameterIps.getMerchantKey())));
				payLogService.savePayLog(transBonuesXml, userId, Long.parseLong(loanId), action, orderNum,0.00,0.00, money);
				String result = CommonUtil.excuteRequest(PayURL.P2PQUERYTESTURL,nvps);
				LOG.error("返回信息" + result);
				Document doc = DocumentHelper.parseText(result);
				Element rootElt = doc.getRootElement(); // 获取根节点
				String code = rootElt.elementTextTrim("code");
				String msg = rootElt.elementTextTrim("msg");
				String sign = rootElt.elementTextTrim("sign");
				// 获取子节点crs下的子节点result
				Iterator iteratorResult = rootElt.elementIterator("result");
				String state = "0"; // 0-失败 1-成功
				while (iteratorResult.hasNext()) {
					Element itemEle = (Element) iteratorResult.next();
					Iterator iteratorOrder = itemEle.elementIterator("order"); // 获取子节点result下的子节点order
					while (iteratorOrder.hasNext()) {
						Element elementOrder = (Element) iteratorOrder.next();
						state = elementOrder.elementTextTrim("state");
						String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
						if (sign.equals(Md5sign)) {
							if (code.equals("CSD000")) {
								if (state.equals("1")) {
									return true;
								}
							} else if (code.equals("CSD333")) {
								return false;
							} else {
								LOG.error("查询处理失败--" + result + "----->订单号----->"+ orderNum);
								return false;
							}
						} else {
							LOG.error("查询返回数据--" + result + "----->订单号----->"+ orderNum);
							return false;
						}
					}
				}
				return false;
			} catch (Exception e) {
				LOG.error("查询失败----->订单号----->" + orderNum,e);
				return false;
			}
	    }
	   
		/***
		 * 绑定宝付账户 若无宝付账户，直接注册 若有先绑定验证码，获得验证码进行注册
		 * @param request
		 * @param userId
		 * @param sendBindCode
		 * @param sendNum
		 *            0-无 1-有
		 * @return
		 */
		public String ipsRegisterService(HttpServletRequest request, String userId,String sendBindCode, String sendNum,String cardId,String name) {
			if(!StringUtil.isNotBlank(cardId)||!StringUtil.isNotBlank(name)){
				return "4"; //如果身份证和姓名为空则返回
			}
			 Pattern pattern = Pattern.compile("/^((\\d{18})|(\\d{17}[Xx]))$/");
			 Matcher matcher = pattern.matcher(cardId);
			if (matcher.matches()) {
				return "6";
			}
			String dates = cardId.substring(6, 10) + "-" + cardId.substring(10, 12) + "-" + cardId.substring(12, 14);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
			Date d;
			try {
				d = sdf.parse(dates);
				d.setYear(d.getYear()+18);
				boolean flag = d.before(new Date());
				if(!flag){
					return "7";
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Userbasicsinfo user = userbasicsinfoService.queryUserById(Long.valueOf(userId));
			RegisterInfo registerInfo = new RegisterInfo();
			if (sendNum.equals("0")) {
				registerInfo.setHas_bf_account("0");
				registerInfo.setBind_code("0");
			} else if (sendNum.equals("1")) {
				registerInfo.setHas_bf_account("1");
				registerInfo.setBind_code(sendBindCode);
			}
			Long user_id = new Date().getTime();
			registerInfo.setBf_account(user.getUserrelationinfo().getPhone());
			registerInfo.setId_card(cardId.trim());
			registerInfo.setName(name.trim());
			registerInfo.setUser_id(String.valueOf(user_id));
			registerInfo.setAccount_type("1");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			try {
				String rechargeInfoxml = CommonUtil.aesEncryptKey16(ParseXML.registrationXml(registerInfo),ParameterIps.getMerchantKey());
				payLogService.savePayLog(rechargeInfoxml, user.getId(),1, user.getId()+"_"+user_id, user.getId(), 0.00,0.00, 0.00); // 保存xml报文
				user.getUserrelationinfo().setCardId(cardId.trim());
				user.setName(name);
				userbasicsinfoService.update(user);
				nvps.add(new BasicNameValuePair("requestParams",rechargeInfoxml));
				nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(rechargeInfoxml + "~|~"+ ParameterIps.getMerchantKey())));
				String result = CommonUtil.excuteRequest(PayURL.QUICKREGISTERURL,nvps);
				LOG.error("用户注册=" + result);
				Document doc = DocumentHelper.parseText(result);
				Element rootElt = doc.getRootElement(); // 获取根节点
				// 拿到crs节点下的子节点code值
				String code = rootElt.elementTextTrim("code");
				String msg = rootElt.elementTextTrim("msg");
				String sign = rootElt.elementTextTrim("sign");
				String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
				if (sign.equals(Md5sign)) {
					if (code.equals("CSD000")) {
						user.setpIpsAcctDate(DateUtil.format("yyyy-MM-dd HH:mm:ss"));
						user.setpMerBillNo(String.valueOf(registerInfo.getUser_id()));
						user.setHasIpsAccount(1);
						user.setIsAuthIps(0);
						user.getUserrelationinfo().setCardId(cardId.trim());
						user.setCardStatus(2);
						user.setName(name);
						userbasicsinfoService.update(user);
						generalizeService.updateGeneralize(user);
						payLogService.updatePayLog(user.getId()+"_"+user_id,Constant.STATUES_ONE);
						LOG.error("宝付支付注册成功，用户Id=" + user.getId());
					} else {
						LOG.error("宝付注册处理失败，用户Id=" + user.getId());
						return code+"_"+msg;
					}
				} else {
					LOG.error("非宝付注册返回数据，用户Id=" + user.getId());
					return "3";
				}
			} catch (Exception e) {
				LOG.error("宝付注册处理失败，用户Id=" + user.getId()+"--",e);
			}
			return "1";
		}
		
		/***
		 * 绑定验证码服务端接口 若已注册宝付账户，先根据宝付账户绑定验证码，验证码会及时发送到手机上
		 * @param request
		 * @param bf_account
		 *            宝付账户
		 * @return
		 */
		public String ipsSendBindCodeService(HttpServletRequest request, String bf_account) {
			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("requestParams", bf_account));
				nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(bf_account+ "~|~" + ParameterIps.getMerchantKey())));
				String result = CommonUtil.excuteRequest(PayURL.SENDBINDCODEURL,nvps);
				System.out.println("绑定验证码=" + result);
				Document doc = DocumentHelper.parseText(result);
				Element rootElt = doc.getRootElement(); // 获取根节点
				System.out.println("根节点：" + rootElt.getName()); // 拿到根节点的名称
				// 拿到crs节点下的子节点code值
				String code = rootElt.elementTextTrim("code");
				String msg = rootElt.elementTextTrim("msg");
				String sign = rootElt.elementTextTrim("sign");
				String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
				if (sign.equals(Md5sign)) {
					if (code.equals("CSD000")) {
						return "1";
					} else {
						return "2";
					}
				} else {
					return "2";
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "2";
			}
		}
		
		/***
		 * 获取手机验证码
		 * @param request
		 * @return
		 */
		public String ipsSendPhoneBindCodeService(HttpServletRequest request) {
			Userbasicsinfo userbasics = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
			Userbasicsinfo user = userbasicsinfoService.queryUserById(userbasics.getId());
			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("requestParams", user.getUserrelationinfo().getPhone()));
				nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(user.getUserrelationinfo().getPhone()+ "~|~"+ ParameterIps.getMerchantKey())));
				payLogService.savePayLog(user.getUserName() + "发送手机验证码",user.getId(), 23);
				String result = CommonUtil.excuteRequest(PayURL.SENDBINDCODEURL,nvps);
				LOG.error("绑定验证码=" + result);
				Document doc = DocumentHelper.parseText(result);
				Element rootElt = doc.getRootElement(); // 获取根节点
				// 拿到crs节点下的子节点code值
				String code = rootElt.elementTextTrim("code");
				String msg = rootElt.elementTextTrim("msg");
				String sign = rootElt.elementTextTrim("sign");
				String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
				if (sign.equals(Md5sign)) {
					if (code.equals("CSD000")) {
						return "1";
					} else {
						return "2";
					}
				} else {
					return "2";
				}
			} catch (Exception e) {
				LOG.error("获取手机验证码:",e);
				return "2";
			}
		}
		
		/****
		 * 前端添加银行卡 type =0 删除 +id type =1 新增 +userbank
		 * @param id
		 * @param request
		 * @return
		 */
		public synchronized String ipsOpBankCardService(HttpServletRequest request,String type, UserBank userBank, String id,String validateCode) {
			Userbasicsinfo userbasics = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
			Userbasicsinfo user = userbasicsinfoService.queryUserById(userbasics.getId());
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			String udId = "";
			try {
				if (type.equals("0")) { // 删除
					udId = id;
					String vilicode=userBank.getValidate_code();
					userBank = userBankService.getUserBankById(Long.valueOf(id));
					userBank.setValidate_code(vilicode);
					if(userBank.getState()==-1){
						userBankService.delete(userBank);
						LOG.info("删除银行卡成功");
						return "1";
					}
				} else if (type.equals("1")) { // 新增
					boolean isbank=userBankService.getUserBank(userBank.getBank_no());
					if(isbank){
						userBank.setState(0);
						userBank.setUserbasicsinfo(user);
						userBank.setBank_no(userBank.getBank_no().trim());
						Serializable seria = userBankService.saveUserBankSeria(userBank); // 保存银行卡
						udId = seria.toString();
					}else{
						return "4";
					}
				
				}
				String opBankCardXml = ParseXML.opBankCardXml(userBank, type);
				String aesOpBankCardXml = CommonUtil.aesEncryptKey16(opBankCardXml,ParameterIps.getMerchantKey());
				payLogService.savePayLog(opBankCardXml, user.getId(),Long.valueOf(type), 22, udId + userBank.getBank_no(), 0.00,0.00, 0.00); // 保存xml报文
				nvps.add(new BasicNameValuePair("requestParams", aesOpBankCardXml));
				nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(aesOpBankCardXml + "~|~"+ ParameterIps.getMerchantKey())));
				String result = CommonUtil.excuteRequest(PayURL.OPBANKCARDURL, nvps);
				LOG.error("绑定银行卡=" + result);
				Document doc = DocumentHelper.parseText(result);
				Element rootElt = doc.getRootElement(); // 获取根节点
				// 拿到crs节点下的子节点code值
				String code = rootElt.elementTextTrim("code");
				String msg = rootElt.elementTextTrim("msg");
				String sign = rootElt.elementTextTrim("sign");
				String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~"+ ParameterIps.getDes_algorithm());
				UserBank userbBank = userBankService.getUserBankById(Long.valueOf(udId));
				if (sign.equals(Md5sign)) {
					if (code.equals("CSD000")) {
						if (type.equals("1")) {
							if (userbBank.getState() != 1) {
								userbBank.setState(Constant.STATUES_ONE);
								userBankService.update(userbBank);
								payLogService.updatePayLog(udId + userbBank.getBank_no(),Constant.STATUES_ONE);
								// 添加注册奖励
								if (null == bonusService.queryRegBonus(user.getId())) {  
									Generalize generalize=bonusService.queryGeneralize(user.getId());
									if(generalize!=null){
										Userbasicsinfo referrer=userbasicsinfoService.queryUserById(generalize.getGenuid());
										RegBonus regBonus = new RegBonus();
										regBonus.setUserbasicsinfo(user);
										regBonus.setReferrer(referrer);
										regBonus.setReleaseStatus(0);
										Costratio cos = loanSignService.queryCostratio();
										regBonus.setBouns(null != cos.getRegBonu()
												&& !"".equals(cos.getRegBonu()) ? cos.getRegBonu() : 0);
										dao.save(regBonus); // 保存信息
									}
								}
								LOG.info("添加银行卡成功");
							}
						} else if (type.equals("0")) {
							userBankService.delete(userbBank);
							LOG.info("删除银行卡成功");
						}
					}else if(code.equals("CSD333")){
						return msg;
					}else if(code.equals("BD004")){
						return msg;
					} else {
						if (type.equals("1")) {
							if (userbBank.getState() != 1) {
								userbBank.setState(-1);
								userBankService.update(userbBank);
								payLogService.updatePayLog(udId, -1);
								LOG.error("添加银行卡失败");
							}
						} 
						return "2";
					}
				} else {
					return "2";
				}
				return "1";
			} catch (Exception e) {
				LOG.error("添加银行卡失败",e);
				e.printStackTrace();
				return "3";
			}
		}
		
		/** 用户授权接口(页面) */
		public String ipsInAccreditUserService(HttpServletRequest request, String type) {
			Userbasicsinfo userbasics = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
			Userbasicsinfo user = userbasicsinfoService.queryUserById(userbasics.getId());
			Map<String, String> map = null;
			try {
				map = RechargeInfoService.inAccreditUserCall(user.getpMerBillNo());
				payLogService.savePayLog(user.getpMerBillNo() + "_" + user.getUserName(),user.getId(), user.getId(), 16, user.getpMerBillNo(), 0.00,0.00, 0.00); // 保存xml报文
				if(type != null){
					map.put("page_url", Constant.H5INACCREDITUSRE);
					map.put("service_url", Constant.H5ASYNCHRONISMINACCREDITUSER);
				}
				map.put("url", PayURL.INACCREDITURL);
				request.getSession().setAttribute("map", map);
				return "member/callcentralInAccredit.htm";
			} catch (Exception e) {
				LOG.error("用户授权数据封装失败",e);
				return "1";
			}
		}
		
	  public List<Withdraw> getListWithdraw(){
		   String sql="select * from withdraw where state in (0,2,5) ";
		   List<Withdraw> list=dao.findBySql(sql, Withdraw.class);
		    return list;
	 }	
	  
	  public List<Recharge> getListRecharge(){
		  String sql="select * from recharge  where `status`=0";
		  List<Recharge> list=dao.findBySql(sql, Recharge.class);
		  return list;
	  }
	  
	 public List<Loanrecord> getLoanRecordList(Long loanId){
		 String sql="select * from loanrecord where isSucceed=1 and loanSign_id=? ";
		  List<Loanrecord> list=dao.findBySql(sql, Loanrecord.class,loanId);
		  return list;
	 }
	 
	 public List<Generalize> getGeneralizeList(Long loanId){
		 String sql="select * from generalize where genuid=? ";
		  List<Generalize> list=dao.findBySql(sql, Generalize.class,loanId);
		  return list;
	 }
	 
	public void updateLoanRecord(Loanrecord loanrecord){
		dao.update(loanrecord);
	}
	
	public void getLoanRecordSql(String tenderTime,Long loanRecordId) {
		String sql = "update  loanrecord set isSucceed=1,tenderTime='"+tenderTime+"' where id="+loanRecordId;
		dao.executeSql(sql);
	}
	
	/***
	 * 修改收取平台服务费状态
	 * @param loanId
	 */
	public void getLoanFeeStateSql(String loanId) {
		String sql = "update  loansign set feeState=0  where id="+loanId;
		dao.executeSql(sql);
	}
	 
	public Accountinfo getAccountinfo(String orderSn){
		String sql="select * from accountinfo where ipsNumber like '%"+orderSn+"%' ";
		Accountinfo accountinfo = dao.findObjectBySql(sql, Accountinfo.class);
		return accountinfo;
	}
	
	/**
	 * 放款流水
	 * @param loan
	 * @param payLog
	 * @param userinfo
	 * @param order_id
	 * @param money
	 */
	public  void getSaveAccount(Loansign loan,Paylog payLog,Userbasicsinfo userinfo,String order_id,Double money){
		
		// 流水账
	    Accountinfo accountTwo = new Accountinfo();
	    accountTwo.setExpenditure(0.00);
	    accountTwo.setExplan("融资总额");
	    accountTwo.setIncome(loan.getIssueLoan());
	    accountTwo.setIpsNumber(order_id);
	    accountTwo.setLoansignId(loan.getId().toString());// 标id（项目id）
	    accountTwo.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
	    accountTwo.setUserbasicsinfo(userinfo);
	    accountTwo.setFee(0.00);
	    accountTwo.setAccounttype(plankService.accounttype(23L));
	    accountTwo.setMoney(0.00);
		plankService.saveAccount(accountTwo);// 添加流水账余额
		
	   if(payLog.getFee()>0&&loan.getRefunway()!=4){
			// 流水账
		    Accountinfo accountOne = new Accountinfo();
		    accountOne.setExpenditure(payLog.getFee());
		    accountOne.setExplan("平台收取服务费");
		    accountOne.setIncome(0.00);
		    accountOne.setIpsNumber(order_id);
		    accountOne.setLoansignId(loan.getId().toString());// 标id（项目id）
		    accountOne.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		    accountOne.setUserbasicsinfo(userinfo);
		    accountOne.setFee(0.00);
		    accountOne.setAccounttype(plankService.accounttype(17L));
		    accountOne.setMoney(0.00);
			plankService.saveAccount(accountOne);// 添加流水账余额
		}
		// 流水账
	    Accountinfo account = new Accountinfo();
		account.setExpenditure(0.00);
		account.setExplan("项目放款");
		account.setIncome(payLog.getAmount());
		account.setIpsNumber(order_id);
		account.setLoansignId(loan.getId().toString());// 标id（项目id）
		account.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		account.setUserbasicsinfo(userinfo);
		account.setFee(0.00);
		account.setAccounttype(plankService.accounttype(12L));
		account.setMoney(money);
		plankService.saveAccount(account);// 添加流水账余额
		
	}
	
	
	/***
	 * 根据标Id进行加息转账
	 * @param request
	 * @param loanId
	 * @return
	 */
	 public  String ipsVoteIncomeZZ(HttpServletRequest request,String loanId){
		   //根据标Id查询有关的佣金记录信息
		 List<VoteIncome> voteIncomeList=voteincomeService.getVoteIncomeList(loanId);
	    	if(voteIncomeList.size()>0){
	    		for (int i = 0; i < voteIncomeList.size(); i++) {
	    			VoteIncome voteIncome=voteIncomeList.get(i);
	    			Userbasicsinfo userbasicsinfo = userbasicsinfoService.queryUserById(voteIncome.getVoterId());
	    			String orderNum = "JX" +StringUtil.getDateTime(userbasicsinfo.getId(),Long.valueOf(loanId));// 佣金转账订单号
	    			AcctTrans acctTran = new AcctTrans();
					acctTran.setMerchant_id(ParameterIps.getCert());
					acctTran.setOrder_id(orderNum);
					acctTran.setPayer_user_id(ParameterIps.getCert());
					acctTran.setPayee_user_id(userbasicsinfo.getpMerBillNo());// 收款
					acctTran.setPayer_type(1);
					acctTran.setPayee_type(0);// 收款
					acctTran.setAmount(Double.valueOf(voteIncome.getIncomeMoney()));
					acctTran.setFee(0.00);
					acctTran.setFee_taken_on(1);
					acctTran.setReq_time(new Date().getTime());
					try {
						String registerXml = ParseXML.accttrans(acctTran);
						ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
						nvps.add(new BasicNameValuePair("requestParams",registerXml));
						nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(registerXml + "~|~"+ ParameterIps.getMerchantKey())));
						payLogService.savePayLog(registerXml, userbasicsinfo.getId(),Long.parseLong(loanId), 28, orderNum, 0.00, 0.00,voteIncome.getIncomeMoney());
						String result = CommonUtil.excuteRequest(PayURL.TRANSFERURL, nvps);
						result = result.replace("\"", "\'");
						voteIncome.setStatus(Constant.STATUES_TWO);
						voteincomeService.updateVoteIncome(voteIncome);
						crs cr = new crs();
						XStream xss = new XStream(new DomDriver());
						xss.alias(cr.getClass().getSimpleName(), cr.getClass());
						cr = (crs) xss.fromXML(result);
						String Md5sign = CommonUtil.MD5(cr.getCode() + "~|~"+ cr.getMsg() + "~|~"+ ParameterIps.getDes_algorithm());
						if (cr.getSign().equals(Md5sign)) {
							if (cr.getCode().equals("CSD000")) {
								  if(transBonusesState(orderNum,loanId,userbasicsinfo.getId(),voteIncome.getIncomeMoney(),28)){
									    voteIncome.setStatus(Constant.STATUES_ONE);
										voteincomeService.updateVoteIncome(voteIncome);
									    // 添加流水
										Accountinfo account = new Accountinfo();
										account.setExpenditure(0.00);
										account.setExplan("加息奖励");
										account.setIncome(voteIncome.getIncomeMoney());
										account.setIpsNumber(orderNum);
										account.setLoansignId(loanId);// 标id（项目id）
										account.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
										account.setUserbasicsinfo(userbasicsinfo);
										account.setAccounttype(plankService.accounttype(24L));
										account.setMoney(0.00);
										account.setFee(0.00);
										payLogService.updatePayLog(orderNum, Constant.STATUES_ONE);
										plankService.saveAccount(account);// 添加流水账余额
										LOG.error("加息奖励发放成功");
//										System.out.println("加息奖励成功");
								  }else{
									    voteIncome.setStatus(-1);
									    voteincomeService.updateVoteIncome(voteIncome);
									    LOG.error("加息奖励发放失败：操作员+用户+应收金额+投资记录起码有一项不对");
//									    System.out.println("加息奖励失败");
									    return "0";
								  }
							}else{
							    voteIncome.setStatus(-1);
							    voteincomeService.updateVoteIncome(voteIncome);
							    LOG.error("加息奖励发放失败宝付返回失败"+cr.getCode());
//							    System.out.println("加息奖励失败");
								return "3";
							}
						}
					} catch (Exception e) {
						LOG.error("加息奖励发放失败",e);
//						e.printStackTrace();
						return "-1";
					} 
				}
	    	}else{
	    		return "2";
	    	}
	    	return "1";
	    }
	 
	 /**
		 * 注册宝付
		 * @param userId   用户id
		 * @param request   HttpServletRequest
		 * @return String
		 */
		public String ipsRegistrationService(HttpServletRequest request,String cardId,String name) {

			// 得到当前用户信息
			Userbasicsinfo userbasics = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
			Userbasicsinfo user = userbasicsinfoService.queryUserById(userbasics.getId());
			RegisterInfo register = new RegisterInfo();
			Long user_id = new Date().getTime();
			register.setBf_account(user.getUserrelationinfo().getPhone());
			register.setId_card(cardId);
			register.setName(name);
			register.setUser_id(String.valueOf(user_id));
			Map<String, String> map = null;
			try {
				String registerXml = ParseXML.registration(register,String.valueOf(user.getId()));
				map = RegisterService.registerCall(registerXml);
				// 添加日志
				payLogService.savePayLog(registerXml, user.getId(), 1,user_id.toString(), user_id, 0.00, 0.00, 0.00);
				map.put("url", PayURL.REGISTRATIONTESTURL);
				request.setAttribute("map", map);
				return "WEB-INF/views/hc9/member/trade/central_news";
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
}
