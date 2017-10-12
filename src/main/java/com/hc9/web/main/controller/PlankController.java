package com.hc9.web.main.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.common.exception.ServicesException;
import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.constant.PayURL;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2016.month04.HcOpenCardActivityCache;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.LoanSignService;
import com.hc9.web.main.service.activity.ActivityAllInOneService;
import com.hc9.web.main.service.baofo.BaoFuInvestService;
import com.hc9.web.main.service.baofo.BaoFuLoansignService;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.InvestLoanRecordVo;
import com.hc9.web.main.vo.InvestVo;

/** 用户购标 */
@Controller
@RequestMapping("/plank")
public class PlankController {
	@Resource
	private BaoFuLoansignService baoFuLoansignService;
	
	@Resource
	private LoanSignService loanSignService;
	
	@Resource
	private BaoFuInvestService baoFuInvestService;

	@Resource
	private ActivityAllInOneService activityAllInOneService;
	
	/** 前端项目投标查询 */
	@ResponseBody
	@CheckLoginOnMethod
	@RequestMapping("ipsPostLoanInfo.htm")
	public synchronized String ipsPostLoanInfo(HttpServletRequest request, String id) {
		try {
			return queryLoanRecordStatus(id);
		} catch(Exception e) {
			LOG.error("投资记录查询过程中报错！", e);
			return "2";
		}
	}

	/** 投资记录查询方法 
	 * @throws Exception */
	private String queryLoanRecordStatus(String loanRecordId) throws Exception {
		// 查询购买记录
		Loanrecord loanRecord = loanSignService.getLoanRecord(loanRecordId);
		if(loanRecord.getIsSucceed()==1){
			return "1";
		}
		String result = "userLockFail";
		Long userId = loanRecord.getUserbasicsinfo().getId();
		/** 同一个用户90秒内不能并发投资 */
		String userIdConcurrentLock = "STR:INVEST:USER:CONCURRENT:LOCK:" + userId;
		if(!RedisHelper.isKeyExistSetWithExpire(userIdConcurrentLock, 90)) {
			/** 同一个项目用户60秒内不能并发投资 */
			 int seconds = 60;
			 Long loanId = loanRecord.getLoansign().getId();
			 String loanIdConcurrentLock = "STR:INVEST:LOANID:CONCURRENT:LOCK:" + loanId;
			 boolean lockFlag = RedisHelper.isKeyExistSetWithExpire(loanIdConcurrentLock, seconds);
			 for(int i = 0; i < 16; i++) {
				 if(!lockFlag) {//如果没被其他用户锁住，则获取锁，跳出循环，否则，循环获取锁16次
					 LOG.error("用户" + userId + "在标" + loanId + "投资记录" + loanRecordId + "查询过程中获取锁成功，当前为第" + (i + 1) + "次！");
					 break;
				 } else {
					 LOG.error("用户" + userId + "在标" + loanId + "投资记录" + loanRecordId + "查询过程中获取锁失败，当前为第" + i + "次！");
					 try {
						 Thread.sleep(500);
					 } catch (InterruptedException e) {
						 LOG.error("用户" + userId + "在标" + loanId + "投资记录" + loanRecordId + "查询过程中出现异常！", e);
					 }
					 lockFlag = RedisHelper.isKeyExistSetWithExpire(loanIdConcurrentLock, seconds);
				 }
			 }
			 if(!lockFlag) {
				 result = baoFuLoansignService.ipsLoanInfoLoanHandle(loanRecord);
				 RedisHelper.del(loanIdConcurrentLock);
				 baoFuLoansignService.updateRedisInfoAfterInvest(userId, loanId);
			 } else {
				 result = "loanLockFail";
			 }
			 RedisHelper.del(userIdConcurrentLock);
		}
		return result;
	}
	
