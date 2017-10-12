package com.hc9.web.main.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Userfundinfo;
import com.hc9.web.main.entity.Usermessage;
import com.hc9.web.main.redis.IndexDataCache;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.vo.PageModel;

/** 会员中心首页 */
@Service
@SuppressWarnings("rawtypes")
public class MemberCenterService {

	@Resource
	private HibernateSupport commonDao;
	
	public void useHongBao(Long id) {
		String sql = " update usermessage set isPush = 2 where id = ? ";
		commonDao.executeSql(sql, id);
	}
	
	public Object queryHongBaoExpireRemindMessage(Long userId) {
		String sql = " select * from usermessage where usermessage.user_id = ? "
				+ " and usermessage.title like '%红包到期%' "
				+ " and usermessage.isPush = 1 "
				+ " and usermessage.expireTime >= curdate() "
				+ " order by usermessage.receivetime desc, usermessage.money desc limit 1 ";
		return commonDao.findObjectBySql(sql, userId);
	}

	/**
	 * 根据id查询会员基本信息
	 * 
	 * @param id
	 *            会员编号
	 * @return 会员基本信息
	 */
	public Userbasicsinfo queryById(Long id) {
		Userbasicsinfo user = commonDao.get(Userbasicsinfo.class, id);
		return user;
	}

	/** 待收本金 */
	public Double toMoney(Long userId) {
		String sql = "SELECT sum(tenderMoney) from loansign ls LEFT JOIN loanrecord lr ON ls.id=lr.loanSign_id "
				+ "where lr.isSucceed=1 and lr.userbasicinfo_id="
				+ userId
				+ " and ls.`status` not in(0,-1,8,9)";
		Object obj = commonDao.findObjectBySql(sql);
		return obj != null ? Double.valueOf(obj.toString()) : 0.00;
	}
	
	/** 待收本金需要排除已回款的投资信息 */
	public Double backMoney(Long userId) {
		String sql = "select sum(IFNULL(money,0)) from repaymentrecordparticulars r,loanrecord lr,loansign ls where ls.id = lr.loanSign_id and lr.id=r.loanrecordId and lr.isSucceed=1 and lr.userbasicinfo_id=? and r.repState = 1 and ls.`status` in (6,7)";
		Object obj = commonDao.findObjectBySql(sql,userId);
		return obj != null ? Double.valueOf(obj.toString()) : 0.00;
	}

	/** 待收佣金 */
	public Double toBonus(Long userId) {
		String sql = "select sum(IFNULL(bonuses,0)) from generalizemoney  where pay_state=0  and refer_userid= "
				+ userId;
		Object obj = commonDao.findObjectBySql(sql);
		return obj != null ? Double.valueOf(obj.toString()) : 0.00;
	}

	/** 历史收益  */
	public Double hostIncome(Long userId) {
		String sql = "select sum((select SUM(ifnull(rmp.realMoney,0)-IFNULL(rmp.money,0))+SUM(IFNULL(rmp.middleRealMoney,0)-IFNULL(rmp.middleMoney,0))+SUM(IFNULL(rmp.afterRealMoney,0)-IFNULL(rmp.afterMoney,0))"
				+ "+(select IFNULL(sum(incomeMoney),0) from voteincome where loanRecordId=lr.id and status = 1)"
				+ " from repaymentrecord repm,repaymentrecordparticulars rmp where repm.id=rmp.repaymentrecordId and rmp.loanrecordId=lr.id and rmp.repState=1)) "
				+ "from loanrecord lr ,loansign ls where ls.id=lr.loanSign_id  and lr.userbasicinfo_id="
				+ userId + " and lr.isSucceed=1 and ls.`status`in(6,7,8)";
		Object obj = commonDao.findObjectBySql(sql);
		return obj != null ? Double.valueOf(obj.toString()) : 0.00;
	}

	/** 查询用户的累计投资金额 */
	public double queryTotalInvestMoneyOfUser(long userId) {
		double totalInvestMoney = 0;
		String sql = "SELECT sum(tenderMoney) from loanrecord where isSucceed=1 and userbasicinfo_id=?";
		Object obj = commonDao.findObjectBySql(sql, userId);
		if(obj != null) {
			totalInvestMoney = Double.valueOf(obj.toString());
		}
		return totalInvestMoney;
	}
	
