package com.hc9.web.main.util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;

import com.jubaopen.commons.LOG;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * c3p0数据库连接池
 * */
public class C3p0DatasourceUtil {
	private static ComboPooledDataSource cpds = null;
	static {//config/db/
		ResourceBundle bundle = ResourceBundle.getBundle("/config/db/jdbc");
		if (bundle == null) {
			LOG.error("解析数据库连接池配置信息出现异常!");
			throw new IllegalArgumentException("解析数据库连接池配置信息出现异常!");
		}
		
		try {
			cpds = new ComboPooledDataSource();
			cpds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			LOG.error("初始化c3p0数据库连接池出现异常!", e);
			throw new RuntimeException("初始化c3p0数据库连接池出现异常!");
		}
		cpds.setJdbcUrl(bundle.getString("jdbc.url"));
		cpds.setUser(bundle.getString("jdbc.username"));
		cpds.setPassword(bundle.getString("jdbc.password"));
		cpds.setInitialPoolSize(Integer.valueOf(bundle.getString("jdbc.initialPoolSize")));
		cpds.setMinPoolSize(Integer.valueOf(bundle.getString("jdbc.minPoolSize")));
		cpds.setMaxPoolSize(Integer.valueOf(bundle.getString("jdbc.maxPoolSize")));
		cpds.setMaxIdleTime(Integer.valueOf(bundle.getString("jdbc.maxIdleTime")));
		cpds.setAcquireIncrement(Integer.valueOf(bundle.getString("jdbc.acquireIncrement")));
	}

	public static void saveChannelVisitLog(String spreadId, String sessionId) {
		Connection con = null;
		try {
			con = cpds.getConnection();
			String sql = "insert into channelspreaddetail(spreadId,sessionId) values(?,?)";
			PreparedStatement  pstmt = con.prepareStatement(sql);
			pstmt.setString(1, spreadId);
			pstmt.setString(2, sessionId);
			pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			LOG.error("保存渠道来源访问日志出错！", e);
		} finally {
			try {
				if(con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOG.error("保存渠道来源访问日志出错！", e);
			}
		}
	}
	
	/** 保存sessionId首次访问页面的信息 */
	public static void saveFirstRequestOfSessionId(String sessionId, String uri,
			String queryString, String ip, String port, String visitDate) {
		Connection con = null;
		try {
			con = cpds.getConnection();
			String sql = "insert into accesscontrolfirstvisit(sessionId,url,queryParam,ip,port,visitDate) values(?,?,?,?,?,?)";
			PreparedStatement  pstmt = con.prepareStatement(sql);
			pstmt.setString(1, sessionId);
			pstmt.setString(2, uri);
			pstmt.setString(3, queryString);
			pstmt.setString(4, ip);
			pstmt.setString(5, port);
			pstmt.setString(6, visitDate);
			pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			LOG.error("保存sessionId首次访问页面的信息出错！", e);
		} finally {
			try {
				if(con != null) {
					con.close();
				}
			} catch (SQLException e) {
				LOG.error("保存sessionId首次访问页面的信息出错！", e);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Connection con = cpds.getConnection();
         Statement stmt; //创建声明
         stmt = con.createStatement();
         ResultSet res = stmt.executeQuery("select * from channelspread");
         String name;
         while (res.next()) {
        	 name = res.getString("name");
             System.out.println(name);
         }
         con.close();
	}
}
