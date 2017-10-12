package com.hc9.web.main.service.activity.year2015;

import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.DadaBusCashCertificate;
import com.hc9.web.main.entity.PrizeDetail;
import com.hc9.web.main.entity.RedEnvelopeDetail;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2015.HcNewyearActivitiCache;
import com.hc9.web.main.service.dadabus.DadaBusService;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.vo.LotteryRank;
import com.hc9.web.main.vo.PrizeInfo;

/** 活动相关服务类 */
@Service
public class NewyearActivityService {

	@Resource
	private HibernateSupport dao;
	
	@Resource
	private DadaBusService dadaBusService;
	
	/** 中奖列表相关数据 */
	public List<LotteryRank> queryLotteryRankList() {
		String beginDate = HcNewyearActivitiCache.getActiveBeginDate() + " 00:00:00";
		String endDate = HcNewyearActivitiCache.getActiveEndDate() + " 23:59:59";
		StringBuffer sql = new StringBuffer("select l.userid,l.prizetype,l.receivetime,u.phone,l.money from ");
		sql.append("(select userid,prizetype,receivetime,0 as money from prizedetail union ");
		sql.append("select userid,100 as prizetype,receivetime,money from redenvelopedetail where sourceType=4 union ");
		sql.append("select userid,80 as prizetype,createTime as receivetime,money from dadabuscashcertificate where loanRecordId = 0) as l,");
		sql.append(" userrelationinfo u where l.userid=u.id ");
		sql.append("and l.receivetime>='" + beginDate + "' and l.receivetime<='" + endDate + "' ");
		sql.append("order by receivetime desc limit 0,100");
		List<LotteryRank> resultList = HcNewyearActivitiCache.getLotteryRankList();
		if(resultList != null && resultList.size() > 20) {
			String key = "QUERY:LOTTERY:NEWYEAR:RANK:FLAG";
			if(!RedisHelper.isKeyExistSetWithExpire(key, 5 * 60)) {
				List list = dao.findBySql(sql.toString());
				HcNewyearActivitiCache.saveLotteryRankList(list);
				resultList = HcNewyearActivitiCache.getLotteryRankList();
			}
		} else {
			List list = dao.findBySql(sql.toString());
			HcNewyearActivitiCache.saveLotteryRankList(list);
			resultList = HcNewyearActivitiCache.getLotteryRankList();
		}
		return resultList;
	}
	
