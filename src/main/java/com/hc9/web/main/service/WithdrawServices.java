package com.hc9.web.main.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Withdraw;
import com.hc9.web.main.entity.WithdrawCard;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.PageModel;

/** 提现业务处理 */
@Service
public class WithdrawServices {

	@Resource
	private HibernateSupport dao;
	
	@Resource
	private LoanSignService loanSignService;

	/** 用户的提现信息 */
	public List<Withdraw> withdrawList(Long id, String beginTime,
			String endTime, Integer search, PageModel page) {

		StringBuffer sql = new StringBuffer(
				"SELECT * FROM withdraw w where w.user_id=" + id);
		StringBuffer sqlCount = new StringBuffer(
				"SELECT count(1) FROM withdraw w where w.user_id=" + id);

		if (StringUtil.isNotBlank(beginTime)) { // 开始时间
			sql.append(" and date_format(w.time,'%Y-%m-%d')>='").append(
					beginTime + "'");
			sqlCount.append(" and date_format(w.time,'%Y-%m-%d')>='")
					.append(beginTime + "'");
		}
		if (StringUtil.isNotBlank(endTime)) { // 结束时间
			sql.append(" and date_format(w.time,'%Y-%m-%d')<='").append(
					endTime + "'");
			sqlCount.append(" and date_format(w.time,'%Y-%m-%d')<='")
					.append(endTime + "'");
		}
		if (search != null && !"".equals(search)) { // 最近几个月
			sql.append(" and DATE_SUB(now(),INT	ERVAL " + search + " MONTH) <= w.time");
			sqlCount.append(" and DATE_SUB(now(),INTERVAL " + search + " MONTH) <= w.time");
		}
		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue()); // 设置总记录数
		sql.append(" order by w.id desc LIMIT " + (page.getPageNum() - 1)
				* page.getNumPerPage() + "," + page.getNumPerPage());
		List<Withdraw> list = dao.findBySql(sql.toString(), Withdraw.class);
		page.setList(list);
		return list;
	}

	/** 更新提现信息 */
	public void uptWithdraw(Withdraw withdraw) {
		dao.update(withdraw);
	}

	/** 修改提现券的状态为待确认
	 * 	@param userId 用户id
	 *  @param ordernum 订单号
	 *  @param useFlag 是否使用：0、未使用，1、已使用；2、待确认；默认为0
	 *  */
	public void updateWithdrawCardStatusAfterSuccess(Long userId, String ordernum) {
		String sql = "update withdrawcard set useFlag=1 where userId=? and useOrderNo=? and useFlag=2";
		dao.executeSql(sql, userId, ordernum);
	}
	
	/** 修改提现券的状态为待确认
	 * 	@param userId 用户id
	 *  @param ordernum 订单号
	 *  @param useFlag 是否使用：0、未使用，1、已使用；2、待确认；默认为0
	 *  */
	public void updateWithdrawCardStatusAfterFailure(Long userId, String ordernum) {
		String sql = "update withdrawcard set useFlag=0,consumeTime='',useOrderNo='' where userId=? and useOrderNo=? and useFlag=2";
		dao.executeSql(sql, userId, ordernum);
	}
	
	/** 根据id查询提现信息 */
	public Withdraw selWithdraw(String wId) {
		String sql = "select * from withdraw where id=?";
		Withdraw withdraw = dao.findObjectBySql(sql, Withdraw.class, wId);
		return withdraw;
	}

	/** 是否计算提现手续费 用户充值金额-用户累积投资金额>0收，<0则不收 */
	public Double selWithdrawMoney(String userID, String timeDate) {
		Double result = 0.0;
		if (StringUtil.isNotBlank(userID)) {
			/** 计算用户充值金额 */
			String sql = "select IFNULL(SUM(income),0) from accountinfo " 
					+ "where accounttype_id=6 and userbasic_id=? and " 
					+ "DATE_FORMAT(time, '%Y-%m-%d')>=DATE_FORMAT(?, '%Y-%m-%d') ";
			Object rechargeObject = dao.findObjectBySql(sql, userID, timeDate);
			double rechargeMoney = Double.valueOf(rechargeObject.toString());
			
			/**  计算用户累计投资金额 */
			sql = "select sum(IFNULL(tendermoney,0)) from loanrecord " 
					+ "where issucceed=1 and userbasicinfo_id=? and " 
					+ "DATE_FORMAT(tenderTime, '%Y-%m-%d')>=DATE_FORMAT(?, '%Y-%m-%d') ";
			Object investObject = dao.findObjectBySql(sql, userID, timeDate);
			double investMoney = 0.0;
			if(investObject!=null){
				investMoney = Double.valueOf(investObject.toString());
			}
			result = Arith.round(Arith.sub(rechargeMoney, investMoney), 2);
		}
		return result;
	}
	
	/**
	 * 收取的手续费
	 * 如果客户充值-投资差额-平台已收手续费>0,手续费计算:提现金*比例.替换原先的站岗金额*比例
	 * @param user 
	 * @param money 提现金额
	 * @return result 手续费
	 */
	public double takeWithdrawFee(Userbasicsinfo user,double money){
		double result=0;
		// 如果是用户借款人，不收手续费。否则按规则走
		if(user.getIsCreditor() == 2){
			return result;
		}else{
			Costratio costratio = loanSignService.queryCostratio();
			//商户 已扣除手续费相对应的金额
			Double tookFee = queryRelativeMoneyOfFee(user.getId());
			//充值-投资差额-平台已收手续费>0? 收：不收
			Double difference = selWithdrawMoney(user.getId().toString(), costratio.getTimeDate());
			difference = Arith.sub(difference, tookFee);//目标金额
			result = computeChargeFee(difference, money, costratio.getWithdrawRate());
			
			return result;
		}

	}
	
	/**
	 * 查询会员 已扣除手续费相对应的金额
	 * @param uid 用户id
	 * @return
	 */
	public Double queryRelativeMoneyOfFee(Long uid) {
		String sql="select SUM(IFNULL(amount,0)) from withdraw WHERE user_id=? AND state=1 and mer_fee>0";
		Object totalMerFee = dao.findObjectBySql(sql, uid);
		if(totalMerFee != null){
			return Double.parseDouble(totalMerFee.toString());
		}else{
			return 0.0;
		}
	}
	
	/** 
	 *  @param difference 目标金额：累计充值-累计认购-已扣除手续费相对应的金额
	 *  @param withDrawMoney 本次提现的金额 
	 *  @param withdrawRate 提现手续费
	 *  手续费收取逻辑如下：
	 *  1、目标金额大于0，则收取手续费；目标金额小于等于0，则不收取手续费。
	 *  1、当提现金额大于等于A，本次收取手续费金额为：A*1%+2元
	 *  2、当提现金额小于A，本次收取手续费金额为：提现金额*1%+2元 */
	private double computeChargeFee(Double difference, Double withDrawMoney, Double withdrawRate) {
		double result = 0;
		if(difference > 0) {
			if(withDrawMoney >= difference) {
				result = Arith.round(Arith.mul(difference, withdrawRate),2);
			} else {
				result = Arith.round(Arith.mul(withDrawMoney, withdrawRate),2);
			}
		}
		return result;
	}
	
	/** 查询最近一张快过期的提现券 */
	public WithdrawCard queryLatestWithdrawCard(long userId) {
		String sql = "select * from withdrawCard where useflag=0 and userid=? order by endtime desc,id asc";
		List<WithdrawCard> list = dao.findBySql(sql, WithdrawCard.class, userId);
		if(list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
}