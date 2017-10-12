package com.hc9.web.main.service;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Banktype;
import com.hc9.web.main.entity.UserBank;

/** 用户银行卡账号业务处理 */
@Service
public class UserBankService {

	@Resource
	private HibernateSupport dao;

	/** 根据银行类型 */
	public List<Banktype> getBankType() {
		String hql = "from Banktype";
		List<Banktype> list = dao.find(hql.toString());
		return list;
	}

	/** 根据id查询 */
	public UserBank getUserBankById(Long id) {
		String hql = "from UserBank b where b.id=?";
		List<UserBank> list = dao.find(hql, id);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	/** 修改银行卡 */
	public void update(UserBank userbank) {
		dao.update(userbank);
	}

	/** 保存银行卡 */
	public String saveUserBankSeria(UserBank userbank) {
		Serializable seria = dao.save(userbank);
		return seria.toString();
	}

	/** 查询是否存在银行卡 */
	public boolean getUserBank(String bankNo){
		String sql="select * from userbank where state=1 and bank_no like '"+bankNo.trim()+"%'";
		List<UserBank> userBankList = dao.findBySql(sql, UserBank.class);
		return userBankList.size()>0?false:true;
	}
	
	/** 根据用户Id查询 */
	public boolean getUserBankId(Long userId){
		String sql="select * from userbank where user_id=?";
		List<UserBank> userBankList = dao.findBySql(sql, UserBank.class,userId);
		return userBankList.size()>0?true:false;
	}

	/** 删除 */
	public void delete(UserBank userbank) {
		dao.delete(userbank);
	}

	public List<UserBank> getUserBankList(String userId) {
		String sql = "SELECT * from userbank where user_id=? and state=1";
		List<UserBank> userbanks = dao.findBySql(sql.toString(),UserBank.class,userId);
		return userbanks;
	}
	
	/** 保存用户银行卡信息 */
	public void saveOrUpdateUserBank(UserBank userBank) {
		dao.saveOrUpdate(userBank);
	}
	
	/** 删除不存在绑定宝付的银行卡账户 */
	public void deleteUserBank(Long userId){
		String sql="delete from userbank where user_id="+userId;
		dao.executeSql(sql);
	}
}