	// 回款条数
	public Integer backCount(Long userId) {
		String sql = "select count(*) from loanrecord r join loansign l where r.loanSign_id =l.id and r.isSucceed=1  and l.`status`  not in(0,-1,8,9) and r.userbasicinfo_id="
				+ userId;
		Object obj = commonDao.findObjectBySql(sql);
		return obj != null ? Integer.valueOf(obj.toString()) : 0;
	}

	// 投资收益条数
	public Integer loanCount(Long userId) {
		String sql = "select count(ls.id) from loanrecord lr ,loansign ls where ls.id=lr.loanSign_id  and lr.userbasicinfo_id="
				+ userId + " and lr.isSucceed=1 and ls.`status`=8";
		Object obj = commonDao.findObjectBySql(sql);
		return obj != null ? Integer.valueOf(obj.toString()) : 0;
	}

	/** 获取用户投资概况相关投资信息 */
	public List queryInvestStatisticInfo(Long userId) {
		// 投资概况 本月(1)，下一个月(2),本年(3)
		String key = "LST:INVEST:STATISTIC:INFO:USER:" + userId;
		List arrylist = IndexDataCache.getList(key);
		if(arrylist == null || arrylist.size() < 1) {
			arrylist = updateInvestStatisticInfo(userId);
		}
		
		return arrylist;
	}
	
	/** 更新用户投资概况缓存 */
	public List updateInvestStatisticInfo(Long userId) {
		String key = "LST:INVEST:STATISTIC:INFO:USER:" + userId;
		List arrylist = new ArrayList();
		for (int i = 1; i < 5; i++) {
			Object[] obj = loanHistory(userId, i);
			arrylist.add(obj);
		}
		IndexDataCache.set(key, arrylist);
		return arrylist;
	}
	
	// 投资概况
	public Object[] loanHistory(Long userId, Integer type) {
		StringBuffer sql = new StringBuffer(
				"SELECT COUNT(lr.id),sum(lr.tenderMoney) from loanrecord lr where  lr.isSucceed=1 ");
		if (type == 1) {
			sql.append(" and DATE_FORMAT( lr.tenderTime, '%Y%m' ) = DATE_FORMAT( CURDATE( ) , '%Y%m' ) ");

		} else if (type == 2) {
			sql.append(" and PERIOD_DIFF( date_format( now( ) , '%Y%m' ) , date_format( lr.tenderTime, '%Y%m' ))=1 ");

		} else if (type == 3) {
			sql.append(" and YEAR(lr.tenderTime)=YEAR(NOW()) ");

		}
		sql.append("  and lr.userbasicinfo_id=" + userId);
		Object[] obj = (Object[]) commonDao.findObjectBySql(sql.toString());
		return obj;
	}

	public Object[] backHistory(Long userId, Integer type) {
		StringBuffer sql = new StringBuffer(
				"SELECT count(lr.id),sum(lr.tenderMoney) from loanrecord lr LEFT JOIN loansign ls ON lr.loanSign_id=ls.id "
						+ "WHERE   lr.isSucceed=1  ");
		if (type == 1) {
			sql.append(" and ls.`status`=1 ");
		} else if (type == 2) {
			sql.append(" and ls.`status`in(6,7) ");
		} else {
			sql.append(" and ls.`status`in(1,6,7) ");
		}
		sql.append("  and lr.userbasicinfo_id=" + userId);
		Object[] obj = (Object[]) commonDao.findObjectBySql(sql.toString());
		return obj;
	}
	
	
	/** 获取用户回款相关投资信息 */
	public List queryBackMoneyStatisticInfo(Long userId) {
		// 回款中和投资中
		String key = "LST:BACK:MONEY:STATISTIC:INFO:USER:" + userId;
		List arrylist = IndexDataCache.getList(key);
		if(arrylist == null || arrylist.size() < 1) {
			arrylist = updateBackMoneyStatisticInfo(userId);
		}
		
		return arrylist;
	}
	
