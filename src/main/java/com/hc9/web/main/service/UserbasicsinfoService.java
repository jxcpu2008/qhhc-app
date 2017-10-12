package com.hc9.web.main.service;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.StatisticsUtil;
import com.hc9.web.main.vo.MsgVo;
import com.hc9.web.main.vo.PageModel;

/** Userbasicinfo CRUD操作 */
@Service
public class UserbasicsinfoService {
	
	@Resource
	private HibernateSupport dao;
	
	/** 查询会员个人信息 */
	public Userbasicsinfo queryUserById(Long id) {
		String sql="SELECT * FROM userbasicsinfo WHERE id=?";
		return dao.findObjectBySql(sql, Userbasicsinfo.class, id);
	}

	/** 修改会员信息 */
	public void update(Userbasicsinfo userbasicsinfo) {
		dao.update(userbasicsinfo);
	}
    
    /** 修改登录密码 */
    public void updatePwd(Userbasicsinfo user, String pwd) {
        user.setPassword(pwd);
        dao.update(user);
    }
    
    /** 修改身份证 */
    public void updateCardImg(Userbasicsinfo user, String cardImg) {
        user.getUserrelationinfo().setCardImg(cardImg);
        dao.update(user);
    }
    
    public boolean queryUserByCardId(String cardId) {
    	return Integer.valueOf(dao.findObjectBySql("select count(1) from userrelationinfo where cardId=?", cardId).toString()) > 0 ?  true :  false;
    }
    
	public Userbasicsinfo queryUserByStaffNo(String member) {
		String sql="SELECT * FROM userbasicsinfo WHERE staff_no=?";
		
		return dao.findObjectBySql(sql, Userbasicsinfo.class, member);
	}
	
	public String getcurrentRegUsers(String key) {
		String sql="SELECT count(id) FROM userbasicsinfo";
		String value=String.valueOf(dao.findObjectBySql(sql,null));
		RedisHelper.set(key, value);
		return value;
	}
	
	/** 查询消息列表 */
	public PageModel queryMessageList(String userId, PageModel page) {
		String sql = "select context,receivetime,id,isread,title from usermessage where user_id=? "
				+ "order by isread asc,receivetime desc ";
		sql += " limit " + (page.getPageNum() - Constant.STATUES_ONE) * page.getNumPerPage() + "," + page.getNumPerPage();
		List list = dao.findBySql(sql, userId);
		
		String sqlCount = "select count(id) from usermessage where user_id=?";
		int totalCount = dao.queryNumberSql(sqlCount, userId).intValue();
		
		List<MsgVo> articleList = new ArrayList<MsgVo>();
		if(list != null && list.size() > 0) {
			for(Object obj : list) {
				Object[] arr = (Object[])obj;
				MsgVo vo = new MsgVo();
				vo.setContent(StatisticsUtil.getStringFromObject(arr[0]));
				vo.setCreateTime(StatisticsUtil.getStringFromObject(arr[1]));
				vo.setId(StatisticsUtil.getLongFromBigInteger(arr[2]));
				vo.setIsread(StatisticsUtil.getIntegerFromObject(arr[3]));
				vo.setTitle(StatisticsUtil.getStringFromObject(arr[4]));
				articleList.add(vo);
			}
		}
		page.setTotalCount(totalCount);
		page.setList(articleList);
		return page;
	}
	
	/** 根据消息id修改为已读状态 */
	public int updateMsgReadStatusById(String userId, String msgId) {
		String sql = "update usermessage set isread=1 where user_id=? and id=?";
		return dao.executeSql(sql, userId, msgId);
	}
	
	/** 查询未读状态的消息提示总数 */
	public int queryUnReadMsgNum(String userId) {
		String sql = "select count(*) from usermessage where isread=0 and user_id=?";
		Object obj = dao.findObjectBySql(sql, userId);
		BigInteger sumNum = (BigInteger)obj;
		return sumNum.intValue();
	}
	
	/** 全部消息修改为已读状态 */
	public int updateAllMsgReadStatus(String userId) {
		String sql = "update usermessage set isread=1 where user_id=? and isread=0";
		return dao.executeSql(sql, userId);
	}
	
	/** 判断传入的用户目前是否是特权会员 */
    public boolean isPrivilege(Userbasicsinfo userbasicsinfo) {
        try {
            String sb = "SELECT MAX(endtime) from vipinfo where user_id=?";
            Object obj = dao.findObjectBySql(sb.toString(), userbasicsinfo.getId());
            if (obj != null && DateUtils.isBefore(Constant.DEFAULT_TIME_FORMAT, obj.toString())) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
        	LOG.error("判断特权用户的过程中出错！", e);
            return false;
        }
    }

	/** 判断是否是指定期间内注册的用户 */
	public boolean isUserRegistedInTimeArea(Long userId, String beginDate, String endDate){
		  String sql="select * from userbasicsinfo where  id=? " 
				  + "and DATE_FORMAT(createTime, '%Y-%m-%d')>=DATE_FORMAT('" + beginDate + "', '%Y-%m-%d')  " 
				  + "AND DATE_FORMAT(createTime, '%Y-%m-%d')<=DATE_FORMAT('" + endDate + "', '%Y-%m-%d') ";
		  List<Userbasicsinfo> list = dao.findBySql(sql, Userbasicsinfo.class, userId);
		  if(list.size() > 0) {
			  return true;
		  } else {
			  return false;
		  }
	}
	
	/** 根据真实姓名和电话以及身份证号查询被推广人是否存在 */
	public Userbasicsinfo checkUserIsExist(String realName, String phone,
			String identity) {
		String sql = "select u.* from userbasicsinfo u join userrelationinfo i "
				+ "on i.user_id=u.id where u.name=? and i.phone=? and i.cardId=? ";
		Userbasicsinfo u = dao.findObjectBySql(sql, Userbasicsinfo.class,
				realName, phone, identity);
		return u;
	}
	
}
