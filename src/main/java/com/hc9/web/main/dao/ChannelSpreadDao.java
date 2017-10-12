package com.hc9.web.main.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.ChannelSpreadDetail;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;

/**
 * 渠道推广：
 * <ul><li>ChannelSpread,</li><li>ChannelSpreadDetail</li></ul>
 * 两个表的相关数据操作
 */
@Service
public class ChannelSpreadDao {
	@Resource
	private HibernateSupport dao;
	
	/**保存ChannelSpreadDetail*/
	public void saveChannelSpreadDetail(ChannelSpreadDetail entity){
		dao.save(entity);
	}
	
	/**
	 * 通过渠道id反查渠道名称
	 * @param spreadId
	 * @return
	 */
	public String getChannelName(String spreadId){
		String sql="SELECT name FROM channelspread WHERE spreadId=?";
		List results=dao.findBySql(sql, spreadId);
		if (results.size()>0) {
			return (String) results.get(0);
		}else {
			return null;
		}
	}
	/**
	 * 通过spreadId查寻投资记录
	 * @param spreadId
	 * @param endTime 起始时间
	 * @param startTime 结束时间
	 * @return
	 */
	public List<Object[]> getLoanrecordsBySpreadId(String spreadId, String startTime, String endTime) {
		String sql="SELECT loanRecordId,cookieValue FROM `channelspreaddetail` WHERE spreadId=? AND loanRecordId is NOT NULL";
		if(startTime!=null && !"".equals(startTime)){
			sql+=" AND DATE(createTime)>=DATE(?) ";
		}
		if(startTime!=null && !"".equals(endTime)){
			sql+=" AND DATE(createTime)<=DATE(?)  ";
		}
		List results=dao.findBySql(sql, spreadId,startTime,endTime);
		if (results.size()>0) {
			return results;
		}else {
			return null;
		}
	}
	public Object[] getUserInfoByRecordId(Object objs) {
		String sql="SELECT lr.tenderTime,lr.tenderMoney FROM loanrecord lr ,loansign l  WHERE lr.userbasicinfo_id=u.id AND lr.userbasicinfo_id=u2.id AND lr.id=?";
		List results=dao.findBySql(sql, objs);
		if (results.size()>0) {
			return (Object[]) results.get(0);
		}else {
			return null;
		}
	}

	public Object[] getLoanInfoByRecordId(Object objs) {
		long id=Long.parseLong(objs.toString()); 
		String sql="SELECT lr.tenderTime,lr.tenderMoney,lr.loanSign_id,lr.order_id,l.`name`,l.loanUnit FROM loanrecord lr ,loansign l  WHERE lr.loanSign_id=l.id AND  lr.id=?";
		List<Object[]> results=dao.findBySql(sql, objs);
		if (results.size()>0) {
			return (Object[]) results.get(0);
		}else {
			return null;
		}
	}

	
	/**
	 * add by xuyh 
	 * at 2015/08/31
	 * 验证渠道 是否存在
	 * @param spreadId
	 * @param password
	 * @return
	 */
	public String findChannelUserByLoginName(String loginName,String password){
		String sql="select spreadId from channeluser where userName=? and password=?";
		if(StringUtil.isNotBlank(loginName)&&StringUtil.isNotBlank(password)){
			List results=dao.findBySql(sql,loginName,password);
			if(results!=null&&!results.isEmpty()){
				return results.get(0).toString();
			}
			return "";
		}
		return "";
	}
	
	/**
	 * 返回标的类型：3月一下0.5%....
	 * @param lid
	 * @return
	 */
	public String getLoanRemonthType(String lid){
		String sql="SELECT type,remonth FROM loansign WHERE id=?";
		Object[] result=(Object[]) dao.findObjectBySql(sql, lid);
		String str="";
		if(null==result){
			return str;
		}
		int type=(int) result[0];
		int month=(int) result[1];
		
		if(type==2){//2-项目 
			if(month<=3){
				str= "1";
			}
			if(month>3 && month<=6){
				str= "2";
			}
			if(month>6 && month<=12){
				str= "3";
			}
			if(month>12){
				str= "4";
			}
		}else{//3-天标
			str= "1";
		}
		return str;
	}
	
	public List<Object[]> getYrtRegData(String spreadId, String startTime,
			String endTime) {
		String sql="SELECT regUserName,regStatus,cookieValue FROM `channelspreaddetail` WHERE spreadId=? AND regUserName IS NOT NULL ";
		if(startTime!=null && !"".equals(startTime)){
			sql+=" AND DATE(createTime)>=DATE(?) ";
		}
		if(startTime!=null && !"".equals(endTime)){
			sql+=" AND DATE(createTime)<=DATE(?)  ";
		}
		List results=dao.findBySql(sql, spreadId,startTime,endTime);
		if (results.size()>0) {
			return results;
		}else {
			return null;
		}
	}
	public List<Object[]> getUserInfoByUserName(Object objs) throws Exception {
		String sql="SELECT u.createTime,r.emailisPass,r.phonepass,u.isAuthIps FROM userbasicsinfo u JOIN userrelationinfo r ON u.id=r.id  WHERE u.userName=?";
		List<Object[]> info=dao.findBySql(sql, objs);
		return info;
	}
	/**
	 * 用户首投金额  ，11-24补充标的期限
	 * @param object
	 * @return
	 */
	public Object[] getUserFirstInvestByUserName(Object object) throws Exception {
		LOG.info("----->"+object);
		String sql="select id from userbasicsinfo where userName=? LIMIT 1";
		Object oUid=dao.findObjectBySql(sql, object.toString());
		sql="SELECT lr.tenderMoney,l.remonth FROM loanrecord lr JOIN loansign l ON lr.loanSign_id=l.id WHERE lr.userbasicinfo_id=? ORDER BY lr.tenderTime LIMIT 1 ";
		List<Object[]> objs=dao.findBySql(sql, oUid) ;
		if(null!=objs && objs.size()>0){
			return objs.get(0);
		}
		return null;
	}

}