	/** 更新用户回款相关缓存 */
	public List updateBackMoneyStatisticInfo(Long userId) {
		String key = "LST:BACK:MONEY:STATISTIC:INFO:USER:" + userId;
		List arrlist = new ArrayList();
		Double backMoney = backMoney(userId);
		for (int i = 1; i < 4; i++) {
			Object[] obj = backHistory(userId, i);
			if (i == 2 || i == 3) {
				if (obj[1] != null) {
					obj[1] = Double.valueOf(obj[1].toString()) - backMoney;
				}
			}
			arrlist.add(obj);
		}
		IndexDataCache.set(key, arrlist);
		return arrlist;
	}
	
	/** 获取用户还款和回款信息列表 */
	public List queryRepaymentBackList(Long userId) {
		List arrylist = new ArrayList();
//		String key = "LST:HC9:REPAYMENT:BACK:LIST:USER:" + userId;
//		String valueFlag = "LST:HC9:REPAYMENT:BACK:VALUE:FLAG:USER:" + userId;
//		String flag = RedisHelper.get(valueFlag);
//		if("1".equals(flag) ) {
//			arrylist = IndexDataCache.getList(key);
//		} else if(!"0".equals(flag)) {
			arrylist = repaymentBackList(userId);
//		}
		return arrylist;
	}
	
	/**
	 * 查询用户的还款和回款信息
	 * @param userId
	 * @return 还款和回款合并后的list
	 */
	public List repaymentBackList(Long userId) {
		StringBuffer sql = new StringBuffer("select ");
		sql.append("t.time,t.type,t.name,t.money from (");
		sql.append("select rr.preRepayDate as time,1 as type,ls.name as `name`,(ifnull(rr.money,0)+IFNULL(rr.preRepayMoney,0)+IFNULL(rr.middleMoney,0)+ifnull(rr.middlePreRepayMoney,0)+IFNULL(rr.afterMoney,0)+IFNULL(rr.afterPreRepayMoney,0)+IF(ls.loansignType_id=5,IFNULL(rr.companyPreFee,0),0)) as money ");
		sql.append(" from loansign ls left join repaymentrecord rr on rr.loanSign_id=ls.id");
		sql.append(" where ls.userbasicinfo_id = "+userId+" and ls.status in (6,7) and rr.repayState in (1,3)");
		sql.append(" union all ");
		sql.append("select rr.preRepayDate as time,2 as type,ls.name as `name`,(ifnull(rp.money,0)+IFNULL(rp.middleMoney,0)+IFNULL(rp.afterMoney,0)+ifnull(rp.preRepayMoney,0)+IFNULL(rp.middlePreRepayMoney,0)+IFNULL(rp.afterPreRepayMoney,0)) as money ");
		sql.append("from loanrecord lr left join repaymentrecordparticulars rp on lr.id=rp.loanrecordId ");
		sql.append("left join repaymentrecord rr on rr.id=rp.repaymentrecordId join loansign ls on ls.id=lr.loanSign_id ");
		sql.append("where isSucceed = 1 and lr.userbasicinfo_id = "+userId+" and ls.status in (6,7) and rr.repayState in (1,3) ");
		sql.append("and (ifnull(rp.money,0)+IFNULL(rp.middleMoney,0)+IFNULL(rp.afterMoney,0)+ifnull(rp.preRepayMoney,0)+IFNULL(rp.middlePreRepayMoney,0)+IFNULL(rp.afterPreRepayMoney,0)) > 0) t ");
		sql.append("order by t.time limit 0,10");
		List resultList = commonDao.findBySql(sql.toString());
//		String key = "LST:HC9:REPAYMENT:BACK:LIST:USER:" + userId;
//		String valueFlag = "LST:HC9:REPAYMENT:BACK:VALUE:FLAG:USER:" + userId;
//		if(resultList == null || resultList.size() < 1) {
//			RedisHelper.set(valueFlag, "0");
//		} else {
//			RedisHelper.set(valueFlag, "1");
//			IndexDataCache.set(key, resultList);
//		}
		return resultList;
	}
	