	/**
	 * 奖品信息：1、IPad MINI；2、Kindle电子书；3、新年台历（红筹版）
	 *  4、3元红包；5、5元红包 ； 6、10元嗒嗒代金券；7、20元嗒嗒代金券；8、50元嗒嗒代金券； 
	 * */
	public int getLotteryResult(Date now, Userbasicsinfo user) {
		int prizeType = 0;
		Long userId = user.getId();
		String currentTime = DateFormatUtil.dateToString(now, "yyyy-MM-dd HH:mm:ss");
		String currentDate = currentTime.substring(0, 10);
		// 先抽实物奖品
		String hour = currentTime.substring(11, 13);//小时
		int nowHour = Integer.valueOf(hour);
		if(nowHour >= 6 && nowHour <= 22) {
			// 设置奖品发放点
			if(nowHour >= 9) {
				prizeType = materialLottery(userId, currentDate);
			}
		}
		// 如果未中实物奖品，抽嗒嗒代金券
		if (prizeType < 1) {
			// 嗒嗒代金券发放时间间隔不低于60second
			if(!RedisHelper.isKeyExistSetWithExpire("STR:MATERIAL:NEWYEAR:TAPTAP:LOCK", 60)){
				//prizeType = tapTapVoucherCouponLottery(user, now);
			}
		}
		// 如果未中实物奖品和抽嗒嗒代金券，抽红包
		if (prizeType < 1) {
			prizeType = threeOrFivePacketLottery(userId, now);
		}
		return prizeType;
	}
	/** 10、20、50元嗒嗒代金券抽奖逻辑 */
	private int tapTapVoucherCouponLottery(Userbasicsinfo user, Date now) {
		int prizeType = 0;  // 抽奖结果值
		int arr[] = {10,0,20,0,50,0,0};  // 定义嗒嗒代金券随机数组，0表示没有
		int tapTapRandom = getRandomByParam(arr);
		Long userId = user.getId();
		try {
			String currentTime = DateFormatUtil.dateToString(now, "yyyy-MM-dd HH:mm:ss");
			String currentDate = currentTime.substring(0, 10);
			// 获取当天发放的"嗒嗒代金券"金额
			int tapTodayMoney = HcNewyearActivitiCache.getTapTapTodayNum(currentDate,tapTapRandom);
			if (tapTodayMoney == 0 && tapTapRandom != 0) { // 表示当天未发放该“嗒嗒代金券”且随机数非0(未中券)
				// 生成嗒嗒巴士代金券对象
				DadaBusCashCertificate dadaBusCash = dadaBusService.generateDadaBusCash(userId, (double)tapTapRandom, user.getUserrelationinfo().getPhone());
				dao.save(dadaBusCash);  // 保存
				dadaBusService.dispatchDadaBusCash(dadaBusCash);  // 发券，与嗒嗒巴士同步
				HcNewyearActivitiCache.incrTapTapTodayNum(currentDate, tapTapRandom);  // 保存当天发放的"嗒嗒代金券"金额
				if (tapTapRandom == 10) { // 设置返回结果
					prizeType = 6;
				} else if (tapTapRandom == 20) {
					prizeType = 7;
				} else if (tapTapRandom == 50) {
					prizeType = 8;
				}
				LOG.error("---“"+tapTapRandom+"元嗒嗒代金券”发放成功！---");
			}
		} catch (Exception e) {
			LOG.error("-----抽奖送嗒嗒巴士代金券出现错误-----");
			e.printStackTrace();
		}
		return prizeType;
	}
	
	/** 3元、5元红包抽奖逻辑 */
	private int threeOrFivePacketLottery(long userId, Date now) {
		int prizeType = 3; // 抽奖结果值
		int arr[] = {3,5,0};  // 定义红包金额随机数组，0表示没有
		int packetTapRandom = getRandomByParam(arr);
		try {
			String currentDate = DateFormatUtil.dateToString(now, "yyyy-MM-dd");
			// 获取当天发放“5元红包”的个数
			int packetTodayNum = HcNewyearActivitiCache.getTapTapTodayNum(currentDate,packetTapRandom);
			if (packetTapRandom == 5 && packetTodayNum < 3) {
				prizeType = 5;  // 更改为5元红包
				saveRedEnvelopeDetail(userId,0D , now, 5.0);
				HcNewyearActivitiCache.incrTapTapTodayNum(currentDate, packetTapRandom);  // 累计当天发放的"5元红包"个数
				LOG.error("---“5元红包”发放成功！---");
			} else {
				prizeType = 4;  // 更改为3元红包
				saveRedEnvelopeDetail(userId,0D , now, 3.0);
				LOG.error("---“3元红包”发放成功！---");
			}
		} catch (Exception e) {
			LOG.error("-----抽奖送红包出现错误-----");
			e.printStackTrace();
		}
		return prizeType;
	}
	
	/** 保存红包信息至数据库 */
	private void saveRedEnvelopeDetail(long userId,Double lowestMoney, Date now, Double money) {
		/** 最近一次发放红包奖品的时间 */
		String lotteryTime = DateFormatUtil.dateToString(now, "yyyy-MM-dd HH:mm:ss");
		Userbasicsinfo userbasicsinfo = new Userbasicsinfo();
		userbasicsinfo.setId(userId);
		RedEnvelopeDetail redEnvelopeDetail = new RedEnvelopeDetail();
		redEnvelopeDetail.setUserbasicsinfo(userbasicsinfo);
		redEnvelopeDetail.setMoney(money);
		redEnvelopeDetail.setLowestUseMoney(lowestMoney);
		redEnvelopeDetail.setReceiveTime(lotteryTime);
		redEnvelopeDetail.setBeginTime(lotteryTime.substring(0, 10));
		Date endTime = DateFormatUtil.increaseDay(now, 30);
		redEnvelopeDetail.setEndTime(DateFormatUtil.dateToString(endTime, "yyyy-MM-dd"));
		redEnvelopeDetail.setUseFlag(0);
		redEnvelopeDetail.setSourceType(4);
		dao.save(redEnvelopeDetail);
	}
	
