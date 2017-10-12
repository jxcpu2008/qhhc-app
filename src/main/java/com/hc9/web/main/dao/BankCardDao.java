package com.hc9.web.main.dao;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.UserBank;

@Service
public class BankCardDao {
	@Resource
	private HibernateSupport dao;
	
	/** 查询是否存在银行卡 */
	public boolean getUserBank(String bankNo) {
		String sql="select * from userbank where state=1 and bank_no like '"+bankNo.trim()+"%'";
		List<UserBank> userBankList = dao.findBySql(sql, UserBank.class);
		return userBankList.size()>0?false:true;
	}
	
	/** 保存银行卡 */
	public String saveUserBankSeria(UserBank userbank) {
		Serializable seria = dao.save(userbank);
		return seria.toString();
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
	
	/** 查询费用比例设置 */
    public Costratio findCostratio() {
        List<Costratio> list = dao.find("from Costratio");
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }
}