	public List repaymentBackListByType(Long userId, int type, int all) {
		if(type == 1){
			StringBuffer sql = new StringBuffer("select ");
			sql.append("rr.preRepayDate,2,ls.name,(ifnull(rp.money,0)+IFNULL(rp.middleMoney,0)+IFNULL(rp.afterMoney,0)+ifnull(rp.preRepayMoney,0)+IFNULL(rp.middlePreRepayMoney,0)+IFNULL(rp.afterPreRepayMoney,0)+(IFNULL((select sum(IFNULL(incomeMoney,0)) from voteincome where voterId=rp.userId and loanrecordId=lr.id and rr.periods=(select periods from repaymentrecord where loanSign_id=ls.id order by periods desc limit 0,1) and status= 0),0))) ");
			sql.append("from loanrecord lr left join repaymentrecordparticulars rp on lr.id=rp.loanrecordId ");
			sql.append("left join repaymentrecord rr on rr.id=rp.repaymentrecordId join loansign ls on ls.id=lr.loanSign_id ");
			sql.append("where isSucceed = 1 and lr.userbasicinfo_id = "+userId+" and ls.status in (6,7) and rr.repayState in (1,3) ");
			sql.append("and (ifnull(rp.money,0)+IFNULL(rp.middleMoney,0)+IFNULL(rp.afterMoney,0)+ifnull(rp.preRepayMoney,0)+IFNULL(rp.middlePreRepayMoney,0)+IFNULL(rp.afterPreRepayMoney,0)) > 0 ");
			sql.append("order by rr.preRepayDate ");
			if(all == 0){
				sql.append("limit 0,11");
			}
			return commonDao.findBySql(sql.toString());
		}else{
			StringBuffer sql = new StringBuffer("select ");
			sql.append(" rr.preRepayDate,1,ls.name,(ifnull(rr.money,0)+IFNULL(rr.preRepayMoney,0)+IFNULL(rr.middleMoney,0)+ifnull(rr.middlePreRepayMoney,0)+IFNULL(rr.afterMoney,0)+IFNULL(rr.afterPreRepayMoney,0)+IF(ls.loansignType_id=5,IFNULL(rr.companyPreFee,0),0))");
			sql.append(" from loansign ls left join repaymentrecord rr on rr.loanSign_id=ls.id");
			sql.append(" where ls.userbasicinfo_id = "+userId+" and ls.status in (6,7) and rr.repayState in (1,3)");
			sql.append(" order by rr.preRepayDate ");
			if(all == 0){
				sql.append("limit 0,11");
			}
			return commonDao.findBySql(sql.toString());
		}
	}

	public Map<String, List> queryDate(String date, Long userId) {
		String sql = "SELECT rp.preRepayDate from repaymentrecordparticulars rmp LEFT JOIN loanrecord lr ON rmp.loanrecordId=lr.id "
				+ "LEFT JOIN repaymentrecord rp ON rmp.repaymentrecordId=rp.id "
				+ "where lr.userbasicinfo_id="
				+ userId
				+ " and rmp.repState in(0,-1) and DATE_FORMAT( rp.preRepayDate, '%Y%m' ) = DATE_FORMAT( '"
				+ date + "', '%Y%m' ) and rp.preRepayDate >'" + date + "' and (ifnull(rmp.money,0)+IFNULL(rmp.middleMoney,0)+IFNULL(rmp.afterMoney,0)+ifnull(rmp.preRepayMoney,0)+IFNULL(rmp.middlePreRepayMoney,0)+IFNULL(rmp.afterPreRepayMoney,0)) > 0";

		String sqll = "SELECT rp.preRepayDate from repaymentrecord rp LEFT JOIN loansign ls ON rp.loanSign_id =ls.id "
				+ "where rp.repayState in (1,3) and ls.userbasicinfo_id="
				+ userId
				+ " and DATE_FORMAT( rp.preRepayDate, '%Y%m' ) = DATE_FORMAT( '"
				+ date + "', '%Y%m' ) and rp.preRepayDate >'" + date + "'";
		List list = commonDao.findBySql(sql.toString());
		List list1 = commonDao.findBySql(sqll.toString());
		Map<String, List> maplist = new HashMap<String, List>();
		maplist.put("loan", list1);
		maplist.put("borrow", list);

		return maplist;
	}

