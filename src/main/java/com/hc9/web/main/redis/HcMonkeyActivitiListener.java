package com.hc9.web.main.redis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import com.hc9.web.main.entity.ActivityMonkey;
import com.hc9.web.main.redis.activity.year2016.month01.HcMonkeyActivitiCache;

// 废弃
public class HcMonkeyActivitiListener extends HttpServlet implements ServletContextListener {

	private static final long serialVersionUID = 1L;

	public HcMonkeyActivitiListener() {
	}

	private java.util.Timer timer = null;

	public void contextInitialized(ServletContextEvent event) {
		Calendar date = Calendar.getInstance();
		 date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), 26, 0, 0, 0);
//		date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), 15, 15, 14, 0);
		// 一周的毫秒数
		 long daySpan = 7 * 24 * 60 * 60 * 1000;
//		long daySpan = 2 * 1000;
		timer = new java.util.Timer(true);
		event.getServletContext().log("定时器已启动");
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				int week = HcMonkeyActivitiCache.week();
				if(week > 0){
					String key = "NEWYEAR:INVEST:MONKEY:WEEK:" + week;
					List<Map> list = IndexDataCache.getList(key);
					if (list != null) {
						// 插入库
						List<ActivityMonkey> activityMonkeyList = new ArrayList<ActivityMonkey>();
						for (int i = 0; i < list.size() && i < 3; i++) {
							Map<String, String> map = list.get(i);
							String rewardMoney = "0";
							if(i==0){
								rewardMoney = (Double.parseDouble(map.get("money"))*0.8/100)+"";
							}else if(i==1){
								rewardMoney = (Double.parseDouble(map.get("money"))*0.6/100)+"";
							}else if(i==3){
								rewardMoney = (Double.parseDouble(map.get("money"))*0.3/100)+"";
							}
							activityMonkeyList.add(HcMonkeyActivitiCache.generateActivityMonkey(map.get("userId"), map.get("phone"), map.get("money"), (3+i), map.get("loanId"), map.get("loanName"), map.get("loanRecordId")+"", rewardMoney, week));
						}
						if(activityMonkeyList.size() > 0){
							//HcMonkeyActivitiCache.saveOrUpdateAll(activityMonkeyList);
						}
					}
					if(week == 6){
						key = "NEWYEAR:INVEST:MONKEY:TOTAL";
						list = IndexDataCache.getList(key);
						if (list != null) {
							// 插入库
							List<ActivityMonkey> activityMonkeyList = new ArrayList<ActivityMonkey>();
							for (int i = 0; i < list.size() && i < 3; i++) {
								Map<String, String> map = list.get(i);
								String rewardMoney = "0";
								activityMonkeyList.add(HcMonkeyActivitiCache.generateActivityMonkey(map.get("userId"), map.get("phone"), map.get("money"), (6+i), map.get("loanId"), map.get("loanName"), map.get("loanRecordId")+"", rewardMoney, 0));
							}
							//HcMonkeyActivitiCache.saveOrUpdateAll(activityMonkeyList);
						}
						//定时器销毁
						this.cancel();
					}
				}
			}
		}, date.getTime(), daySpan);
		event.getServletContext().log("已经添加任务调度表");
	}

	public void contextDestroyed(ServletContextEvent event) {
		timer.cancel();
		System.out.println("定时器销毁");
		event.getServletContext().log("定时器销毁");
	}
}
