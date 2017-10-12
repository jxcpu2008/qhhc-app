package com.hc9.web.main.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Paylog;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.LOG;
/**
 * 交易报文记录工具
 * @author frank
 *
 */
@Service
public class PayLogService {
	
	@Resource
	private HibernateSupport dao;
	
	private static Map<Integer,String> OPER;
	
	static{
		OPER=new HashMap<>();
		OPER.put(1, "绑定用户");
		OPER.put(2, "充值");
		OPER.put(3, "提现");
		OPER.put(4, "投标");
		OPER.put(5, "满标");
		OPER.put(6, "流标");
		OPER.put(7, "还款");
		OPER.put(8, "充值查询");
		OPER.put(9, "提现查询");
		OPER.put(10, "投标查询");
		OPER.put(11, "满标查询");
		OPER.put(12, "流标查询");
		OPER.put(13, "还款查询");
		OPER.put(14, "清盘");
		OPER.put(15, "清盘查询");
		OPER.put(16, "用户授权");
		OPER.put(17, "用户查询");
		OPER.put(18, "第三方担保充值");
		OPER.put(19, "第三方担保提现");
		OPER.put(20, "佣金转账");
		OPER.put(21, "收取平台手续费");
		OPER.put(22, "绑定银行卡");
		OPER.put(23, "发送手机验证码");
		OPER.put(24, "奖励发放");
		OPER.put(25, "退款");
		OPER.put(26, "佣金转账查询");
		OPER.put(27, "红包转账");
		OPER.put(28, "加息奖励");
		OPER.put(29, "现金转账");
		OPER.put(30, "奖励补贴");
	}
	
	 
	/**
	 * 保存店铺
	 * @param log  		 报文
	 * @param userId	用户ID
	 * @param action	操作动作
	 * @param orderSn	订单号
	 * @param shopId	店铺Id
	 * @param fee		平台手续费
	 * @param payfee 	宝付平台收取的手续费
	 * @param amount	应收金额
	 * 
	 * @param paylog
	 */
	public void savePayLog(String log, long userId, int action,String orderSn,long shopId,double fee,double payfee,double amount){
		Paylog paylog=new Paylog();
		paylog.setAction(OPER.get(action));
		paylog.setMessage(log);
		paylog.setOrderSn(orderSn);
		paylog.setStatus(Constant.STATUES_ZERO);
		paylog.setUserId(userId);
		paylog.setSendTime(DateUtils.formatSimple());//当前时间
		paylog.setShopId(shopId);
		paylog.setFee(fee);
		paylog.setPayFee(payfee);
		paylog.setAmount(amount);
		dao.save(paylog);
	}
	
	/***
	 * 
	 * @param log 信息
	 * @param userId 用户
	 * @param action 类型
	 */
	public void savePayLog(String log ,long userId,int action){
		Paylog paylog=new Paylog();
		paylog.setAction(OPER.get(action));
		paylog.setMessage(log);
		paylog.setUserId(userId);
		paylog.setStatus(Constant.STATUES_ZERO);
		paylog.setSendTime(DateUtils.formatSimple());//当前时间
		dao.save(paylog);
	}
	
	/***
	 * 保存项目
	 * @param log  报文
	 * @param userId  用户ID
	 * @param loansignId   项目ID
	 * @param action  类型
	 * @param orderSn  订单号
	 * @param fee    应付手续费
	 * @param payfee  实付手续费
	 * @param amount   购买基恩
	 */
	public void savePayLog(String log, long userId, long loansignId,int action,String orderSn,double fee,double payfee,double amount){
		Paylog paylog=new Paylog();
		paylog.setAction(OPER.get(action));
		paylog.setMessage(log);
		paylog.setOrderSn(orderSn);
		paylog.setStatus(Constant.STATUES_ZERO);
		paylog.setUserId(userId);
		paylog.setSendTime(DateUtils.formatSimple());//当前时间
		paylog.setLoansignId(loansignId);
		paylog.setFee(fee);
		paylog.setPayFee(payfee);
		paylog.setAmount(amount);
		LOG.error("支付记录日志：" + JsonUtil.toJsonStr(paylog));
		dao.save(paylog);
	}
	
	
	/***
	 * 保存项目
	 * @param log  报文
	 * @param userId  用户ID
	 * @param loansignId   项目ID
	 * @param action  类型
	 * @param orderSn  订单号
	 * @param fee    应付手续费
	 * @param payfee  实付手续费
	 * @param amount   
	 * @param repaymentrecordId  还款Id
	 */
	public void savePayLog(String log, long userId, long loansignId,int action,String orderSn,double fee,double payfee,double amount,long repaymentrecordId){
		Paylog paylog=new Paylog();
		paylog.setAction(OPER.get(action));
		paylog.setMessage(log);
		paylog.setOrderSn(orderSn);
		paylog.setStatus(Constant.STATUES_ZERO);
		paylog.setUserId(userId);
		paylog.setSendTime(DateUtils.formatSimple());//当前时间
		paylog.setLoansignId(loansignId);
		paylog.setFee(fee);
		paylog.setPayFee(payfee);
		paylog.setAmount(amount);
		paylog.setShopId(repaymentrecordId);
		LOG.error("支付记录日志：" + JsonUtil.toJsonStr(paylog));
		dao.save(paylog);
	}
	
	/**
	 * 查询是否存在订单号，并且更新
	 * @param ddh
	 */
	public void updatePayLog(String orderSn,Integer status) {
		Paylog paylog=queryPaylogByOrderSn(orderSn);
		paylog.setUpdateTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		paylog.setStatus(status);
		dao.update(paylog);
	}
	/**
	 * 通过订单号查询
	 * @param orderSn
	 * @return
	 */
	public Paylog queryPaylogByOrderSn(String orderSn){
		String hql="from Paylog where orderSn = '" + orderSn+"'";
		return dao.find(hql).size()>0?(Paylog) dao.find(hql).get(0):null;
	}
	
	
	/**
	 * 充值与提现
	 * 查询是否存在订单号，并且更新
	 * @param ddh
	 */
	public void updatePayLog(String orderSn,Integer status,Double payAmount,Double payFee) {
		Paylog paylog=queryPaylogByOrderSn(orderSn);
		paylog.setUpdateTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		paylog.setStatus(status);
		paylog.setPayAmount(payAmount);
		paylog.setPayFee(payFee);
		dao.update(paylog);
	}
}