package com.hc9.web.main.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Recharge;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.PageModel;

/** 在线充值的业务处理 */
@Service
public class RechargesService {
	@Resource
	private HibernateSupport dao;

	/**
	 * 查询充值信息
	 * 
	 * @param id
	 *            当前登录账户编号
	 * @param beginTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @return list 返回当前用户的充值信息
	 */
	public List<Recharge> rechargeList(Long id, String beginTime,
			String endTime, Integer search, PageModel page) {
		StringBuffer sql = new StringBuffer("SELECT * FROM recharge r where r.user_id="+ id);
		StringBuffer sqlCount = new StringBuffer(
				"select count(1) from recharge r  where r.user_id=" + id);
		if (StringUtil.isNotBlank(beginTime)) { // 开始时间
			sql.append(" and date_format(r.time,'%Y-%m-%d')>='").append(
					beginTime + "'");
			sqlCount.append(" and date_format(r.time,'%Y-%m-%d')>='").append(
					beginTime + "'");
		}
		if (StringUtil.isNotBlank(endTime)) { // 结束时间
			sql.append(" and date_format(r.time,'%Y-%m-%d')<='").append(
					endTime + "'");
			sqlCount.append(" and date_format(r.time,'%Y-%m-%d')<='").append(
					endTime + "'");
		}
		if (search != null && !"".equals(search)) { // 最近几个月
			sql.append(" and DATE_SUB(now(),INTERVAL " + search
					+ " MONTH) <= r.time");
			sqlCount.append(" and DATE_SUB(now(),INTERVAL " + search
					+ " MONTH) <= r.time");
		}
		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue()); // 获取总记录数
		sql.append(" ORDER BY r.time DESC LIMIT "+(page.getPageNum() - 1) * page.getNumPerPage())
				.append(",").append(page.getNumPerPage());
		List<Recharge> list = dao.findBySql(sql.toString(), Recharge.class);
		page.setList(list);
		return list;
	}

	/** 根据pId查询充值记录信息 */
	public Recharge selRecharge(String rId) {
		String sql = "select * from recharge where id=?";
		Recharge recharge = dao.findObjectBySql(sql, Recharge.class, rId);
		return recharge;
	}

}