	/** 服务端项目投标 */
	@RequestMapping("ipsGetLoanInfo.htm")
	@ResponseBody
	@CheckLoginOnMethod
	public synchronized Map<String, Object> ipsGetLoanInfo(String pcode, String tpwd,Long loanId,Double priority, Double middle,Double after,Long redId,Long cardId,Integer webOrApp,HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			 // 获取当前用户
			 Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
			 if(user != null) {
				 Long userId = user.getId();
				 /** 同一个用户90秒内不能并发投资 */
				 String userIdConcurrentLock = "STR:INVEST:USER:CONCURRENT:LOCK:" + userId;
				 if(!RedisHelper.isKeyExistSetWithExpire(userIdConcurrentLock, 90)) {
					 /** 同一个项目用户90秒内不能并发投资 */
					 int seconds = 90;
					 String loanIdConcurrentLock = "STR:INVEST:LOANID:CONCURRENT:LOCK:" + loanId;
					 boolean lockFlag = RedisHelper.isKeyExistSetWithExpire(loanIdConcurrentLock, seconds);
					 for(int i = 0; i < 16; i++) {
						if(!lockFlag) {//如果没被其他用户锁住，则获取锁，跳出循环，否则，循环获取锁16次
							LOG.error("用户" + userId + "在标" + loanId + "投资过程中获取锁成功，当前为第" + (i + 1) + "次！");
							break;
						} else {
							LOG.error("用户" + userId + "在标" + loanId + "投资过程中获取锁失败，当前为第" + i + "次！");
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								LOG.error("用户" + userId + "在标" + loanId + "投资过程中出现异常！", e);
							}
							lockFlag = RedisHelper.isKeyExistSetWithExpire(loanIdConcurrentLock, seconds);
						}
					}
					boolean loanIdConcurrentLockFlag = false;
					if(!lockFlag) {
						 resultMap = validAndSaveInvestRecord(pcode, tpwd, loanId, 
								 priority, middle, after, redId, cardId, webOrApp, request);
						 loanIdConcurrentLockFlag = true;
						 baoFuLoansignService.updateRedisInfoAfterInvest(userId, loanId);
						 boolean activityFlag = HcOpenCardActivityCache.isOpenCardActivity(new Date());
						 if(activityFlag) {
							 resultMap.put("openCardFlag", 1);
						 }
					 } else {
						 resultMap = generateMapData("1", "系统繁忙，稍后再试！");
					 }
					 /** 释放投资过程中相关的锁 */
					 releaseLockOfInvest(loanIdConcurrentLockFlag, loanIdConcurrentLock, userIdConcurrentLock);
				 } else {
					 resultMap = generateMapData("1", "系统繁忙，稍后再试！");
				 }
			 } else {
				 resultMap = generateMapData("-1", "尚未登录，投资失败！");
			 }
		} catch (ServicesException e) {
			LOG.error("投资过程中出现异常！", e);
			resultMap = generateMapData("1", "项目可投余额不足，请重新输入!");
		} catch(Exception e) {
			LOG.error("投资过程中出现异常！", e);
			resultMap = generateMapData("1", "系统异常，投资失败！");
		}
		 return resultMap;
	}
	
	/** 释放投资过程中锁相关 */
	private void releaseLockOfInvest(boolean loanIdConcurrentLockFlag, String loanIdConcurrentLock, 
			String userIdConcurrentLock) {
		final boolean loanIdConcurrentLockFlagFinal = loanIdConcurrentLockFlag;
		final String loanIdConcurrentLockFinal = loanIdConcurrentLock;
		final String userIdConcurrentLockFinal = userIdConcurrentLock;
		CacheManagerService.threadPoolExecutor.submit(
			new Thread() {
				public void run() {
					if(loanIdConcurrentLockFlagFinal) {
						RedisHelper.del(loanIdConcurrentLockFinal);
					}
					RedisHelper.del(userIdConcurrentLockFinal);
				}
			});
	}
	
	/** 校验相关参数并保存相关投资记录 */
	public Map<String, Object> validAndSaveInvestRecord(String pcode, String tpwd, 
			Long loanId, Double priority, Double middle, Double after, Long redId, Long cardId, 
			Integer webOrApp, HttpServletRequest request){
		String investOrderNum = System.currentTimeMillis() + "_" + loanId;
		String juChengActivityFlag = getJuChengProperty(request,  priority, middle, after);
		Map<String, Object> resultMap =  baoFuInvestService.validAndSaveInvestRecord(pcode, tpwd, loanId, 
				priority, middle, after, redId, cardId, webOrApp, request);
		String code = "" + resultMap.get("code");
		String msg = "" + resultMap.get("msg");
		if("0".equals(code)) {
			InvestVo investVo = (InvestVo)resultMap.get("investVo");
			if(investVo == null || investVo.getLoanRecordList().size() < 1) {
				return generateMapData("1", "投资失败!");
			}
			List<InvestLoanRecordVo> loanRecordList = investVo.getLoanRecordList();
			/** 翻牌抽奖活动处理送翻牌次数问题:第5笔优先和夹层的问题 */
			handleOpenCardFifthRecord(investVo.getLoan(), loanRecordList);
			if(loanRecordList != null && loanRecordList.size() > 0) {
				List<Loanrecord> successList = new ArrayList<Loanrecord>();
				List<Loanrecord> failureList = new ArrayList<Loanrecord>();
				int i = 0;
				for(InvestLoanRecordVo investLoanRecordVo : loanRecordList) {
					i = i + 1;
					Loanrecord loanrecord = investLoanRecordVo.getLoanrecord();
					String bidinfoXml = investLoanRecordVo.getBidinfoXml();
					if(StringUtil.isNotBlank(bidinfoXml)) {
						boolean isLast = false;
						if(i == loanRecordList.size()) {
							isLast = true;
						}
						boolean resultFlag = callBaofuPayForInvest(investVo, investLoanRecordVo, bidinfoXml, 
								request, isLast, investOrderNum);
						if(resultFlag == true) {
							successList.add(loanrecord);
						} else {
							failureList.add(loanrecord);
						}
					}
				}
				msg = "恭喜您,投资成功!";
				if(successList.size() > 0 && successList.size() < loanRecordList.size()) {
					String successInfo = "";
					for(Loanrecord loanrecord : successList) {
						int subType = loanrecord.getSubType();
						double tenderMoney = loanrecord.getTenderMoney();
						if(subType == 1) {
							successInfo += "优先：" + tenderMoney + " 元;";
						} else if(subType == 2) {
							successInfo += "夹层：" + tenderMoney + " 元;";
						} else if(subType == 3) {
							successInfo += "劣后：" + tenderMoney + " 元;";
						}
					}
					String failureInfo = "";
					for(Loanrecord loanrecord : failureList) {
						int subType = loanrecord.getSubType();
						double tenderMoney = loanrecord.getTenderMoney();
						if(subType == 1) {
							failureInfo += "优先：" + tenderMoney + " 元;";
						} else if(subType == 2) {
							failureInfo += "夹层：" + tenderMoney + " 元;";
						} else if(subType == 3) {
							failureInfo += "劣后：" + tenderMoney + " 元;";
						}
					}
					msg = "恭喜您," + successList.size() + "笔投资成功（" + successInfo + 
							"）," + failureList.size() + "笔失败（" + failureInfo + "）！";
				} else if(failureList.size() == loanRecordList.size()) {
					code = "1";
					msg = "投资失败！";
				}
			}
		}
		resultMap.clear();
		resultMap.put("code", code);
		resultMap.put("msg", msg);
		resultMap.put("investOrderNum", investOrderNum);
		resultMap.put("juChengActivityFlag", juChengActivityFlag);
		return resultMap;
	}
	
	/** 设置聚橙网用户相关的属性 */
	private String getJuChengProperty(HttpServletRequest request, 
			Double priority, Double middle, Double after) {
		if(priority == null) {
			priority = 0d;
		}
		if(middle == null) {
			middle = 0d;
		}
		if(priority >= 10000 || middle >= 10000) {
			Userbasicsinfo user = (Userbasicsinfo) request.getSession().
					getAttribute(Constant.SESSION_USER);
			if(user != null) {
				if(activityAllInOneService.isUserValidJuChengActivity(user.getUserName())) {
					/** 用户是否已经获取过演唱会门票 */
					String key = "STR:HC9:JUCHENG:YCH:TICKET:" + user.getId();
					if(!RedisHelper.isKeyExist(key)) {
						return "1";
					}
				}
			}
		}
		return "0";
	}
	
	/** 调用宝付接口进行投资支付 */
	private boolean callBaofuPayForInvest(InvestVo investVo, InvestLoanRecordVo investLoanRecordVo, String bidinfoXml, 
			HttpServletRequest request, boolean isLast, String investOrderNum) {
		boolean resultFlag = false;
		if(StringUtil.isNotBlank(bidinfoXml)) {
			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				LOG.error("投资调用宝付接口所传递参数为：" + bidinfoXml);
				nvps.add(new BasicNameValuePair("requestParams", bidinfoXml));
				nvps.add(new BasicNameValuePair("sign",
						CommonUtil.MD5(bidinfoXml + "~|~" + ParameterIps.getMerchantKey())));
				String result = CommonUtil.excuteRequest(PayURL.REPAYMRNTTESTURL, nvps);
				Document doc = null;
				try {
					doc = DocumentHelper.parseText(result);
				} catch (Exception e) {
					LOG.error("投资过程中宝付返回报文转换过程中出错！", e);
				}
				if(doc != null) {
					Element rootElt = doc.getRootElement(); // 获取根节点
					// 拿到crs节点下的子节点code值
					String code = rootElt.elementTextTrim("code");
					String msg = rootElt.elementTextTrim("msg");
					String sign = rootElt.elementTextTrim("sign");
					String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~" + ParameterIps.getDes_algorithm());
					if (sign.equals(Md5sign)) {
						if (code.equals("CSD000")) {
							/** 宝付调用成功，处理相关记录 */
							baoFuInvestService.handleRecordAfterInvestSuccess(investVo, investLoanRecordVo, 
									request, isLast, investOrderNum);
							resultFlag = true;
						} else if (code.equals("CSD333")) {
							/** 宝付调用失败，处理相关记录 */
							baoFuInvestService.handleRecordAfterInvestFailure(investVo, investLoanRecordVo);
						}
					} else {
						Loanrecord loanrecord = investLoanRecordVo.getLoanrecord();
						LOG.error("订单数据MD5校验不通过，订单号：" + loanrecord.getOrder_id());
					}
				}
			} catch(Exception e) {
				LOG.error("投资宝付调用处理后相关过程出错！", e);
			}
		}
		return resultFlag;
	}
	
	@RequestMapping("/checkCode")
	@ResponseBody
	public boolean checkCode(HttpServletRequest request,String input) {
		// 取验证码
		String validate = (String) request.getSession().getAttribute("user_tend");
		if (input.equalsIgnoreCase(validate)) {
			return true;
		} else {
			return false;
		}
	}

	/** 支付页面 */
	@RequestMapping("/toLoanPay.htm")
	@ResponseBody
	public String toLoanPay(HttpServletRequest request, Long loanId,
			Double money, Integer subType) {
		Map<String, String> loanmap = new HashMap<String, String>();
		loanmap.put("money", String.valueOf(money));
		loanmap.put("subType", subType.toString());
		loanmap.put("loanId", loanId.toString());
		request.getSession().setAttribute("loanMap", loanmap);
		return "1";
	}

	/** 翻牌抽奖活动处理送翻牌次数问题:第5笔优先和夹层的问题 */
	public void handleOpenCardFifthRecord(Loansign loan, List<InvestLoanRecordVo> loanRecordList) {
		if(HcOpenCardActivityCache.isOpenCardActivity(new Date())) {
			int loanType = loan.getType();
			int remonth = loan.getRemonth();
			boolean activityFlag = true;
			if(loanType == 3) {
				if(remonth < 25) {
					activityFlag = false;
				}
			}
			if(loanRecordList != null && loanRecordList.size() > 1 && activityFlag) {
				long userId = loanRecordList.get(0).getLoanrecord().getUserbasicsinfo().getId();
				long totalInvestNum = HcOpenCardActivityCache.getTotalInvestNum(userId);
				if(totalInvestNum == 4) {
					/** 获取优先金额和优先记录id */
					double priTenderMoney = 0;
					long midLoanRecordId = 0;
					double midTenderMoney = 0;
					
					/** 是否使用红包和加息券  */
					boolean isRed = false;
					for(InvestLoanRecordVo vo : loanRecordList) {
						/*** 投标类型: 1-优先，2-夹层，3-列后 4-vip众筹，5-股东众筹 */
						if(vo.getIncreaseCard() != null || vo.getRedEnvelopeDetail() != null) {
							isRed = true;
						}
						if(1 == vo.getLoanrecord().getSubType()) {
							priTenderMoney = vo.getLoanrecord().getTenderMoney();
						} else if(2 == vo.getLoanrecord().getSubType()) {
							midLoanRecordId = vo.getLoanrecord().getId();
							midTenderMoney = vo.getLoanrecord().getTenderMoney();
						}
					}
					
					if(priTenderMoney > 0 && midTenderMoney > 0) {
						if(midTenderMoney > priTenderMoney || isRed) {
							String moneyKey = "STR:HC9:OPEN:CARD:FIFTH:RECORD:MONEY:" + userId;
							RedisHelper.set(moneyKey, "" + midTenderMoney);
							RedisHelper.expireByKey(moneyKey, 3 * 60);
							
							String midIdKey = "STR:HC9:OPEN:CARD:FIFTH:MID:RECORD:ID:" + userId;
							RedisHelper.set(midIdKey, "" + midLoanRecordId);
							RedisHelper.expireByKey(midIdKey, 3 * 60);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 封装返回参数
	 * @param rcode 操作类型 0成功 1失败 -1失效
	 * @param msg 返回信息
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> generateMapData(String code, String msg) {
		Map<String, Object> result = new HashMap<>();
		result.put("code", code);
		result.put("msg", msg);
		return result;
	}
}
