package com.hc9.web.main.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Hccoindetail;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;

@Service
public class HccoindetailService {
	
	@Resource
	private HibernateSupport dao;
	
	/***
	 * 保存红筹币
	 * 红筹币=投资额*投资天数/360*13%
	 * @param loanrecord
	 * @param hccoinMoney
	 * @param user
	 */
	public void saveHccoindetail(Loanrecord loanrecord,Double hccoinMoney,Userbasicsinfo user){
			Integer day=30;
			 if(loanrecord.getLoansign().getType()==2){ //项目
		    	if(loanrecord.getLoansign().getRefunway()==1){
		    		day=day*loanrecord.getLoansign().getRemonth();
		    	}else if(loanrecord.getLoansign().getRefunway()==2){
		    		day=day*loanrecord.getLoansign().getRemonth()*3;
		    	}
		    }else if(loanrecord.getLoansign().getType()==3){ //天标
		    	day=loanrecord.getLoansign().getRemonth();
		    }
			Long number=Math.round(Arith.mul(Arith.div(Arith.mul(loanrecord.getTenderMoney(), day),360),hccoinMoney));
			Hccoindetail hccoindetail=new Hccoindetail();
			hccoindetail.setUserbasicsinfo(user);
			hccoindetail.setSourceType(Constant.STATUES_THERE);
			hccoindetail.setRemark("推荐投资");
			hccoindetail.setNumber(number);
			hccoindetail.setReceiveTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
			hccoindetail.setLoanrecordId(loanrecord.getId());
			dao.save(hccoindetail);
	}
	
	/***
	 * lkl-20150811
	 * 员工推荐注册所得
	 * @param user
	 * @param number
	 */
	public void saveHccoindetailNumber(Userbasicsinfo user){
		Hccoindetail hccoindetail=new Hccoindetail();
		hccoindetail.setUserbasicsinfo(user);
		hccoindetail.setSourceType(Constant.STATUES_ONE);
		hccoindetail.setRemark("推荐注册");
		hccoindetail.setNumber(Long.valueOf(5));
		hccoindetail.setReceiveTime(DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		dao.save(hccoindetail);
	}
}
