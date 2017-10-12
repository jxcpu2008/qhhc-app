package com.hc9.web.main.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Loansignflow;
import com.hc9.web.main.entity.RepaymentRecordDetail;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.entity.Repaymentrecordparticulars;
import com.hc9.web.main.redis.IndexDataCache;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.vo.PageModel;
import com.hc9.web.main.vo.pay.Payuser;

/**
 * 对还款信息封装成对象(只封装了投资者信息)
 * 
 * @author RanQiBing 2014-03-30
 * 
 */
@Service
public class LoanManageService {

	@Resource
	private HibernateSupport dao;

	@Resource
	private LoanSignService loanSignService;

	public List<Loansignflow> getLoansignflow(Long userId, Long loanId,
			Integer loanType) {
		String hql2 = "FROM Loansignflow where userDebt=? and loanId=?  and loanType=? and auditResult=1";
		List<Loansignflow> loansignflows = new ArrayList<Loansignflow>();
		loansignflows = dao.find(hql2, userId, loanId, loanType);
		return loansignflows;
	}

	/**
	 * 普通标逾期还款违约利息 (逾期违约金额 = 借款金额*逾期利率*逾期天数)
	 * 
	 * @param money
	 *            借款金额
	 * @param scale
	 *            逾期利率
	 * @param day
	 *            逾期天数
	 * @return 返回逾期违约金额
	 */
	public Double overdueRepayment(Double money, int day) {
		String hql = "from Costratio c";
		List<Costratio> list = dao.find(hql);
		Costratio costratio = list.get(0);
		return Arith
				.mul(Arith.mul(money, costratio.getOverdueRepayment()), day);
	}

	/**
	 * 查找指定id的标
	 * 
	 * @return
	 */
	public List<Loansign> getList(Long id) {
		String hql = "from Loansign l WHERE ((l.loanType=1 AND l.refundWay=3) OR l.loanType=2 and l.loanType=3) AND l.loanstate=3 AND l.userbasicsinfo.id=?";
		List<Loansign> loanList = dao.find(hql, id);
		return loanList;
	}

