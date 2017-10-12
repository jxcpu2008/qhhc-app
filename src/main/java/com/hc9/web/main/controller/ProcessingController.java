package com.hc9.web.main.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.entity.Accountinfo;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Paylog;
import com.hc9.web.main.entity.Recharge;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Withdraw;
import com.hc9.web.main.redis.activity.year2016.month05.HcNewerTaskCache;
import com.hc9.web.main.service.GeneralizeService;
import com.hc9.web.main.service.HccoindetailService;
import com.hc9.web.main.service.LoanSignService;
import com.hc9.web.main.service.MessagePushService;
import com.hc9.web.main.service.PayLogService;
import com.hc9.web.main.service.PlankService;
import com.hc9.web.main.service.ProcessingService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.WithdrawServices;
import com.hc9.web.main.service.baofo.BaoFuService;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtil;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.vo.pay.ReturnInfo;
import com.hc9.web.main.vo.pay.crs;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import freemarker.template.TemplateException;

/** 处理宝付返回的信息 */
@Controller
@RequestMapping("/processing")
public class ProcessingController {
	private static final Logger logger = Logger.getLogger(ProcessingController.class);

	@Resource
	private ProcessingService processingService;

	@Resource
	private WithdrawServices withdrawServices;

	@Resource
	private LoanSignService loanSignService;

	@Resource
	private PlankService plankService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private BaoFuService baoFuService;

	@Resource
	private PayLogService payLogService;

	@Resource
	private GeneralizeService generalizeService;
	
	@Resource
	private HccoindetailService hccoindetailService;
	
	@Resource
	private MessagePushService messagePushService;
	
	/** 用户注册信息处理 */
	@RequestMapping("registration.htm")
	public String registration(ReturnInfo info, HttpServletRequest request) {

		return "WEB-INF/views/success";
	}

	/** 注册异步处理 */
	@RequestMapping("asynchronismRegistration.htm")
	public String asynchronismRegistration(ReturnInfo info, @RequestParam("param") Long userid
			, HttpServletRequest request)
			throws IOException, TemplateException {
		if (info != null) {
				crs resignc = new crs();
				XStream xs = new XStream(new DomDriver());
				xs.alias(resignc.getClass().getSimpleName(), resignc.getClass());
				resignc = (crs) xs.fromXML(info.getResult());
				String Md5sign = CommonUtil.getMd5sign(info.getResult());
				// 校验是否宝付回传数据
				if (info.getSign().equals(Md5sign)) {
					Userbasicsinfo user = userbasicsinfoService.queryUserById(userid);
					if (resignc.getCode().equals("CSD000")) {
							try{
								if(generalizeService.getGeneralizeIsAuthIps(user.getId())){
									user.setUserType(Constant.STATUES_SIX);
								}
								// 余额查询
								/*crs cr = baoFuService.getCasbalance(String.valueOf(resignc.getUser_id()));
								user.getUserfundinfo().setCashBalance(cr.getBalance());*/
								user.setpIpsAcctDate(DateUtil.format("yyyy-MM-dd HH:mm:ss"));
								user.setAuthIpsTime(DateUtil.format("yyyy-MM-dd HH:mm:ss"));
								user.setIsAuthIps(1);//前端页面注册，确定为授权
								user.setHasIpsAccount(1);  //成功
								user.setCardStatus(2);
								user.setpMerBillNo(String.valueOf(resignc.getUser_id()));
								userbasicsinfoService.update(user);
								/** 宝付状态修改：更新session中相关对象 */
								request.getSession().setAttribute(Constant.SESSION_USER, user);
								// 更新支付报文信息
								LOG.error("宝付支付注册成功"+info.getResult()+"----");
								generalizeService.updateGeneralize(user);
								//通过被推广人id反查推广人
								Userbasicsinfo userGen = generalizeService.queryPromoterByPromotedId(user.getId());
								if(userGen!=null){
									//lkl-20150811-添加员工推荐注册增加红筹币
									if(userGen.getUserType() == 2){
										hccoindetailService.saveHccoindetailNumber(userGen);
									}
									if((userGen.getUserType() == 1 || userGen.getUserType() == 3) && userGen.getIsAuthIps() == 1){
										userGen.setUserType(Constant.STATUES_SIX);
										userbasicsinfoService.update(userGen);
									}
								}
								return "WEB-INF/views/success";
							} catch(Exception e) {
								e.printStackTrace();
								LOG.error("宝付注册处理成功---->平台数据处理失败---" + info.getResult());
								return "WEB-INF/views/failure";
							}
						}else if(resignc.getCode().equals("CSD333")){
							   payLogService.updatePayLog(resignc.getOrder_id(),-1,0.00,0.00);
							   LOG.error("宝付注册处理失败----->" + info.getResult());
							   return "WEB-INF/views/failure";
					} else {
							LOG.error("宝付注册处理失败--" + info.getResult());
							return "WEB-INF/views/failure";
						}
				} else {
					LOG.error("非宝付注册返回数据--" + info.getResult() );
					return "WEB-INF/views/failure";
				}
		} else {
			return "WEB-INF/views/failure";
		}
	}

