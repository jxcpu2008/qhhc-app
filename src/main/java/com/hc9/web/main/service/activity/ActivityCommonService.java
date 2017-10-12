package com.hc9.web.main.service.activity;

import java.math.BigInteger;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.ActivityMonkey;
import com.hc9.web.main.entity.InterestIncreaseCard;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.PrizeDetail;
import com.hc9.web.main.entity.WithdrawCard;
import com.hc9.web.main.redis.activity.year2016.month05.HcNewerTaskCache;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.DateUtil;
import com.hc9.web.main.util.DateUtils;

/** 活动通用服务 */
@Service
public class ActivityCommonService {
	@Resource
	private HibernateSupport dao;
	
	/** 判断是否为符合使用红包要求
	 * 
	 *  平台的3元，5元红包没有投资期限的限制，10，30，60元红包需要投资期限达到25天及以上期限的标的才可以使用
	 *  */
	public boolean isValidForUseRed(Loansign loan) {
		boolean flag = true;
		if(loan != null) {
			/** 标类型：1-店铺  2-项目 3-天标 4-债权转让 */
			int loanType = loan.getType();
			/** 回购期限:如果是天标的话为天数，项目的话为月数 */
			int reMonth = loan.getRemonth();
			if(loanType == 3 && reMonth < 25) {
				flag = false;
			}
		} else {
			flag = false;
		}
		return flag;
	}
	
	/** 判断用户是否从某个渠道过来的用户 */
	public boolean isUserFromSpecialChannelInActivityTime(String spreadId, String userName, 
			String beginTime, String endTime) {
		boolean result = false;
		String sql = "select count(id) from channelspreaddetail where spreadId=? and regUserName=? "
				+ "and regStatus=1 and createTime>=? and createTime<=?";
		Object obj = dao.findObjectBySql(sql, spreadId, userName, beginTime, endTime);
		BigInteger sumNum = (BigInteger)obj;
		/** 是否有记录 */
		if(sumNum.longValue() > 0) {
			result = true;
		}
		return result;
	}
	
	/** 判断用户是否已经投资过 */
	public boolean hasSuccessInvestRecordBefore(long userId) {
		boolean result = false;
		String sql = "select count(*) from loanrecord where isSucceed=1 and userbasicinfo_id=?";
		Object obj = dao.findObjectBySql(sql, userId);
		BigInteger sumNum = (BigInteger)obj;
		/** 已有投资记录 */
		if(sumNum.longValue() > 0) {
			result = true;
		}
		return result;
	}
	
	/** 判断当前投资记录是否属于投资成功的首投 */
	public boolean isFirstInvestSuccess(long userId, long loanRecordId) {
		boolean result = false;
		String sql = "select count(*) from loanrecord where isSucceed=1 and userbasicinfo_id=? and id!=?";
		Object obj = dao.findObjectBySql(sql, userId, loanRecordId);
		BigInteger sumNum = (BigInteger)obj;
		if(sumNum.longValue() < 1) {
			result = true;
		}
		return result;
	}
	
	/** 判断用户是否首次充值 */
	public boolean isFirstRechargeSuccess(long id) {
		boolean result = false;
		String sql = "select count(*) from recharge where status=1 and id!=?";
		Object obj = dao.findObjectBySql(sql, id);
		BigInteger sumNum = (BigInteger)obj;
		if(sumNum.longValue() < 1) {
			result = true;
		}
		return result;
	}
	
	/** 判断用户是否已经获取过某种类型的实物奖品 */
	public boolean isUserGetPrizeByType(long userId, int prizeType){
		int ticketNum = 0;
		String sql = "select count(id) from prizedetail where userId=? and prizeType=?";
		Object obj = dao.findObjectBySql(sql, userId, prizeType);
		if(obj != null) {
			ticketNum = Integer.valueOf(obj.toString());
		}
		return ticketNum > 0;
	}
	
	/** 保存奖品一：现金券 */
	public void saveAcivityMoney(long userId, String phone, double rewardMoney, int type, 
			double investMoney, long loanRecordId, long loanId, String loanName) {
		Date date = new Date();
		String createTime = DateFormatUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss");
		
		ActivityMonkey activityMonkey = new ActivityMonkey();
		activityMonkey.setUserId(userId);
		activityMonkey.setMobilePhone(phone);
		activityMonkey.setMoney(investMoney);
		activityMonkey.setType(type);
		activityMonkey.setLoanId(loanId);
		activityMonkey.setLoanName(loanName);
		activityMonkey.setLoanRecordId(loanRecordId);
		activityMonkey.setRewardMoney(rewardMoney);
		activityMonkey.setCreateTime(createTime);
		activityMonkey.setWeek(0);
		activityMonkey.setStatus(0);
		activityMonkey.setExamineStatus(0);
		dao.save(activityMonkey);
	}
	
