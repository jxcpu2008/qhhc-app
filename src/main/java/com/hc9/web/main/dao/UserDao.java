package com.hc9.web.main.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.sys.UserInfoCache;
import com.hc9.web.main.util.DateUtil;

/** 用户相关接口 */
@Service
public class UserDao {
	@Resource
	private HibernateSupport commonDao;

	/** 根据用户id查询用户信息 */
	public Userbasicsinfo getUserById(Long userId) {
		String sql="select * from userbasicsinfo where id="+userId;
		return commonDao.findObjectBySql(sql, Userbasicsinfo.class);
	}
	
	/** 开通宝付修改用户基本信息姓名、宝付账号、用户相关信息身份证号 */
	public void updateUserAccountInfo(Long userId, String pMerBillNo) {
		String sql = "update userbasicsinfo set pMerBillNo=? where id=?";
		commonDao.executeSql(sql, pMerBillNo, userId);
	}
	
	/** 根据宝付账户查询用户信息 */
	public Userbasicsinfo getUserByMerBillNo(String pMerBillNo) {
		String sql="select * from userbasicsinfo where pMerBillNo=?";
		return commonDao.findObjectBySql(sql, Userbasicsinfo.class, pMerBillNo);
	}
	
	/** 开通宝付成功修改相关信息 */
	public void updateUserInfoAfterOpenAccount(Long userId) {
		String name = UserInfoCache.getNameFromRedis(userId);
		String cardId = UserInfoCache.getCardIdFromRedis(userId);
		String sql = "update userrelationinfo set cardId=? where user_id=?";
		commonDao.executeSql(sql, cardId, userId);
		
		String currentTime = DateUtil.format("yyyy-MM-dd HH:mm:ss");
		sql = "update userbasicsinfo set name=?,pIpsAcctDate=?,hasIpsAccount=?,isAuthIps=?,authIpsTime=?,cardStatus=? where id=?";
		commonDao.executeSql(sql, name, currentTime, 1, 1, currentTime, 2, userId);
	}
}
