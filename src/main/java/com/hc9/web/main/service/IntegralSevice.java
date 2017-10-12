package com.hc9.web.main.service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Autointegral;
import com.hc9.web.main.entity.ContenAndPoints;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.util.DateUtils;

/**
 * 积分操作
 * @author frank
 *
 */
@Service
public class IntegralSevice {
	@Resource
	HibernateSupport dao;
	@Resource
	GeneralizeService generalizeService;
	/**积分内容映射表*/
    public static final Map<Integer,ContenAndPoints> POINT_MAP = new HashMap<Integer, ContenAndPoints>(){
    	{
    	put(1, new ContenAndPoints("登录", 1));
    	put(2, new ContenAndPoints("评论、咨询", 2));
    	put(3, new ContenAndPoints("约谈", 5));
    	put(4, new ContenAndPoints("投资", 0));
    	put(5, new ContenAndPoints("推荐好友", 20));
    	put(6, new ContenAndPoints("实名认证", 10));
    	put(7, new ContenAndPoints("手机认证", 10));
    	put(8, new ContenAndPoints("邮箱认证", 5));
    	put(9, new ContenAndPoints("上传图像", 5));
    	put(10, new ContenAndPoints("其它认证", 2));
    	put(11, new ContenAndPoints("投资优先", 0));
    	put(12, new ContenAndPoints("投资夹层", 0));
    	put(13, new ContenAndPoints("投资劣后", 0));
    	put(14, new ContenAndPoints("修改用户头像", 5));
    	}
    };
    /**每天积分上限*/
    private static final int LIMIT=20;
	
	/**
	 * 根据传进来的参数给用户积分
	 * 登录1天1分
	 */
	public void AddIntegralForUser(Userbasicsinfo user,int type){
		switch(type){
		case 1:
			login(user,type);
			break;
		case 2:
			interViewOrComment(user, type);
			break;
		case 3:
			break;
		case 4:
			//TODO 未从宝付获取信息，查单后，成功积1分，失败不积分。
			break;
		case 5:
			break;
		case 6:
			break;
		case 7:
			break;
		case 8:
			break;
		case 9:
			break;
		case 10:
			break;
		}
	}
	
	/**  登录的积分计算 */
	public void login(Userbasicsinfo user,int type){
		String sql="from Autointegral ai where ai.userbasicsinfo.id=? and ai.type=1 ORDER BY ai.id DESC";
		List list= dao.find(sql, user.getId());
		boolean flag=false;
		if(list.size()>0){
			Autointegral autointegral=(Autointegral) list.get(0);
			try {
				//登录日期与今天比较不为0，意味今天还没登录过。
				flag=DateUtils.differenceDateSimple(autointegral.getGetintegraltime())!=0?true:false;
			}catch (ParseException e) {
				e.printStackTrace();
			}
		}else{
			flag=true;
		}
			//增加一条记录,计1分
		if(flag){
			ContenAndPoints cp=POINT_MAP.get(type);
			Autointegral integral=new Autointegral(user, cp.getPoint(), cp.getContent(), DateUtils.formatSimple(), type);
			dao.save(integral);
		}
	}
	
	/**
	 * 约谈评论加分</br>
	 * 约谈评论操作完成后，判断今天的积分累计超过20分</br>
	 * 总得分不能超过20
	 */
	public void interViewOrComment(Userbasicsinfo user,int type){
		boolean flag=false;
		//用户当日积分总和
		String sql="SELECT SUM(a.realityintegral) from autointegral a "
				+ "where "
				+ "DATE(a.getintegraltime)=DATE(NOW()) "	//当日
				+ "AND a.type in (1,2,3)"					//累计上限
				+ "AND a.user_id=?";
		
		Object todayPoints=dao.findObjectBySql(sql, user.getId());
		int points=Integer.parseInt(todayPoints.toString());
		ContenAndPoints cp=POINT_MAP.get(type);
		//
		if(points<LIMIT){
			flag=true;
			if(LIMIT-points>cp.getPoint()){
				points=cp.getPoint();
			}else{
				points=LIMIT-points;
			}
		}
		if(flag){
			Autointegral integral=new Autointegral(user, points, cp.getContent(), DateUtils.formatSimple(), type);
			dao.save(integral);
		}
	}
	
	/** 投资积分 */
	public void invest(Userbasicsinfo user,double money,int type){
		Autointegral integral=new Autointegral();
		ContenAndPoints cp=POINT_MAP.get(type);
		integral.setContent(cp.getContent());
		integral.setGetintegraltime(DateUtils.formatSimple());
		integral.setRealityintegral((int)money/100);
		integral.setType(type);
		integral.setUserbasicsinfo(user);
		dao.save(integral);
	}
	
	/** 邮箱激活认证积分 */
	public void activateEmail(Userbasicsinfo user,int identy){
		addNewRecord(user, identy);
	}
	
	/** 手机验证 */
	public void phoneAuth(Userbasicsinfo userbasics, int type){
		addNewRecord(userbasics, type);
	}
	
	/** 新增一条记录 */
	public void addNewRecord(Userbasicsinfo user, int type){
		Autointegral integral=new Autointegral();
		ContenAndPoints cp=POINT_MAP.get(type);
		integral.setContent(cp.getContent());
		integral.setGetintegraltime(DateUtils.formatSimple());
		integral.setRealityintegral(cp.getPoint());
		integral.setType(type);
		integral.setUserbasicsinfo(user);
		dao.save(integral);
	}
}
