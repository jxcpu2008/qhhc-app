package com.hc9.web.main.service.activity.year2015;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.ActivityMonkey;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.IndexDataCache;
import com.hc9.web.main.redis.activity.year2016.month03.HcPeachActivitiCache;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.vo.PageModel;

/** 活动相关服务类 */
@Service
public class ActivityService {
	
	@Resource
	private HibernateSupport dao;
	
	public PageModel getPlusInterestList(PageModel page, Long id) {
		StringBuffer sql = new StringBuffer(
				"select id,money,beginTime,endTime,lowestUseMoney,"
				+ "case when (date_format(now(),'%Y-%m-%d') > date_format(endTime,'%Y-%m-%d') and useFlag = 0) then '3' when useFlag = 0 then '0' when useFlag = 1 then '1' when useFlag = 2 then '2' end as i"
				+ ",consumeTime from redenvelopedetail where userId=? " 
						+ " order by i,endTime,money desc");
		String sqlCount = "select count(1) from redenvelopedetail where userId=? ";
		page.setTotalCount(dao.queryNumberSql(sqlCount, id).intValue()); // 设置总记录数
		sql.append(" limit " + page.getNumPerPage() * (page.getPageNum() - 1))
				.append(",").append(page.getNumPerPage());
		List list = dao.findBySql(sql.toString(), id);
		page.setList(list);
		return null;
	}
	
	public PageModel queryNewYearMonkeyRecord(PageModel page, Long userId) {
		StringBuffer sql = new StringBuffer("select type,rewardMoney,createTime,status,userId,byUserId");
		sql.append(" from activity_monkey a where a.userId=? and examineStatus!=9");
		String sqlCount = "select count(1) from activity_monkey where userId=? and examineStatus!=9";
		page.setTotalCount(dao.queryNumberSql(sqlCount,userId).intValue()); // 设置总记录数
		sql.append(" order by createTime desc limit " + page.getNumPerPage() * (page.getPageNum() - 1))
				.append(",").append(page.getNumPerPage());
		List list = dao.findBySql(sql.toString(),userId);
		page.setList(list);
		return page;
	}

	public List queryH5NewYearMonkeyRecord(Long userId) {
		StringBuffer sql = new StringBuffer("select type,rewardMoney,createTime,status,loanName from activity_monkey a where a.type != 9 and a.userId=? order by createTime desc");
		return dao.findBySql(sql.toString(),userId);
	}
	