	/** 实物抽奖逻辑   */
	private int materialLottery(long userId, String currentDate) {
		int prizeType = 0;
		// 总共发放实物奖品数量 
		int totalNum = HcNewyearActivitiCache.getTotalLotteryNum();
		if(totalNum < 130) {   // iPad2个、电子书2个、红筹台历126个
			int totalMoney = HcNewyearActivitiCache.getNewyearInvestMoney(userId); // 获取该用户在活动范围投资优先的总投资额
			int arr[] = {1,2,3};  // 定义实物奖品随机数组，1、IPad 2、Kindle 3、台历
			int materialRandom = getRandomByParam(arr);
			// VIP区的奖品用户不能重复中 
			if(!HcNewyearActivitiCache.isUserHaveVIPPrize(userId)) {
				if (materialRandom == 1) {
					int ipad_num = HcNewyearActivitiCache.getOutPutPrizeNum(0); // 获取“IPad MINI”已发放数量
					// 获取“IPad MINI”当天已发放数量
					int today_ipad_num = HcNewyearActivitiCache.getOutPutTodayPrizeNum(0, currentDate);
					if (ipad_num < 2 && today_ipad_num == 0) {  // 最多发放2个“IPad mini”，且一天最多发放一个
						if (totalMoney >= 300000) {  // 累计投资额满30w
							prizeType = lockMaterialLotter(userId, currentDate, 0);
							if (prizeType > 0) {
								LOG.error("---“IPad Mini”已发放，还剩"+(1-ipad_num)+"个！---");
							}
						}
					}
				}
				if (materialRandom == 2) {
					int ebook_num = HcNewyearActivitiCache.getOutPutPrizeNum(1); // 获取“Kindle电子书”已发放数量
					// 获取“Kindle电子书”当天已发放数量
					int today_ebook_num = HcNewyearActivitiCache.getOutPutTodayPrizeNum(1, currentDate);
					if (ebook_num < 2 && today_ebook_num == 0) {  // 最多发放2个“Kindle电子书”，且一天最多发放一个
						if (totalMoney >= 100000) {  // 累计投资额满10w
							prizeType = lockMaterialLotter(userId, currentDate, 1);
							if (prizeType > 0) {
								LOG.error("---“Kindle电子书”已发放，还剩"+(1-ebook_num)+"个！---");
							}
						}
					}
				}
			}
			if (materialRandom == 3 && !RedisHelper.isKeyExistSetWithExpire("STR:MATERIAL:NEWYEAR:DESKCALD:LOCK", 10 * 60)) {  // 间隔10分钟
				int calendar_num = HcNewyearActivitiCache.getOutPutPrizeNum(2); // 获取“新年台历”已发放数量
				// 获取“红筹台历”当天已发放数量
				int today_calendar_num = HcNewyearActivitiCache.getOutPutTodayPrizeNum(2, currentDate);
				if (calendar_num < 126 && today_calendar_num < 2) {  // 最多发放126个“新年台历”，且每天最多发放2个
					prizeType = lockMaterialLotter(userId, currentDate, 2);
					if (prizeType > 0) {
						LOG.error("---“红筹台历”已发放"+(calendar_num+1)+"个，还剩"+(125-calendar_num)+"个，当天已发放"+(today_calendar_num+1)+"个！---");
					}
				}
			}
		}
		return prizeType;
	}
	