	/** 用户充值返回处理 */
	@RequestMapping("recharge.htm")
	public  String recharge(ReturnInfo info,
			HttpServletRequest request) {
		return "WEB-INF/views/success_recharge";

	}

	/** 宝付支付充值异步返回 */
	@RequestMapping("asynchronismRecharge.htm")
	public  String asynchronismRecharge(ReturnInfo info,HttpServletRequest request) throws IOException, TemplateException {
		if (info != null) {
			crs resignc = new crs();
			XStream xs = new XStream(new DomDriver());
			xs.alias(resignc.getClass().getSimpleName(), resignc.getClass());
			resignc = (crs) xs.fromXML(info.getResult());
			String Md5sign = CommonUtil.getMd5sign(info.getResult());
			if (info.getSign().equals(Md5sign)) {
				// 保存充值记录
				Recharge charge = processingService.findRechargeByOrderId(resignc.getOrder_id());
				if (resignc.getCode().equals("CSD000")) {
					try {
						if (charge.getStatus() == 0) {
								charge.setFee(resignc.getFee()); // 宝付收取费用
								charge.setMer_fee(resignc.getMer_fee()); // 商户收取的手续费
								charge.setIncash_money(resignc.getIncash_money()); // 充值结算金额(实际到账)
								charge.setCode(resignc.getCode());
								charge.setStatus(Constant.STATUES_ONE);
								charge.setAdditional_info(resignc.getAdditional_info());
								charge.setSuccTime(resignc.getSucc_time());
								processingService.updaterecharge(charge);
								Userbasicsinfo user = userbasicsinfoService.queryUserById(charge.getUserbasicsinfo().getId());
								// 流水账
								Accountinfo account = new Accountinfo();
								account.setExpenditure(0.00);
								account.setExplan("充值");
								account.setIncome(resignc.getIncash_money());
								account.setIpsNumber(resignc.getOrder_id());
								account.setTime(resignc.getSucc_time());
								account.setUserbasicsinfo(user);
								account.setFee(charge.getFee());
								account.setAccounttype(plankService.accounttype(6L));
								// 余额查询
								crs cr = baoFuService.getCasbalance(user.getpMerBillNo());
								user.getUserfundinfo().setCashBalance(cr.getBalance());
								user.getUserfundinfo().setOperationMoney(cr.getBalance());
								account.setMoney(cr.getBalance());
								userbasicsinfoService.update(user);
								plankService.saveAccount(account);// 保存流水账余额
								// 更新支付报文信息
								payLogService.updatePayLog(resignc.getOrder_id(),Constant.STATUES_ONE,charge.getIncash_money(), charge.getFee());
								long userId = charge.getUserbasicsinfo().getId();
								HcNewerTaskCache.giveFirstRechargeRedenvelopeKey(userId);
								LOG.error("宝付支付充值成功");
						}
						return "WEB-INF/views/success_recharge";
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("宝付充值处理成功---->平台数据处理失败---" + info.getResult()
								+ "----->订单号----->" + resignc.getOrder_id());
						return "WEB-INF/views/failure";
					}
				} else if(resignc.getCode().equals("CSD333")){
						   charge.setStatus(-1);
						   charge.setFee(0.00);
						   processingService.updaterecharge(charge);
						   payLogService.updatePayLog(charge.getOrderNum(),-1,charge.getIncash_money(),charge.getFee());
						   return "WEB-INF/views/failure";
				}else{
					LOG.error("宝付充值处理失败--" + info.getResult()+ "----->订单号----->" + resignc.getOrder_id());
					return  "WEB-INF/views/failure";
				}
			} else {
				LOG.error("非宝付充值返回数据--" + info.getResult() + "----->订单号----->"
						+ resignc.getOrder_id());
				return "WEB-INF/views/failure";
			}
		} else {
			return "WEB-INF/views/failure";
		}
	}

