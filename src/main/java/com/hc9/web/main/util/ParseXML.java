package com.hc9.web.main.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.hc9.web.main.entity.UserBank;
import com.hc9.web.main.vo.BaoFuAccountVo;
import com.hc9.web.main.vo.CardInfo;
import com.hc9.web.main.vo.pay.AcctTrans;
import com.hc9.web.main.vo.pay.BalanceQueryInfo;
import com.hc9.web.main.vo.pay.BidInfo;
import com.hc9.web.main.vo.pay.BidInfo4;
import com.hc9.web.main.vo.pay.P2pQuery;
import com.hc9.web.main.vo.pay.Payuser;
import com.hc9.web.main.vo.pay.RechargeInfo;
import com.hc9.web.main.vo.pay.RegisterInfo;
import com.hc9.web.main.vo.pay.WithdrawalInfo;

import freemarker.template.TemplateException;

/** 生成xml文件 */
public class ParseXML {
	/**
	 * 用户注册信息生成xml文件
	 * 
	 * @param register
	 *            用户信息
	 * @return 返回一个xml文件
	 * @throws TemplateException
	 * @throws IOException
	 */
	public static String registration(RegisterInfo register,String param)
			throws IOException, TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("bf_account", register.getBf_account().toString());
		map.put("name", register.getName());
		map.put("id_card", register.getId_card().toString());
		map.put("user_id", register.getUser_id());
		map.put("return_url", register.getReturn_url()+"?param="+param);
		map.put("page_url", register.getPage_url());
		String registrationxml = FreeMarkerUtil.execute(
				"config/pay/registration.flt", "UTF-8", map);
		return registrationxml;
	}
	
	/***
	 * 服务端注册宝付账户
	 * @param register
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String registrationXml(RegisterInfo register)
			throws IOException, TemplateException{
		Map<String, String> map = new HashMap<String, String>();
		map.put("has_bf_account", register.getHas_bf_account());
		map.put("bf_account", register.getBf_account().toString());
		map.put("user_id", register.getUser_id());
		map.put("real_name", register.getName());
		map.put("id_card", register.getId_card().toString());
		map.put("bind_code", register.getBind_code());
		map.put("account_type", register.getAccount_type());
		String registrationxml = FreeMarkerUtil.execute("config/pay/quickRegister.flt", "UTF-8", map);
		return registrationxml;
	}


	/**
	 * 充值信息转换成xml
	 * 
	 * @param recharge
	 *            充值信息
	 * @return 返回一个xml文件
	 * @throws TemplateException
	 * @throws IOException
	 */
	public static String rechargeXml(RechargeInfo recharge) throws IOException,
			TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", recharge.getMerchant_id());
		map.put("order_id", recharge.getOrder_id());
		map.put("user_id", recharge.getUser_id());
		map.put("amount", recharge.getAmount().toString());
		map.put("fee", recharge.getFee().toString());
		map.put("fee_taken_on", recharge.getFee_taken_on());
		map.put("additional_info", recharge.getAdditional_info());
		map.put("return_url", recharge.getReturn_url());
		map.put("page_url", recharge.getPage_url());
		String recharges = FreeMarkerUtil.execute("config/pay/recharge.flt","UTF-8", map);
		return recharges;
	}
	
	
	/**
	 * 余额查询
	 * @param entity
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String BalanceInquiryXml(BalanceQueryInfo entity) throws IOException,
	TemplateException {
			Map<String, String> map = new HashMap<String, String>();
			map.put("merchant_id", entity.getMerchant_id());
			map.put("user_id", entity.getUser_id());
			String recharges = FreeMarkerUtil.execute("config/pay/balance.flt","UTF-8", map);
			return recharges;
	}

	/**
	 * 提现
	 * 
	 * @param withdrawal
	 *            提现信息
	 * @return 返回一个xml文件
	 * @throws TemplateException
	 * @throws IOException
	 */
	public static String withdrawalXml(WithdrawalInfo wi)
			throws IOException, TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("order_id", wi.getOrder_id());
		map.put("user_id", wi.getUser_id());
		map.put("amount", String.valueOf(wi.getAmount()));
		map.put("fee", String.valueOf(wi.getFee()));
		map.put("merchant_id", wi.getMerchant_id());
		map.put("fee_taken_on", wi.getFee_taken_on());
		map.put("return_url", wi.getReturn_url());
		map.put("page_url", wi.getPage_url());
		String tenderAuditxml = FreeMarkerUtil.execute("config/pay/withdrawal.flt", "UTF-8", map);
		return tenderAuditxml;

	}
	
	/***
	 * 服务端提现
	 * @param wi
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String withdrawServceXml(WithdrawalInfo wi)throws IOException, TemplateException{
		Map<String, String> map = new HashMap<String, String>();
		map.put("order_id", wi.getOrder_id());
		map.put("user_id", wi.getUser_id());
		map.put("amount", String.valueOf(wi.getAmount()));
		map.put("fee", String.valueOf(wi.getFee()));
		map.put("fee_taken_on", wi.getFee_taken_on());
		map.put("bank_no", wi.getBank_no());
		String withdrawServceXml=FreeMarkerUtil.execute("config/pay/withdrawServer.flt", "UTF-8", map);
		return withdrawServceXml;
	}
	/**
	 * 转账
	 * @param at
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String accttrans(AcctTrans at)
			throws IOException, TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", at.getMerchant_id());
		map.put("order_id", at.getOrder_id());
		map.put("payer_user_id", at.getPayer_user_id());
		map.put("payee_user_id", at.getPayee_user_id());
		map.put("payer_type", String.valueOf(at.getPayer_type()));
		map.put("payee_type", String.valueOf(at.getPayee_type()));
		map.put("amount", String.valueOf(at.getAmount()));
		map.put("fee", String.valueOf(at.getFee()));
		map.put("fee_taken_on", String.valueOf(at.getFee_taken_on()));
		map.put("req_time", String.valueOf(at.getReq_time()));
		String tenderAuditxml = FreeMarkerUtil.execute(
				"config/pay/acctTrans.flt", "UTF-8", map);
		return tenderAuditxml;
	}
	
	
	/**
	 * 组合投标信息为XML String
	 * @param BidInfo
	 * @return XML String
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String bidInfoXML(BidInfo bidInfo)
			throws IOException, TemplateException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("merchant_id", bidInfo.getMerchant_id());
		map.put("order_id", bidInfo.getOrder_id());
		map.put("action_type", String.valueOf(bidInfo.getAction_type()));
		map.put("cus_id", String.valueOf(bidInfo.getCus_id()));
		map.put("cus_name", bidInfo.getCus_name());
		map.put("brw_id", bidInfo.getBrw_id());
		map.put("req_time", bidInfo.getReq_time());
		map.put("user_id", bidInfo.getUser_id());
		map.put("user_name", bidInfo.getUser_name());
		map.put("amount", String.valueOf(bidInfo.getAmount()));
		map.put("actions",bidInfo.getActions());
		map.put("fee", String.valueOf(bidInfo.getFee()));
		String tenderAuditxml = FreeMarkerUtil.execute("config/pay/bid.flt", "UTF-8", map);
		return tenderAuditxml;
	}
	
	/**
	 * 绑定银行卡
	 * @param bidInfo
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String opBankCardXml(UserBank  bank,String type)throws IOException, TemplateException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", bank.getUserbasicsinfo().getpMerBillNo());
		map.put("type", type);
		map.put("bank_no",bank.getBank_no());
		map.put("pro_value", bank.getPro_value());
		map.put("city_value", bank.getCity_value());
		map.put("bank_name", bank.getBank_name());
		map.put("bank_address", bank.getBank_address());
		map.put("validate_code", bank.getValidate_code());
		String tenderAuditxml = FreeMarkerUtil.execute("config/pay/opBankCard.flt", "UTF-8", map);
		return tenderAuditxml;
	}
	
	/**
	 * 还标
	 * @param bi
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String bidinfo4XML(BidInfo4 bi)
			throws IOException, TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", bi.getMerchant_id());
		map.put("action_type", String.valueOf(bi.getAction_type()));
		map.put("order_id", bi.getOrder_id());
		map.put("cus_id", String.valueOf(bi.getCus_id()));
		map.put("cus_name", bi.getCus_name());
		map.put("brw_id", bi.getBrw_id());
		map.put("req_time", bi.getReq_time());
		String tenderAuditxml = FreeMarkerUtil.execute("config/pay/payment/bid_action_type4_1.flt", "UTF-8", map);
		
		for(Payuser payuser:bi.getPayuser()){
			map.put("user_id", payuser.getUser_id());
			map.put("amount", String.valueOf(payuser.getAmount()));
			map.put("fee", String.valueOf(payuser.getFee()));
			tenderAuditxml += FreeMarkerUtil.execute("config/pay/payment/bid_action_type4_2.flt", "UTF-8", map);
		}
		
		map.put("voucher_id", String.valueOf(bi.getVoucher_id()));
		map.put("voucher_fee", String.valueOf(bi.getVoucher_fee()));
		map.put("special", String.valueOf(bi.getSpecial()));
		tenderAuditxml += FreeMarkerUtil.execute("config/pay/payment/bid_action_type4_3.flt", "UTF-8", map);
	
		return tenderAuditxml;
	}
	
	
	/**
	 * 业务查询
	 * @param cardInfo
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String p2pQueryXml(P2pQuery p2pQuery)
			throws IOException, TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("order_id", p2pQuery.getOrder_id());
		map.put("type", String.valueOf(p2pQuery.getType()));
		String tenderAuditxml = FreeMarkerUtil.execute("config/pay/p2pQuery.flt", "UTF-8", map);
		return tenderAuditxml;
	}
	
	/**
	 * 业务查询
	 * @param cardInfo
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String p2pQueryTimeXml(P2pQuery p2pQuery)
			throws IOException, TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("type", String.valueOf(p2pQuery.getType()));
		map.put("start_time", p2pQuery.getStart_time());
		map.put("end_time", p2pQuery.getEnd_time());
		String tenderAuditxml = FreeMarkerUtil.execute("config/pay/p2pTimeQuery.flt", "UTF-8", map);
		return tenderAuditxml;
	}

	/**
	 * @param cardInfo
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String cardXml(CardInfo cardInfo)
			throws IOException, TemplateException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", cardInfo.getName());
		map.put("documentNo", cardInfo.getDocumentNo());
		map.put("subreportIDs", cardInfo.getSubreportIDs());
		map.put("refID", cardInfo.getRefID());
		String registrationxml = FreeMarkerUtil.execute(
				"config/pay/queryxml.flt", "UTF-8", map);
		return registrationxml;
	}

	/** 绑定银行卡(新接口) */
	public static String bindBankCardXml(UserBank bank)throws IOException, TemplateException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", bank.getUserbasicsinfo().getpMerBillNo());
		map.put("bank_no",bank.getBank_no());
		map.put("mobile", bank.getUserbasicsinfo().getUserrelationinfo().getPhone());
		map.put("validate_code", bank.getValidate_code());
		String tenderAuditxml = FreeMarkerUtil.execute("config/pay/bindBankCard.flt", "UTF-8", map);
		return tenderAuditxml;
	}
	
	/** 页面开通宝宝付相关  */
	public static String pageOpenBaofuXML(BaoFuAccountVo vo) throws IOException, TemplateException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("bf_account", vo.getBf_account());
		map.put("name", vo.getName());
		map.put("id_card", vo.getId_card());
		map.put("user_id", vo.getUser_id());
		map.put("return_url", vo.getReturn_url());
		map.put("page_url", vo.getPage_url());
		String sdkRechargeXml = FreeMarkerUtil.execute("config/pay/pageOpenBaoFu.flt", "UTF-8", map);
		return sdkRechargeXml;
	}
}