	/**
	 * 发放实物奖品
	 * @param userId 用户id
	 * @param currentDate 当前时间
	 * @param p_index 奖品列表中对应的奖品索引( 1、IPad MINI 2、Kindle电子书3、红筹台历)
	 * @return result 奖品类别1、IPad 2、Kindle 3、红筹台历
	 */
	private int lockMaterialLotter(long userId, String currentDate,int p_index) {
		int result = 0;
		/** 实物奖品并发逻辑处理 */
		String concurrentLock = "STR:MATERIAL:NEWYEAR:CONCURRENT:LOCK";
		if(!RedisHelper.isKeyExistSetWithExpire(concurrentLock, 60)) {
			try {
				Date now = new Date();
				PrizeInfo prizeInfo = grantMaterialLottery(p_index);
				if(prizeInfo != null) {
					int prizeType = Integer.valueOf(prizeInfo.getPrizeType());
					if(prizeType > 0) {
						PrizeDetail prizeDetail = new PrizeDetail();
						prizeDetail.setUserId(userId);
						prizeDetail.setPrizeType(prizeType);
						prizeDetail.setReceiveTime(DateFormatUtil.dateToString(now, "yyyy-MM-dd HH:mm:ss"));
						prizeDetail.setPrizeName(prizeInfo.getPrizeName());
						dao.save(prizeDetail);
						if (p_index == 0) {
							result = 1;
							// 保存当前用户的实物奖品
							HcNewyearActivitiCache.giveVIPPrizeToUser(userId, "" + prizeType);
						} else if (p_index == 1) {
							result = 2;
							// 保存当前用户的实物奖品
							HcNewyearActivitiCache.giveVIPPrizeToUser(userId, "" + prizeType);
						} else {
							result = 3;
						}
						
						// 更新总实物奖品发放数量
						HcNewyearActivitiCache.increaseTotalLotteryNum();
						// 设置实物奖品发放数量
						HcNewyearActivitiCache.setOutPutPrizeNum(p_index);
						// 设置当天实物奖品发放数量
						HcNewyearActivitiCache.setOutPutTodayPrizeNum(p_index, currentDate);;
					}
				}
			} catch (Exception e) {
				LOG.error("-----抽奖送实物奖品出现错误-----");
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/** 实物奖品发放 */
	private PrizeInfo grantMaterialLottery(int prize_index) {
		List<PrizeInfo> prizeList = HcNewyearActivitiCache.getNewyearMaterialPrizeList();
		if(prizeList != null && prizeList.size() > 0) {
			PrizeInfo prizeInfo = prizeList.get(prize_index);
			return prizeInfo;
		}
		return null;
	}
	
	/**
	 * 查询当前用户的抽奖次数，如果为0，表示该用户还没有抽过奖
	 * @param userId
	 * @return
	 */
	public Integer isTodayFirstLottery(Long userId,String currentDate) {
		String sql = "select count(1) from loanrecord where userbasicinfo_id=? and date_format(tenderTime,'%Y-%m-%d')=?";
		Object obj = dao.findObjectBySql(sql, userId, currentDate);
		Integer count = 0;
		if (obj != null) {
			count = Integer.valueOf(obj.toString());
		}
		return count;
	}
	
	private Integer getRandomByParam(int[] arr) {
	   	Random random = new Random();//创建随机对象
	   	int arrIdx = random.nextInt(arr.length);//随机数组索引
	   	return arr[arrIdx];
	}
	
	/** 中奖列表相关数据 */
	public List<PrizeDetail> queryLotteryListByUserId(Long userId) {
		String beginDate = HcNewyearActivitiCache.getActiveBeginDate() + " 00:00:00";
		String endDate = HcNewyearActivitiCache.getActiveEndDate() + " 23:59:59";
		StringBuffer sql = new StringBuffer("select * from prizedetail");
		sql.append(" where receivetime>='" + beginDate + "' and receivetime<='" + endDate + "' and userId = ? ");
		sql.append("order by receivetime desc");
		List<PrizeDetail> list = dao.findBySql(sql.toString(), PrizeDetail.class, userId);
		return list;
	}
}