	/** 保存奖品二：红包
	 *  @param userId 领取红包的用户id
	 *  @param bonusMoney 红包金额
	 *  @param lowestUseMoney 最低使用门槛
	 *  @param sourceType 红包来源 来源类型：1、投资；2、注册；3、奖励；4、抽奖；5、老客户送红包；
	 *  					6，38.以后作为节假日用,7.补发调整 8、四月活动 9、关注微信 10、充值；11、生日礼包；
	 *  					
	 *  @param effectMonth 有效期
	 *  @param 
	 *  */
	public void saveRedEnvelope(long userId, double bonusMoney, double lowestUseMoney, 
			int sourceType, int effectMonth) {
		String receiveTime = DateUtils.format("yyyy-MM-dd HH:mm:ss");
		String beginTime = DateUtils.format("yyyy-MM-dd");
		String endTime = DateUtil.getSpecifiedMonthAfter(beginTime, effectMonth);
		String sql = "insert into redenvelopedetail" + 
				"(userId,money,lowestUseMoney,receiveTime,beginTime,endTime,useFlag,sourceType) " + 
				"values(?,?,?,?,?,?,0,?)";
		dao.executeSql(sql, userId, bonusMoney, lowestUseMoney, receiveTime, 
				beginTime, endTime, sourceType);
		HcNewerTaskCache.setCouponTipFlag(userId);
	}
	
	/** 保存奖品三：加息券 */
	public void saveInterestIncreaseCard(long userId, double interestRate, 
			double lowestUseMoney, int sourceType, int effectMonth) {
		String time = DateUtils.format("yyyy-MM-dd HH:mm:ss");
		InterestIncreaseCard interest= new InterestIncreaseCard();
		interest.setUserId(userId);
		interest.setLoanrecordId(0l);
		interest.setLowestUseMoney(lowestUseMoney);
		interest.setInterestRate(interestRate);
		interest.setReceiveTime(time);
		String beginTime = DateUtils.format("yyyy-MM-dd");
		interest.setBeginTime(beginTime);
		interest.setEndTime(DateUtil.getSpecifiedMonthAfter(beginTime, effectMonth));
		interest.setUseFlag(0);
		interest.setSourceType(sourceType);
		dao.save(interest);
		HcNewerTaskCache.setCouponTipFlag(userId);
	}
	
	/** 保存奖品四：提现券 */
	public void saveWithdrawCard(long userId, double freeMoney, int sourceType, int effectMonth) {
		String time = DateUtils.format("yyyy-MM-dd HH:mm:ss");
		WithdrawCard withdrawCard= new WithdrawCard();
		withdrawCard.setUserId(userId);
		withdrawCard.setFreeMoney(freeMoney);
		withdrawCard.setRealUseMoney(0.0);
		withdrawCard.setReceiveTime(time);
		String beginTime = DateUtils.format("yyyy-MM-dd");
		withdrawCard.setBeginTime(beginTime);
		withdrawCard.setEndTime(DateUtil.getSpecifiedMonthAfter(beginTime, effectMonth));
		withdrawCard.setUseFlag(0);
		withdrawCard.setSourceType(sourceType);
		dao.save(withdrawCard);
		HcNewerTaskCache.setCouponTipFlag(userId);
	}
	
	/** 保存奖品五：实物奖品
	 *  @param userId 投资用户id
	 *  @param prizeType 实物奖品类型：1：运动相机；2：体脂秤；3：冰箱卫士；...| 
	 *  7、蝙蝠侠抱枕（20元）；8、小米迷你风扇+移动电源；9、Kindle电子书；10、智能航拍无人机  
	 *  11、IPad MINI 12、红筹台历 13 聚橙网演唱会门票 
	 *  @param prizeName 奖品名称
	 *  @param prizeNum 奖品数量：默认为1
	 *  @param loanRecordId 投资记录id
	 *  */
	public void savePrizedetail(long userId, int prizeType, String prizeName, int prizeNum, long loanRecordId) {
		String receiveTime = DateUtils.format("yyyy-MM-dd HH:mm:ss");
		PrizeDetail prizeDetail = new PrizeDetail();
		prizeDetail.setUserId(userId);
		prizeDetail.setPrizeType(prizeType);
		prizeDetail.setReceiveTime(receiveTime);
		prizeDetail.setPrizeName(prizeName);
		prizeDetail.setPrizeNum(prizeNum);
		prizeDetail.setLoanRecordId(loanRecordId);
		dao.save(prizeDetail);
	}
}