	public List nowDate(String date, Long userId) {
		String sql = "SELECT count(lr.id) from repaymentrecordparticulars rmp LEFT JOIN loanrecord lr ON rmp.loanrecordId=lr.id "
				+ "LEFT JOIN repaymentrecord rp ON rmp.repaymentrecordId=rp.id "
				+ "where lr.userbasicinfo_id="
				+ userId
				+ " and rmp.repState in(0,-1) and rp.preRepayDate LIKE '%"
				+ date + "%' and (ifnull(rmp.money,0)+IFNULL(rmp.middleMoney,0)+IFNULL(rmp.afterMoney,0)+ifnull(rmp.preRepayMoney,0)+IFNULL(rmp.middlePreRepayMoney,0)+IFNULL(rmp.afterPreRepayMoney,0)) > 0";
		List list = commonDao.findBySql(sql.toString());
		return list;
	}

	public List nowDateBorrow(String date, Long userId) {
		String sql = "SELECT count(rp.id) from repaymentrecord rp LEFT JOIN loansign ls ON rp.loanSign_id =ls.id "
				+ "where ls.userbasicinfo_id="
				+ userId
				+ " and rp.preRepayDate LIKE '%" + date + "%'";

		List list = commonDao.findBySql(sql.toString());
		return list;
	}

	/** 待收收益 */
	public Object dueRepay(Long id) {
		String sql = "SELECT sum(IFNULL(rpp.preRepayMoney,0))+sum(IFNULL(rpp.middlePreRepayMoney,0))+sum(IFNULL(rpp.afterPreRepayMoney,0))+IFNULL((select sum(IFNULL(incomeMoney,0)) from voteincome where voterId=rpp.userId and status= 0),0) "
				+ "from "
				+ "repaymentrecord rp JOIN repaymentrecordparticulars rpp on rp.id=rpp.repaymentrecordId "
				+ "where rp.repayState in (1,3) AND rpp.userId=?";
		Object obj = commonDao.findObjectBySql(sql, id);
		if(obj == null) {
			obj = 0.00;
		}
		return obj;
	}

	/** 邮箱加密 */
	public static String getEncryptionEmail(String email) {
		String prefix = email.substring(0, email.lastIndexOf("@"));
		if (prefix.length() > 2) {
			prefix = prefix.substring(0, 2);
		}
		prefix += "****";
		String host = email.substring(email.indexOf("@"));
		email = prefix + host;
		return email;
	}

	/** 手机加密 */
	public String getEncryptionPhone(String phone) {
		String mphone = phone.substring(3, phone.length() - 4);
		phone = phone.replace(mphone, "****");
		return phone;
	}

	/** 用户名加密  */
	public String getEncryptionName(String userName) {
		System.out.println(userName);
		if (userName.length() >= 6) {
			String name = userName.substring(2, userName.length() - 2);
			userName = userName.replace(name, "****");
			System.out.println(userName);
		}
		if (userName.length() <= 5) {
			String name = userName.substring(1, userName.length() - 1);
			userName = userName.replace(name, "***");
		}
		return userName;
	}

	/** 判断用户是否已经登录 */
	public boolean webLogin(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		if (user == null) {
			request.setAttribute("please_login", "请先登录...");
			return false;
		}
		return true;
	}

	/** 用户是否有充值记录 */
	public boolean hasRecharged(Userbasicsinfo user) {
		if(user.getUserfundinfo().getCashBalance() > 0) {
			return true;
		}
		/**  */
		String key = "STR:HC9:RECHARGE:RECORD:USER:" + user.getId();
		String rechargeFlag = RedisHelper.get(key);
		if("1".equals(rechargeFlag)) {
			return true;
		} else if("0".equals(rechargeFlag)) {
			return false;
		}
		String sql="SELECT COUNT(1) FROM recharge r WHERE r.user_id=?";
		Object obj=commonDao.findObjectBySql(sql, user.getId());
		if(0==Long.parseLong(obj.toString())){
			RedisHelper.setWithExpireTime(key, "0", 10);
			return false;
		}else{
			RedisHelper.setWithExpireTime(key, "1", 10);
			return true;
		}
	}
}