	/** 用户提现返回处理 */
	@RequestMapping("withdrawal.htm")
	public String withdrawal(ReturnInfo returnInfo, HttpServletRequest request) {
		logger.debug("提现操作，宝付同步处理返回！");
		return "WEB-INF/views/success_withdraw";
	}

	/** 提现的异步处理 */
	@RequestMapping("withdrawAsynchronous.htm")
	public  String withdrawAsynchronous(ReturnInfo info,HttpServletRequest request)throws IOException, TemplateException {
		logger.debug("提现操作，宝付异步处理返回！");
		
		if (info != null) {
			String Md5sign = CommonUtil.getMd5sign(info.getResult());
			crs crs = new crs();
			XStream xs = new XStream(new DomDriver());
			xs.alias(crs.getClass().getSimpleName(), crs.getClass());
			crs = (crs) xs.fromXML(info.getResult());
			if (info.getSign().equals(Md5sign)) {
				Withdraw withdraw = processingService.findForchargebyorderid(crs.getOrder_id());
				if (crs.getCode().equals("CSD000")) {
					try {
						if (withdraw.getState() == 0) {
							withdraw.setState(Constant.STATUES_TWO);
							withdrawServices.uptWithdraw(withdraw);
							// 更新支付报文信息
							payLogService.updatePayLog(crs.getOrder_id(),Constant.STATUES_TWO,0.00,0.00);
								LOG.error("宝付支付提现成功");
						}
						withdrawServices.updateWithdrawCardStatusAfterSuccess(withdraw.getUserbasicsinfo().getId(), 
								crs.getOrder_id());
						// 提现成功推送消息
						messagePushService.pushMessageAfterWithdrawSuccess(withdraw);
						
						return "WEB-INF/views/success_withdraw";
					} catch (Exception e) {
						LOG.error("宝付提现处理成功---->平台数据处理失败---" + info.getResult()
								+ "----->订单号----->" + crs.getOrder_id(), e);
						return "WEB-INF/views/failure";
					}
				} else  if(crs.getCode().equals("CSD333")){
						withdraw.setFee(0.00);
	                	withdraw.setState(-1);
	                	withdrawServices.uptWithdraw(withdraw);
	                	
	                	withdrawServices.updateWithdrawCardStatusAfterFailure(
	                			withdraw.getUserbasicsinfo().getId(), 
								crs.getOrder_id());
	                	// 更新支付报文信息
						payLogService.updatePayLog(withdraw.getStrNum(),-1,withdraw.getWithdrawAmount(), withdraw.getFee());
						return "WEB-INF/views/failure";
				}else{
					LOG.error("宝付提现处理失败--" + info.getResult()+ "----->订单号----->" + crs.getOrder_id());
					return "WEB-INF/views/failure";
				}
			} else {
				LOG.error("非宝付提现返回数据--" + info.getResult() + "----->订单号----->"+ crs.getOrder_id());
				return "WEB-INF/views/failure";
			}
		} else {
			return "WEB-INF/views/failure";
		}
	}

	/** 用户授权同步 */
	@RequestMapping("returnInaccreditUser.htm")
	public String returnInaccreditUser(ReturnInfo info,HttpServletRequest request){
		return  "WEB-INF/views/success";
	}
	
