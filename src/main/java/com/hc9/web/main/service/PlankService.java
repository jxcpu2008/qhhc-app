package com.hc9.web.main.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.constant.IntegralType;
import com.hc9.web.main.entity.Accountinfo;
import com.hc9.web.main.entity.Accounttype;
import com.hc9.web.main.entity.Loandynamic;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.LOG;

/** 标的购买服务层 */
@Service
public class PlankService {
	
	@Resource
	private HibernateSupport dao;
	
	@Resource
	private IntegralSevice integralSevice;

	public void update(Loanrecord loanrecord,Accountinfo accountinfo,Userbasicsinfo userbasicsinfo,Loansign loan){
		dao.saveOrUpdate(loanrecord);
		dao.save(accountinfo);
		dao.save(loan);
	    dao.saveOrUpdate(userbasicsinfo);
	}
	
	public void saveAccount(Accountinfo ac){
		dao.save(ac);
	}
	
	/** 保存放款时新增一条项目动态 */
	public void saveLoandynamic(Loandynamic loandynamic){
		LOG.error("支付记录日志：" + JsonUtil.toJsonStr(loandynamic));
		dao.save(loandynamic);
	}
	
	/**  获取类型 */
	public Accounttype accounttype(Long id){
		return dao.get(Accounttype.class, id);
	}
	
    /** 保存自动投标规则数据 */
    public void saveLoanrecord(Loanrecord lo){
    	dao.save(lo);
    }
    
    
    /** 根据pP2PBillNo查询automatic是否存在 */
    public String getAutomaticId(String pP2PBillNo){
    	if(!pP2PBillNo.equals("")&&pP2PBillNo!=null){
    		String sql="select id from automatic where pP2PBillNo='"+pP2PBillNo+"'";
        	Object id=this.dao.findBySql(sql);
        	return id.toString().substring(1, id.toString().length()-1);
    	}else{
    		return null;
    	}
    }
    
    /** 投资项目所得积分
     * @param user  用户
     * @param product 投资项目所得积分
     * @param type  购买类型
     */
    public void saveAutointegralBuyProject(Userbasicsinfo user,double money,Integer type) {

    	switch(type){
    	case 1:
    		integralSevice.invest(user, money, IntegralType.PRIO);
    		break;
    	case 2:
    		integralSevice.invest(user, money, IntegralType.MIDDLE);
    		break;
    	case 3:
    		integralSevice.invest(user, money, IntegralType.AFTER);
    		break;
    	}
    }	
}
