package com.hc9.web.main.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.util.StatisticsUtil;
import com.hc9.web.main.vo.LoginRelVo;

/** 系统缓存管理相关dao */
@Service
public class CacheManagerDao {

	/** 注入数据库操作层 */
	@Resource
	private HibernateSupport dao;
	
	/** 查询用户缓存相关信息:用户名，手机，密码、用户显示名称，email状态，实名状态，宝付状态，锁定状态 */
	@SuppressWarnings("rawtypes")
	public List<LoginRelVo> queryUserLoginRelCache() {
		List<LoginRelVo> resultList = new ArrayList<LoginRelVo>();
		String sql = "select u.id,u.userName,r.phone,u.name,u.password,u.isLock,u.cardStatus,r.emailisPass,u.isAuthIps,u.pMerBillNo,u.staff_no " + 
				"from userbasicsinfo u , userrelationinfo r where u.id=r.id";
		List list = dao.findBySql(sql);
		if (list != null && list.size() > 0) {
			for (Object obj : list) {
				Object[] arr = (Object[]) obj;
				LoginRelVo vo = new LoginRelVo();
				vo.setId(StatisticsUtil.getLongFromBigInteger(arr[0]));
				vo.setUserName(StatisticsUtil.getStringFromObject(arr[1]));
				vo.setPhone(StatisticsUtil.getStringFromObject(arr[2]));
				vo.setName(StatisticsUtil.getStringFromObject(arr[3]));
				vo.setPassword(StatisticsUtil.getStringFromObject(arr[4]));
				vo.setIsLock(StatisticsUtil.getIntegerFromObject(arr[5]));
				vo.setCardStatus(StatisticsUtil.getIntegerFromObject(arr[6]));
				vo.setEmailisPass(StatisticsUtil.getIntegerFromObject(arr[7]));
				vo.setIsAuthIps(StatisticsUtil.getIntegerFromObject(arr[8]));
				vo.setpMerBillNo(StatisticsUtil.getStringFromObject(arr[9]));
				vo.setStaffNo(StatisticsUtil.getStringFromObject(arr[10]));
				resultList.add(vo);
			}
		}
		return resultList;
	}
	
	/** 根据登录账号和手机号查询当前注册用户的信息 */
	public LoginRelVo queryLoginRelVoBy(String userName, String phone) {
		String sql = "select u.id,u.userName,r.phone,u.name,u.password,u.isLock,u.cardStatus,r.emailisPass,u.isAuthIps,u.pMerBillNo,u.staff_no " + 
				"from userbasicsinfo u , userrelationinfo r where u.id=r.id and (u.userName=? or r.phone=?)";
		List list = dao.findBySql(sql, userName, phone);
		if (list != null && list.size() > 0) {
			Object obj = list.get(0);
			Object[] arr = (Object[]) obj;
			LoginRelVo vo = new LoginRelVo();
			vo.setId(StatisticsUtil.getLongFromBigInteger(arr[0]));
			vo.setUserName(StatisticsUtil.getStringFromObject(arr[1]));
			vo.setPhone(StatisticsUtil.getStringFromObject(arr[2]));
			vo.setName(StatisticsUtil.getStringFromObject(arr[3]));
			vo.setPassword(StatisticsUtil.getStringFromObject(arr[4]));
			vo.setIsLock(StatisticsUtil.getIntegerFromObject(arr[5]));
			vo.setCardStatus(StatisticsUtil.getIntegerFromObject(arr[6]));
			vo.setEmailisPass(StatisticsUtil.getIntegerFromObject(arr[7]));
			vo.setIsAuthIps(StatisticsUtil.getIntegerFromObject(arr[8]));
			vo.setpMerBillNo(StatisticsUtil.getStringFromObject(arr[9]));
			vo.setStaffNo(StatisticsUtil.getStringFromObject(arr[10]));
			return vo;
		}
		return null;
	}
	
	/** 满标放款根据项目id更新用户的回款记录 */
	public List<Long> queryUserIdListByLoanSignId(Long loanSignId) {
		List<Long> userIdList = new ArrayList<Long>();
		String sql = "select distinct r.userbasicinfo_id from loansign l , loanrecord r where l.id=r.loanSign_id and l.id=?";
		List list = dao.findBySql(sql, loanSignId);
		if(list != null && list.size() > 0) {
			for(Object obj : list) {
				BigInteger bigObj = (BigInteger)obj;
				long userId = bigObj.longValue();
				userIdList.add(userId);
			}
		}
		return userIdList;
	}
	
	/** 查询所有已经发布的标id */
	public List<Long> queryAllLoanIdList() {
		List<Long> loanIdList = new ArrayList<Long>();
		String sql = "select id from loansign where status !=0 order by id desc";

		List list = dao.findBySql(sql);
		if(list != null && list.size() > 0) {
			for(Object obj : list) {
				BigInteger bigObj = (BigInteger)obj;
				long loanId = bigObj.longValue();
				loanIdList.add(loanId);
			}
		}
		return loanIdList;
	}
}