	/** 用户授权异步 */
	@RequestMapping("asynchronismInaccreditUser.htm")
	public  String asynchronismInaccreditUser(ReturnInfo info,HttpServletRequest request){
		if (info != null) {
			crs crsFlow = new crs();
			XStream xs = new XStream(new DomDriver());
			xs.alias(crsFlow.getClass().getSimpleName(), crsFlow.getClass());
			crsFlow = (crs) xs.fromXML(info.getResult());
			String Md5sign = CommonUtil.MD5(info.getResult() + "~|~"+ ParameterIps.getDes_algorithm());
			if (info.getSign().equals(Md5sign)) {
				 Paylog payLog = payLogService.queryPaylogByOrderSn(crsFlow.getUser_id().toString());
				 Userbasicsinfo userinfo = userbasicsinfoService.queryUserById(payLog.getLoansignId());
					if (crsFlow.getCode().equals("CSD000")) {
						if(userinfo.getIsAuthIps()!=1){
							userinfo.setIsAuthIps(Constant.STATUES_ONE);
							userinfo.setAuthIpsTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
							//理财师(被推广人)
							if(generalizeService.getGeneralizeIsAuthIps(userinfo.getId())){
								userinfo.setUserType(Constant.STATUES_SIX);
//								generalizeService.delGeneralize(userinfo);
							}
							userbasicsinfoService.update(userinfo);
							payLogService.updatePayLog(crsFlow.getUser_id().toString(),Constant.STATUES_ONE);
							/** 宝付状态修改：更新session中相关对象 */
							request.getSession().setAttribute(Constant.SESSION_USER, userinfo);
							LOG.error("用户授权成功--" + info.getResult()+"----->订单号----->"+ crsFlow.getUser_id());
							//通过被推广人id反查推广人
							Userbasicsinfo userGen = generalizeService.queryPromoterByPromotedId(userinfo.getId());
							if(userGen!=null){
								//lkl-20150811-添加员工推荐注册增加红筹币
								if(userGen.getUserType() == 2){
									hccoindetailService.saveHccoindetailNumber(userGen);
								}
								//理财师(推广人)
								if((userGen.getUserType() == 1 || userGen.getUserType() == 3) && userGen.getIsAuthIps() == 1){
									userGen.setUserType(Constant.STATUES_SIX);
									userbasicsinfoService.update(userGen);
//									generalizeService.delGeneralize(userGen);
								}
							}
						}
						return "WEB-INF/views/success";
					}else{
						userinfo.setIsAuthIps(-1);
						userinfo.setAuthIpsTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
						userbasicsinfoService.update(userinfo);
						payLogService.updatePayLog(crsFlow.getUser_id().toString(),-1);
						/** 宝付状态修改：更新session中相关对象 */
						request.getSession().setAttribute(Constant.SESSION_USER, userinfo);
						LOG.error("用户授权失败--" + info.getResult()+"----->订单号----->"+ crsFlow.getUser_id());
						return "WEB-INF/views/failure";
					}
			}else{
				LOG.error("非用户授权返回数据--" + info.getResult()+"----->订单号----->"+ crsFlow.getUser_id());
				return "WEB-INF/views/failure";
			}
		}else{
			return "WEB-INF/views/failure";
		}
	}

	/** 项目还款页面跳转 */
	@RequestMapping("returnRepaymentSign.htm")
	public String returnRepaymentSign(ReturnInfo returnInfo,
			HttpServletRequest request) {
		return  "WEB-INF/views/success";
	}

	/** 项目还款异步处理 */
	@RequestMapping("asynchronismRepaymentSign.htm")
	public String asynchronismRepaymentSign(ReturnInfo info,
			@RequestParam("amount") Double amount, @RequestParam("id") String id) throws IOException, TemplateException {
		if (info != null) {
			crs c = new crs();
			XStream xs = new XStream(new DomDriver());
			xs.alias(c.getClass().getSimpleName(), c.getClass());
			c = (crs) xs.fromXML(info.getResult());

			String Md5sign = CommonUtil.MD5(c.getCode() + "~|~" + c.getMsg() + "~|~" + c.getOrder_id() + "~|~" + ParameterIps.getDes_algorithm());
			Loansign loan = loanSignService.getLoansignById(id);
			Userbasicsinfo userinfo = userbasicsinfoService.queryUserById(loan .getUserbasicsinfo().getId());
			if (c.getSign().equals(Md5sign)) {
				loan.setStatus(3);
				processingService.updateLoan(loan);
				// 流水账
				Accountinfo account = new Accountinfo();
				account.setExpenditure(0.00);
				account.setExplan("项目放款");
				account.setIncome(amount);
				account.setIpsNumber(c.getOrder_id());
				account.setLoansignId(loan.getId().toString());// 标id（项目id）
				// account.setProject_id(""); // 店铺id
				account.setTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
				account.setUserbasicsinfo(userinfo);
				account.setAccounttype(plankService.accounttype(12L));

				crs cr = baoFuService.getCasbalance(userinfo.getpMerBillNo());

				userinfo.getUserfundinfo().setCashBalance(cr.getBalance());
				userinfo.getUserfundinfo().setOperationMoney(cr.getBalance());
				account.setMoney(cr.getBalance());
				plankService.saveAccount(account);// 添加流水账余额
				userbasicsinfoService.update(userinfo);
				if (c.getCode().equals("CSD000")) {
					return "WEB-INF/views/success";
				} else {
					return "WEB-INF/views/failure";
				}
			} else {
				LOG.error("宝付还款--" + info.getResult());
				return "WEB-INF/views/failure";
			}
		} else {
			return "WEB-INF/views/failure";
		}
	}
}