	/**
	 * 获取红筹达人1-3名的手机号
	 * @return obj
	 */
	public Object queryHongChouEredarTopThree() {
		StringBuffer sql = new StringBuffer("select ");
		sql.append("(select mobilePhone from activity_monkey where type =6),");
		sql.append("(select mobilePhone from activity_monkey where type =7),");
		sql.append("(select mobilePhone from activity_monkey where type =8)");
		sql.append(" from dual");
		Object obj = null;
		try {
			obj = dao.findObjectBySql(sql.toString());
		} catch (Exception e) {
			LOG.error("获取红筹达人信息时出现异常");
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * 查询金桃朵朵开推荐排行信息
	 * @return list
	 */
	public List<Object[]> queryGoldPeachBillBoard(Long userId) {
		StringBuffer sql = new StringBuffer("SELECT (SELECT phone FROM userrelationinfo WHERE user_id=g.genuid) as phone,COUNT(1) as num,max(u2.createTime) as time");
		sql.append(" FROM generalize g,userbasicsinfo u2,cardimgaudit ca WHERE g.uid=u2.id AND u2.id=ca.userId AND g.genuid in");
		sql.append(" (SELECT id FROM userbasicsinfo) AND ca.cardImgState=1 AND date_format(u2.createTime,'%Y-%m-%d') >='"+HcPeachActivitiCache.getActiveBeginDate()+"'");
		sql.append(" AND date_format(u2.createTime,'%Y-%m-%d') <='"+HcPeachActivitiCache.getActiveEndDate()+"' AND g.state=2 ");
		if (userId != null) {
			sql.append(" AND g.genuid="+userId);
		} else {
			sql.append(" GROUP BY g.genuid ORDER BY num desc,time limit 0,10");
		}
		List<Object[]> billboards = null;
		try {
			billboards = dao.findBySql(sql.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return billboards;
	}
	
	/**
	 * 查询当前用户是否满足现金奖励活动，满足返回相应的活动规则提示结果
	 * @param userId
	 * @return message
	 */
	public String cashRewardStandard(Long userId) {
		String message = "";
		String keyUser = "NEWYEAR:INVEST:FINANCIAL:USER" + userId;
		Map<String,String> recommend = IndexDataCache.getObject(keyUser);
		if (recommend != null) {
			Double tenderMoney = 0D;
			Integer state = 0; 
			for (int i = 0; i < recommend.size(); i++) {
				tenderMoney = Double.valueOf(recommend.get("money"));
				state = Integer.valueOf(recommend.get("static"));
			}
			if (tenderMoney != 0 && tenderMoney != null && state != 0) {
				if (tenderMoney == 188) { // 累计推荐好友投资10万及以上可得188
					message = "10万送188元现金奖励的标准";
				} else if (tenderMoney == 388) { // 累计推荐好友投资20万及以上可得388
					message = "20万送388元现金奖励的标准";
				} else if (tenderMoney == 888) { // 累计推荐好友投资50万及以上可得888
					message = "50万送888元现金奖励的标准";
				} else if (tenderMoney == 1888) { // 累计推荐好友投资100万及以上可得1888
					message = "100万送1888元现金奖励的标准";
				}
				Map<String,String> map = new HashMap<String, String>();
				map.put("money", tenderMoney.toString());
				map.put("static", "0");  // 将map中的static设置为0表示已提醒过用户
				IndexDataCache.set(keyUser, map);
			}
		}
		return message;
	}

	/**
	 * 插入抽奖记录
	 * @param user 推荐人
	 * @param byUser 被推荐人(可以是自己)
	 * @return
	 */
	public void lotteryPeach(Userbasicsinfo user,Userbasicsinfo byUser) {
		Long userId = user.getId();
		String phone = user.getUserrelationinfo().getPhone();
		String createTime=DateUtils.format("yyyy-MM-dd HH:mm:ss");
		ActivityMonkey activityMonkey = new ActivityMonkey();
		try {
			activityMonkey.setUserId(userId);
			activityMonkey.setMobilePhone(phone);
			activityMonkey.setType(10);
			activityMonkey.setMoney(0D);
			activityMonkey.setRewardMoney(2D);
			activityMonkey.setCreateTime(createTime);
			activityMonkey.setStatus(0); // 待发放
			activityMonkey.setExamineStatus(9); // 未抽奖
			activityMonkey.setByUser(byUser);  // 被推荐人
			dao.save(activityMonkey);
		} catch (Exception e) {
			LOG.error("插入抽奖记录时出现异常-->"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询金桃抽奖记录
	 * @param user 推荐人
	 */
	public ActivityMonkey getLotteryPech(Long userId) {
		String sql = "select * from activity_monkey where userId=? and type=10 and examineStatus=9 order by id limit 0,1";
		return dao.findObjectBySql(sql,ActivityMonkey.class, userId);
	}
	
	/**
	 * 更新金桃抽奖记录
	 */
	public void updateLotteryPech(Long id,String createTime) {
		String sql = "update activity_monkey set examineStatus=0,createTime='"+createTime+"' where id="+id;
		dao.executeSql(sql);
	}
	
	public boolean remindGoldPech(Long userId) {
		String sql = "select count(1) from activity_monkey where type=10 and userId=byUserId and userId=? and examineStatus!=9";
		Object obj = dao.findObjectBySql(sql, userId);
		if (obj != null) {
			if (Integer.valueOf(obj.toString()) > 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 通过“2元中奖记录以及推荐人数”判断是否存在自己注册中的奖（金桃朵朵开）
	 * @param userId
	 * @return 0：表示没有、非0：表示自己中奖记录的id
	 */
	/*public Long queryIsSelfOrRecomendData(Long userId) {
		StringBuffer buf = new StringBuffer("select ");
		buf.append("case when (select count(1) from userbasicsinfo u,userrelationinfo i where u.id=i.user_id and date_format(createTime,'%Y-%m-%d') >='"+HcPeachActivitiCache.getActiveBeginDate()+"'");
		buf.append(" and date_format(createTime,'%Y-%m-%d') <='"+HcPeachActivitiCache.getActiveEndDate()+"' and (i.cardImg is null or i.cardImg = '') and u.id="+userId+") > 0 then ");
		buf.append("(select id from activity_monkey where userId="+userId+" and type = 10 order by createTime limit 0,1) else '0' end from dual");
		Object obj = dao.findObjectBySql(buf.toString());
		Long lg = 0L;
		if (obj != null) {
			lg = Long.valueOf(obj.toString());
		}
		return lg;
	}*/
	
}
