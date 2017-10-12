package com.hc9.web.main.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.InterestIncreaseCard;

@Service
public class InterestIncreaseCardService {

	@Resource
	private HibernateSupport dao;
	
	/** 判断购买时间是否符合 */
	public InterestIncreaseCard getIncreaseCard(Long id,Long userId){
		String sql="select * from interestIncreaseCard where id=? and userId=?  and  date_format(endTime,'%Y-%m-%d') >= date_format(now(),'%Y-%m-%d') and date_format(beginTime,'%Y-%m-%d')<=date_format(now(),'%Y-%m-%d') and useFlag = 0";
		List<InterestIncreaseCard> inList=dao.findBySql(sql, InterestIncreaseCard.class, id,userId);
		if(inList.size()>0){
			return inList.get(0);
		}else{
			return null;
		}
	}
	
	/** 根据loanrecord进行查询 */
	public InterestIncreaseCard getLoanRecordCard(Long loanrecordId){
		String sql="select * from interestIncreaseCard where loanrecordId=?";
		List<InterestIncreaseCard> listIncreaseCard=dao.findBySql(sql, InterestIncreaseCard.class, loanrecordId);
		if(listIncreaseCard.size()>0){
			return listIncreaseCard.get(0);
		}else{
			return null;
		}
	}
	
	/** 根据状态进行更新加息券表 */
	public  void uptIncreaseCard(InterestIncreaseCard increaseCard){
		 if(increaseCard.getUseFlag()==1){
			 String sql="update interestIncreaseCard set useFlag="+increaseCard.getUseFlag()+" ,consumeTime='"+increaseCard.getConsumeTime().trim()+"' where id="+increaseCard.getId();
			 dao.executeSql(sql);
		 }else{
			 String sql="update interestIncreaseCard set useFlag="+increaseCard.getUseFlag()+" ,loanrecordId="+increaseCard.getLoanrecordId()+" where id="+increaseCard.getId();
			 dao.executeSql(sql);
		 }
	}
}