	/**
	 * 得到发布中的借款给标
	 * 
	 * @param request
	 * @beginTime 开始时间
	 * @endTime 结束时间
	 * @return 返回页面路径
	 */
	public PageModel getAchieveLoan(HttpServletRequest request, Long userid,
			String beginTime, String endTime, PageModel page) {
		String sqlCount = "select count(l.id)";
		StringBuffer achieveSql = new StringBuffer(
				"SELECT s.loanNumber, s.loanTitle, l.issueLoan, l.rate, CASE WHEN l.refundWay = 1 THEN '按月等额本息' WHEN l.refundWay = 2 THEN '按月付息到期还本' ELSE '到期一次性还本息' END, l.`month`, l.publishTime, IFNULL(( SELECT sum(tenderMoney) FROM loanrecord WHERE loanrecord.loanSign_id = l.id ), 0 ) / issueLoan ");
		StringBuffer sql = new StringBuffer(
				" FROM loansign l,loansignbasics s WHERE l.id = s.id "
						+ "AND l.loanstate = 2 AND l.userbasicinfo_id = ")
				.append(userid);
		if (null != beginTime && !"".equals(beginTime)) {
			sql.append(" AND l.publishTime>='").append(beginTime).append("'");
		}
		if (null != endTime && !"".equals(endTime)) {
			sql.append(" AND l.publishTime<='").append(endTime).append("'");
		}
		sqlCount = sqlCount + sql.toString();
		achieveSql = achieveSql.append(sql);
		achieveSql
				.append(" LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* Constant.SRSRUES_TEN).append(",")
				.append(Constant.SRSRUES_TEN);
		page.setTotalCount(dao.queryNumberSql(sqlCount).intValue());
		List<Object[]> list = dao.findBySql(achieveSql.toString());
		page.setList(list);
		return page;
	}

	/**
	 * 得到还款中的借款给标
	 * 
	 * @param request
	 * @beginTime 开始时间
	 * @endTime 结束时间
	 * @return 返回页面路径
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public PageModel getRepaymentLoan(HttpServletRequest request, Long userid,
			String beginTime, String endTime, PageModel page, String month)
			throws ParseException {
		String sqlCount = "select count(r.id) ";
		StringBuffer repaymentSql = new StringBuffer(
				"SELECT r.id, s.loanNumber, s.loanTitle, l.issueLoan, l.rate, CASE WHEN l.refundWay = 1"
						+ " THEN '按月等额本息' WHEN l.refundWay = 2 THEN '按月付息到期还本' ELSE '到期一次性还本息' END, l.`month`,"
						+ " l.useDay, s.creditTime, r.preRepayDate, SUM(r.money + r.preRepayMoney),r.repayState ");
		StringBuffer sql = new StringBuffer(
				"FROM loansign l, repaymentrecord r, loansignbasics s WHERE l.id = r.loanSign_id AND l.id = s.id AND"
						+ " l.loanstate = 3 AND l.userbasicinfo_id = ")
				.append(userid);
		if (null != month && !"".equals(month)) {
			if (Integer.parseInt(month) > 0) {
				String date = DateUtils.add("yyyy-MM-dd", Calendar.MONTH,
						Integer.parseInt(month));
				sql.append(" and r.preRepayDate<='").append(date).append("'");
			}
		}
		if (null != beginTime && !"".equals(beginTime)) {
			sql.append(" and r.preRepayDate>='").append(beginTime).append("'");
		}
		if (null != endTime && !"".equals(endTime)) {
			sql.append(" and r.preRepayDate<='").append(endTime).append("'");
		}

		sqlCount = sqlCount + sql.toString();
		sql.append(" GROUP BY r.id");
		repaymentSql.append(sql);
		repaymentSql
				.append(" LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* Constant.SRSRUES_TEN).append(",")
				.append(Constant.SRSRUES_TEN);
		page.setTotalCount(dao.queryNumberSql(sqlCount).intValue());
		List<Object[]> list = dao.findBySql(repaymentSql.toString());
		page.setList(list);
		return page;
	}

	// /**
	// * 得到逾期中的借款给标
	// *
	// * @param request
	// * @beginTime 开始时间
	// * @endTime 结束时间
	// * @return 返回页面路径
	// */
	// @SuppressWarnings("unchecked")
	// public PageModel getOverdueLoan(HttpServletRequest request,Long userid,
	// String beginTime, String endTime,PageModel page) {
	// StringBuffer sqlCount = new StringBuffer("select count(r.id)");
	// StringBuffer oberdueSql = new
	// StringBuffer("SELECT r.id, s.loanNumber, s.loanTitle, l.issueLoan, l.rate, CASE WHEN l.refundWay = 1 THEN '按月等额本息' WHEN l.refundWay = 2 THEN '按月付息到期还本' ELSE '到期一次性还本息' END, l.`month`, l.useDay, s.creditTime, r.preRepayDate, SUM(r.money + r.preRepayMoney)");
	// StringBuffer sql = new
	// StringBuffer(" FROM loansign l, repaymentrecord r, loansignbasics s WHERE l.id = r.loanSign_id AND l.id = s.id AND ( r.repayState = 1 OR r.repayState = 3 ) AND r.preRepayDate < '").append(DateUtils.format("yyyy-MM-dd")).append("' AND l.userbasicinfo_id = ").append(userid);
	// if(null!=beginTime&&!"".equals(beginTime)){
	// sql.append(" and r.r.preRepayDate>='").append(beginTime).append("'");
	// }
	// if(null!=endTime&&!"".equals(endTime)){
	// sql.append(" and r.r.preRepayDate<='").append(endTime).append("'");
	// }
	// sql.append(" GROUP BY r.id");
	// sqlCount.append(sql);
	// oberdueSql.append(sql);
	// oberdueSql.append(" LIMIT ").append((page.getPageNum()-Constant.STATUES_ONE)*Constant.SRSRUES_TEN).append(",").append(Constant.SRSRUES_TEN);
	// page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue());
	// List<Object[]> list= dao.findBySql(oberdueSql.toString());
	// page.setList(list);
	// return page;
	// }
	/**
	 * 得到还款中的借款给标
	 * 
	 * @param request
	 * @beginTime 开始时间
	 * @endTime 结束时间
	 * @return 返回页面路径
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public PageModel getHasTheRepaymentLoan(HttpServletRequest request,
			Long userid, String beginTime, String endTime, PageModel page)
			throws ParseException {
		String sqlCount = "select count(r.id)";
		StringBuffer repaymentSql = new StringBuffer(
				"SELECT r.id, s.loanNumber, s.loanTitle, l.issueLoan, l.rate, CASE WHEN l.refundWay = 1"
						+ " THEN '按月等额本息' WHEN l.refundWay = 2 THEN '按月付息到期还本' ELSE '到期一次性还本息' END, l.`month`,"
						+ " l.useDay, s.creditTime, r.preRepayDate, SUM(r.money + r.preRepayMoney), r.overdueInterest ");
		StringBuffer sql = new StringBuffer(
				"FROM loansign l, repaymentrecord r, loansignbasics s WHERE l.id = r.loanSign_id AND l.id = s.id AND"
						+ " l.loanstate!=4 and (r.repayState != 1 and r.repayState!=3) AND l.userbasicinfo_id = ")
				.append(userid);
		if (null != beginTime && !"".equals(beginTime)) {
			sql.append(" and r.preRepayDate>='").append(beginTime).append("'");
		}
		if (null != endTime && !"".equals(endTime)) {
			sql.append(" and r.preRepayDate<='").append(endTime).append("'");
		}
		sqlCount = sqlCount + sql.toString();

		repaymentSql.append(sql);
		sql.append(" GROUP BY r.id");
		repaymentSql
				.append(" LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* Constant.SRSRUES_TEN).append(",")
				.append(Constant.SRSRUES_TEN);
		page.setTotalCount(dao.queryNumberSql(sqlCount).intValue());
		List<Object[]> list = dao.findBySql(repaymentSql.toString());
		page.setList(list);
		return page;
	}

	/**
	 * 得到已完成的借款给标
	 * 
	 * @param request
	 * @beginTime 开始时间
	 * @endTime 结束时间
	 * @return 返回页面路径
	 */
	@SuppressWarnings("unchecked")
	public PageModel getUnderwayLoan(HttpServletRequest request, Long userid,
			String beginTime, String endTime, PageModel page) {
		String sqlCount = "select count(l.id) from loansign l where l.loanstate=4 and l.userbasicinfo_id=?";
		StringBuffer sql = new StringBuffer(
				"SELECT s.loanNumber, s.loanTitle,l.issueLoan,l.rate,CASE WHEN l.refundWay = 1 THEN '按月等额本息' WHEN l.refundWay = 2 THEN '按月付息到期还本' ELSE '到期一次性还本息' END, l.`month`, l.useDay FROM loansign l,loansignbasics s WHERE l.id = s.id AND l.loanstate = 4 AND l.userbasicinfo_id=")
				.append(userid);
		sql.append(" LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* Constant.SRSRUES_TEN).append(",")
				.append(Constant.SRSRUES_TEN);
		page.setTotalCount(dao.queryNumberSql(sqlCount, userid).intValue());
		List<Object[]> list = dao.findBySql(sql.toString());
		page.setList(list);
		return page;
	}

	/**
	 * 成功案例:项目
	 * 
	 * @param month
	 *            月份
	 * @param loanstate
	 *            状态
	 * @param type
	 *            类型
	 * @param page
	 *            分页对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PageModel getloanList(String loanType, String city, String money,
			PageModel page) {

		StringBuffer sql = new StringBuffer(
				"select ls.id,ls.`name`,s.issueLoan,s.loanimg,ls.status,"
						+ " ls.remoney,ls.getMoneyWay,ls.remark,ls.state ");

		StringBuffer sqlCount = new StringBuffer("select count(ls.id) ");

		StringBuffer sqlsb = new StringBuffer(
				" FROM loansign ls,loansignbasics s WHERE ls.id = s.id AND  ls.status=4");

		if (!"".equals(loanType) && null != loanType) {
			sqlsb.append(" and ls.loanType=")
					.append(Integer.parseInt(loanType));
		}
		if (!"".equals(city) && null != city) {
			sqlsb.append(" and ls.city= '").append(city).append("'");
		}
		if (!"".equals(money) && null != money) {
			if (Integer.parseInt(money) == 1) {
				sqlsb.append(" and s.issueLoan<=").append(100000);
			}
			if (Integer.parseInt(money) == 2) {
				sqlsb.append(" and s.issueLoan>").append(100000)
						.append(" and s.issueLoan<=").append(1000000);
			}
			if (Integer.parseInt(money) == 3) {
				sqlsb.append(" and ls.issueLoan>=").append(1000000)
						.append(" and s.issueLoan<=").append(2000000);
			}

			if (Integer.parseInt(money) == 4) {
				sqlsb.append(" and s.issueLoan>").append(2000000);

			}
		}

		sqlsb.append(" order by ls.id desc,ls.state asc");

		page.setTotalCount(dao
				.queryNumberSql(sqlCount.append(sqlsb).toString()).intValue());

		sqlsb.append(" LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* Constant.STATUES_THERE).append(",")
				.append(page.getNumPerPage());
		List<Loansign> list = dao.findBySql(sql.append(sqlsb).toString());
		page.setList(list);
		return page;
	}

	/**
	 * 获取项目
	 * 
	 * @param month
	 * @param loanstate
	 * @param type
	 * @param choutype
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PageModel getLoanList(Integer type, Integer state, Integer month,
			Integer rate, PageModel page) {

		StringBuffer sql = new StringBuffer(
				"select ls.id, ls.name,ls.remonth,ls.issueLoan,ls.loanUnit,ls.rest_money,ls.prio_rate,ls.prio_aword_rate,ls.type,ls.status,ls.loansignType_id,ls.activityStatus "
						+ "from loansign ls where ls.state=2  ");
		StringBuffer sqlCount = new StringBuffer(
				"select count(ls.id) from loansign ls where ls.state=2  ");
		StringBuffer sqlsb = new StringBuffer(" ");

		if (type != null) {
			sqlsb.append(" and ls.loansignType_id=").append(type);
		}

		if (rate != null) {
			if (rate == 8) {
				sqlsb.append(" and ls.prio_rate>").append(0.08)
						.append(" and ls.prio_rate<=").append(0.12);
			} else if (rate == 12) {
				sqlsb.append(" and ls.prio_rate>").append(0.12)
						.append(" and ls.prio_rate<= ").append(0.15);
			}

		}

		if (month != null) {
			sqlsb.append(" and ls.remonth=").append(month);
		}

		if (null != state) { // 金额
			if (state == 1) {
				sqlsb.append(" and ls.status in(1,2,3,4,5) ");
			} else if (state == 2) {
				sqlsb.append(" and ls.status in(6,7) ");
			} else if (state == 3) {
				sqlsb.append(" and ls.status in(8) ");
			}

		} else {
			sqlsb.append(" and  ls.status !=0 and ls.status !=9");
		}
		StringBuffer sqlsb1 = new StringBuffer(" ORDER BY ls.rest_money DESC,ls.publish_time desc ");

		page.setTotalCount(dao.queryNumberSql(
				sqlCount.append(sqlsb).append(sqlsb1).toString()).intValue());

		StringBuffer sqlsb2 = new StringBuffer(" LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* page.getNumPerPage()).append(",")
				.append(page.getNumPerPage());
		List<Loansign> list = dao.findBySql(sql.append(sqlsb).append(sqlsb1)
				.append(sqlsb2).toString());

		page.setList(list);
		return page;
	}

	/**
	 * H5获取项目
	 * 
	 * @param month
	 * @param loanstate
	 * @param type
	 * @param choutype
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PageModel getLoanList(PageModel page) {
		
		StringBuffer sqlCount = new StringBuffer();
		sqlCount.append(" select count(ls.id) from loansign ls where ls.state=2 ");
		sqlCount.append(" and  ls.status !=0 and ls.status !=9 ");
		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue());
		
		StringBuffer sql = new StringBuffer();
		sql.append(" select ls.id, ls.name,ls.remonth,ls.issueLoan,ls.loanUnit,ls.rest_money,ls.prio_rate,ls.prio_aword_rate,ls.type,ls.status,ls.loansignType_id,ls.activityStatus ");
		sql.append(" from loansign ls where ls.state=2 ");
		sql.append(" and ls.status !=0 and ls.status !=9 ");
		sql.append(" ORDER BY ls.rest_money DESC,ls.publish_time desc ");
		sql.append(" LIMIT ");
		sql.append((page.getPageNum() - Constant.STATUES_ONE)* page.getNumPerPage());
		sql.append(",");
		sql.append(page.getNumPerPage());
		
		List<Loansign> list = dao.findBySql(sql.toString());
		page.setList(list);
		
		return page;
	}
	
	/**
	 * H5热门推荐
	 * 
	 * @param month
	 * @param loanstate
	 * @param type
	 * @param choutype
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List getLoanRecommandList(String key) {
		StringBuffer sql = new StringBuffer(
				"select ls.id, ls.name,ls.remonth,ls.issueLoan,ls.loanUnit,ls.rest_money,ls.prio_rate,ls.prio_aword_rate,ls.type,ls.status,ls.loansignType_id,ls.activityStatus "
						+ "from loansign ls  where ls.state=2  ");
		sql.append(" and (ls.status >0 and ls.status <9) and ls.recommend=1");
		sql.append(" ORDER BY ls.rest_money DESC,ls.publish_time desc ");
		sql.append(" LIMIT 0,2");
		List loanRecommandList = dao.findBySql(sql.toString());
        try{
        	IndexDataCache.set(key, loanRecommandList);
        }catch(Exception e){
        	LOG.error("更新缓存失败："+e);
        }
		return loanRecommandList;
	}

	/**
	 * 获取上期还款滞纳金
	 * 
	 * @param upPeriodsNum
	 * @param repaymentInfo
	 */
	public double getupPeridosMoney(int upPeriodsNum,
			Repaymentrecord repaymentInfo) {
		double upperidosMoney = 0.00;
		String sql = "SELECT * from repaymentrecord  r WHERE r.loanSign_id=?   and r.periods="
				+ upPeriodsNum + "";
		List<Repaymentrecord> list = dao.findBySql(sql, Repaymentrecord.class,
				repaymentInfo.getLoansign().getId());
		try {
			if (list != null) {
				if (list.get(0).getRepayState() == 4) {
					int diffNum = DateUtils.differenceDateSimple(list.get(0)
							.getPreRepayDate(), list.get(0).getRepayTime());
					if (diffNum > 0) {
						System.out.println("出现滞纳");
						upperidosMoney = repaymentInfo.getLoansign()
								.getIssueLoan() * 0.24 / 365 * diffNum;
					}
				}
			}
		} catch (Exception e) {

		}

		return upperidosMoney;
	}

	/**
	 * 获取上期还款滞纳天数
	 * 
	 * @param upPeriodsNum
	 * @param repaymentInfo
	 */
	public Map<String, String> getupPeridosDateAndMoney(
			Repaymentrecord repaymentInfo, Costratio cost) {
		Map<String, String> map = new HashMap<String, String>();
		Double priOut = 0.0; // 优先滞纳金
		Double midOut = 0.0; // 夹层滞纳金
		Double aftOut = 0.0;// 劣后滞纳金
		// Double feeOut=0.0;//平台收取滞纳金
		int diffNum = 0;
		/**逾期滞纳金比例*/
		Double overdueRepayment = cost.getOverdueRepayment();
		if (repaymentInfo.getLoansign().getType() == 3) {
			// 获取天标滞纳天数
			String sql = "SELECT * from repaymentrecord  r WHERE r.loanSign_id=? ";
			List<Repaymentrecord> list = dao.findBySql(sql,
					Repaymentrecord.class, repaymentInfo.getLoansign().getId());
			try {
				if (list != null) {
					if (list.get(0).getRepayState() == 1) {
						diffNum = DateUtils.differenceDateSimple(list.get(0)
								.getPreRepayDate(), DateUtils
								.formatSimple(new Date()));
						if (diffNum > 0) {
							LOG.error("出现滞纳");
							/** 需要计算滞纳金的优先金额 */
							Double preTotal = Arith.add(repaymentInfo.getMoney(), 
									repaymentInfo.getPreRepayMoney());
							priOut = Arith.round(BigDecimal.valueOf(preTotal * overdueRepayment * diffNum).doubleValue(), 2);// 优先滞纳金
							
							/** 需要计算滞纳金的夹层金额 */
							Double midTotal = Arith.add(
									repaymentInfo.getMiddleMoney(), 
									repaymentInfo.getMiddlePreRepayMoney());
							midOut = Arith.round(BigDecimal.valueOf(midTotal * 
									overdueRepayment * diffNum).doubleValue(), 2);// 夹层滞纳金
							
							/** 需要计算滞纳金的劣后金额 */
							Double afterTotal = Arith.add(
									repaymentInfo.getAfterMoney(), repaymentInfo.getAfterPreRepayMoney());
							aftOut = Arith.round(
									BigDecimal.valueOf(afterTotal * overdueRepayment * diffNum).doubleValue(), 2);// 劣后滞纳金
							// feeOut=
							// Arith.round(BigDecimal.valueOf(list.get(0).getCompanyPreFee()*
							// cost.getOverdueRepayment()* diffNum),
							// 2).doubleValue(); //计算平台服务费
							LOG.error("滞纳天数=" + diffNum + "优先滞纳金=" + priOut 
									+ "，夹层滞纳金=" + midOut + "，劣后滞纳金=" + aftOut);
						}
					}
				}
			} catch (Exception e) {
				LOG.error("计算天标滞纳金过程中出错,还款记录id:" + repaymentInfo.getId() + "！", e);
			}
		} else if (repaymentInfo.getLoansign().getType() == 2) {
			Integer periods = repaymentInfo.getPeriods();
			String sql = "SELECT * from repaymentrecord  r WHERE r.loanSign_id=? and periods="
					+ periods + " ";
			List<Repaymentrecord> list = dao.findBySql(sql,
					Repaymentrecord.class, repaymentInfo.getLoansign().getId());
			try {
				if (list != null) {
					if (list.get(0).getRepayState() == 1) {
						diffNum = DateUtils.differenceDateSimple(list.get(0)
								.getPreRepayDate(), DateUtils
								.formatSimple(new Date()));
						if (diffNum > 0) {
							LOG.error("出现滞纳");
							Repaymentrecord repayVo = list.get(0);
							/** 需要计算滞纳金的优先金额 */
							Double preTotal = Arith.add(repayVo.getMoney(), repayVo.getPreRepayMoney());
							priOut = Arith.round(
									BigDecimal.valueOf(preTotal * overdueRepayment * diffNum).doubleValue(), 2);// 优先滞纳金
							/** 需要计算滞纳金的夹层金额 */
							Double midTotal = Arith.add(
									repayVo.getMiddleMoney(), repayVo.getMiddlePreRepayMoney());
							midOut = Arith.round(
									BigDecimal.valueOf(midTotal * overdueRepayment * diffNum).doubleValue(),
									2);// 夹层滞纳金
							/** 需要计算滞纳金的劣后金额 */
							Double afterTotal = Arith.add(
									repayVo.getAfterMoney(), repayVo.getAfterPreRepayMoney());
							aftOut = Arith.round(
									BigDecimal.valueOf(afterTotal * overdueRepayment * diffNum).doubleValue(), 2);// 劣后滞纳金
							// feeOut=
							// Arith.round(BigDecimal.valueOf(list.get(0).getCompanyPreFee()*
							// cost.getOverdueRepayment()* diffNum),
							// 2).doubleValue(); //计算平台服务费
							LOG.error("滞纳天数=" + diffNum + "优先滞纳金="
									+ priOut + "，夹层滞纳金=" + midOut + "，劣后滞纳金="
									+ aftOut);
						}
					}
				}
			} catch (Exception e) {
				LOG.error("计算项目滞纳金过程中出错,还款记录id:" + repaymentInfo.getId() + "！", e);
			}
		}
		map.put("priOut", String.valueOf(priOut));
		map.put("midOut", String.valueOf(midOut));
		map.put("aftOut", String.valueOf(aftOut));
		// map.put("feeOut", String.valueOf(feeOut));
		map.put("total", String.valueOf(priOut + midOut + aftOut));
		map.put("diffNum", String.valueOf(diffNum));
		return map;
	}

	/** 计算需要需要还款的总金额 */
	public double getTotalNeedRepaymentMoney(Repaymentrecord repaymentrecord, Costratio costratio) {
		double needMoney =0;
		// 得到滞纳金, 滞纳天数
		Map<String, String> map = getupPeridosDateAndMoney(repaymentrecord, costratio);
		double sumOverdueMoney = Double.parseDouble(map.get("total").toString());
		// 需要金额
		needMoney = Arith.add(repaymentrecord.getMoney(), repaymentrecord.getMiddleMoney());
		needMoney = Arith.add(needMoney, repaymentrecord.getAfterMoney());
		needMoney = Arith.add(needMoney, repaymentrecord.getPreRepayMoney());
		needMoney = Arith.add(needMoney, repaymentrecord.getMiddlePreRepayMoney());
		needMoney = Arith.add(needMoney, repaymentrecord.getAfterPreRepayMoney());
		needMoney = Arith.add(needMoney, sumOverdueMoney);
		return needMoney;
	}
	
	/**
	 * 获取天标滞纳金
	 * 
	 * @param repaymentInfo
	 * @return
	 */
	public double getDayPeridosMoney(Repaymentrecord repaymentInfo) {
		double upperidosMoney = 0.00;
		String sql = "SELECT * from repaymentrecord  r WHERE r.loanSign_id=? ";
		List<Repaymentrecord> list = dao.findBySql(sql, Repaymentrecord.class,
				repaymentInfo.getLoansign().getId());
		try {
			if (list != null) {
				if (list.get(0).getRepayState() == 1) {
					int diffNum = DateUtils.differenceDateSimple(list.get(0)
							.getPreRepayDate(), list.get(0).getRepayTime());
					if (diffNum > 0) {
						System.out.println("出现滞纳");
						upperidosMoney = repaymentInfo.getLoansign()
								.getIssueLoan() * 0.24 / 360 * diffNum;
					}
				}
			}
		} catch (Exception e) {

		}
		return upperidosMoney;
	}

	/***
	 * 根据Id和类型得到count
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	public Integer getRecordparticularsCount(Long id, Integer type) {
		String sql = "select count(1) from repaymentrecordparticulars rcp where rcp.repaymentrecordId="
				+ id + " and rcp.loanType=" + type + "";
		return loanSignService.queryCount(sql);
	}

	/**
	 * 得到当期 还款详情
	 * 
	 * @param repaymentrecord
	 * @return
	 */
	public List<Repaymentrecordparticulars> getRepaymentrecordparticulars(
			Repaymentrecord repaymentrecord) {
		String sql = "SELECT * from repaymentrecordparticulars rcp where rcp.repaymentrecordId=?  and rcp.repState=-1 order by userid asc";
		return dao.findBySql(sql, Repaymentrecordparticulars.class,
				repaymentrecord.getId());
	}
	
	/**
	 * 获取当前还款详情记录列表
	 * @param repayRecordId
	 * @return
	 */
	public List<RepaymentRecordDetail> getRepaymentRecordDetails(long repayRecordId) {
		String sql = "select * from repaymentrecordparticulars rrp where rrp.repaymentrecordId = ? and rrp.repState = -1 order by rrp.userid asc";
		return dao.findBySql(sql, RepaymentRecordDetail.class, repayRecordId);
	}
	
	/**
	 * 获取还款记录列表
	 * @param repayRecordId
	 * @return
	 */
	public List<Repaymentrecord> getRepayRecords(long loanId) {
		String sql = "select * from repaymentrecord where repaymentrecord.loanSign_id = ?";
		return dao.findBySql(sql, Repaymentrecord.class, loanId);
	}
	
	/**
	 * 查询用户成功投资列表
	 * @param loanId
	 * @return
	 */
	public List<Loanrecord> getLoanRecordList(long loanId) {
		String sql = " select * from loanrecord where isSucceed = 1 and loanSign_id = ? ";
		return dao.findBySql(sql, Loanrecord.class, loanId);
	}

	/***
	 * 更新userfundinfo表
	 */
	public void updateOperationMoney() {
		String sql = "update userfundinfo set operation_money=cashBalance";
		dao.executeSql(sql);
	}

	public Repaymentrecordparticulars getParticulars(Payuser payuserinfo) {
		String sql = "select * from repaymentrecordparticulars rcp where rcp.id=? and rcp.repState=0 ";
		List list = dao.findBySql(sql, Repaymentrecordparticulars.class,
				payuserinfo.getrId());
		return (Repaymentrecordparticulars) list.get(0);
	}

	public double getRealInterest(Repaymentrecord repaymentrecord, int type) {
		String sql = "SELECT * from repaymentrecordparticulars rcp where rcp.repaymentrecordId=? and rcp.loanType=? LIMIT 1";
		List list = dao.findBySql(sql, Repaymentrecordparticulars.class,
				repaymentrecord.getId(), type);
		Repaymentrecordparticulars rpc = null;
		if (list.size() <= 0) {
			return 0.0;
		}
		rpc = (Repaymentrecordparticulars) list.get(0);
		Loansign loansign = repaymentrecord.getLoansign();
		Loanrecord loanrecord = rpc.getLoanrecord();
		if (type == 1) {
			return (rpc.getPreRepayMoney() * loansign.getPriority())
					/ loanrecord.getTenderMoney();
		}
		if (type == 2) {
			if (loansign.getMiddle() == 0) {
				return 0.0;
			} else {
				return (rpc.getMiddlePreRepayMoney() * loansign.getMiddle())
						/ loanrecord.getTenderMoney();
			}
		}
		if (type == 3) {
			return (rpc.getAfterPreRepayMoney() * loansign.getAfter())
					/ loanrecord.getTenderMoney();
		}
		return 0.0;
	}

	/**
	 * 更新还款明细状态
	 * 
	 * @param info
	 */
	public void updateRepaymentrecordParticulars(Repaymentrecordparticulars info) {
		System.out.println(info.getId());
		dao.update(info);
	}

	/**
	 * 是否有还款遗漏
	 * 
	 * @param lonid
	 * @return
	 */
	public double getNotLiquidationCountByLoanid(String lonid) {
		String sql = "SELECT count(*) from liquidation  where liquidation.loanId=? and liquidationState!=1 ";
		return dao.queryNumberSql(sql, lonid);
	}

	/**
	 * 根据标 ID 更新 标的 标的状态 为清盘 -1 & 更新 还款状态 为清盘 -1
	 * 
	 * @param lonid
	 */
	public void updateLoansignAndRepaymentrecordByLoanid(String lonid) {
		// 更新标的状态
		String sql = "UPDATE repaymentrecord  r SET  r.repayState=-1    where  r.loanSign_id=? and r.repayState=1";
		// 更新还款计划状态
		String sql2 = "UPDATE loansign l set l.status =-1 WHERE l.id=?";
		dao.executeSql(sql, lonid);
		dao.executeSql(sql2, lonid);
	}

	/**
	 * 根据标ID 判断当前 标是否有生成 清盘计划
	 * 
	 * @param lonid
	 * @return
	 */
	public double getLoanginLiquidationPlanCount(String lonid) {
		String sql = "SELECT count(*) from liquidation  where liquidation.loanId=? ";
		return dao.queryNumberSql(sql, lonid);
	}

	/**
	 * 根据标的信息或者还没有还款的计划数据
	 * 
	 * @param loansign
	 * @return
	 */
	public List<Repaymentrecord> getNotRepaymentRecords(Loansign loansign) {
		String sql = "SELECT * from repaymentrecord WHERE repaymentrecord.repayState=1 and repaymentrecord.loanSign_id=?";
		return dao.findBySql(sql, Repaymentrecord.class, loansign.getId());
	}
	
	/***
	 * 修改repaymentrecordparticulars表状态
	 * @param repaymentrecordId
	 */
	public void uptRepaymentrecordparticulars(Long repaymentrecordId) {
		String sql = "update repaymentrecordparticulars set  repState=1 where repaymentrecordId=?";
		dao.executeSql(sql, repaymentrecordId);
	}
	
	/***
	 * 更新还款信息
	 * @param repayMentRecordId
	 */
	public void updateRepaymen(Long repayMentRecordId){
		String sql="update repaymentrecord  set realMoney=0,middleRealMoney=0,afterRealMoney=0,companyRealFee=0,repayState=1 where id=?";
		dao.executeSql(sql, repayMentRecordId);
		String sqlRe = "update repaymentrecordparticulars set  repState=-1,realMoney=0,middleRealMoney=0,afterRealMoney=0 where repaymentrecordId=?";
		dao.executeSql(sqlRe, repayMentRecordId);
	}

}
