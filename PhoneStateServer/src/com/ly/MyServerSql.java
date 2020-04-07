package com.ly;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 
* @name MyServerSql
* @description 实现服务器对数据库的相关操作，包括连接数据库、插入或查询数据等操作
 */
public class MyServerSql {
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver"; //MySQL 8.0+ 驱动
	private static final String URL = "jdbc:mysql://127.0.0.1:3306/phonestate?useUnicode=true&"
			+ "characterEncoding=utf8&useSSL=true&serverTimezone=GMT%2B8&zeroDateTimeBehavior=convertToNull";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "1102";
	private static Connection conn = null;
	
	/**
	 * 
	* @title ConnectToDB 
	* @description 连接数据库
	* @return 生成的数据库连接对象
	 */
	public static Connection ConnectToDB() {
		try {
			//加载MySQL驱动
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			if(!conn.isClosed()) {
				System.out.println(">>>Connected to Database!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 
	* @title Insert 
	* @description 插入一个对象作为一条记录
	* @param conn 数据库连接对象
	* @param phoneState 待插入的数据对象
	* @return 成功插入的记录条数
	* @throws SQLException
	 */
	public static int Insert(Connection conn, PhoneState phoneState) throws SQLException {
		PreparedStatement ps = null;
		int res = 0;
		String sql = "INSERT INTO phonestate VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        ps = conn.prepareStatement(sql);
        ps.setString(1, phoneState.getPhoneID());
        ps.setString(2, phoneState.getRecordTime());
        ps.setString(3, phoneState.getAvailRAM());
        ps.setString(4, phoneState.getTotalRAM());
        ps.setString(5, phoneState.getAvailROM());
        ps.setString(6, phoneState.getTotalROM());
        ps.setString(7, phoneState.getSignalStrength());
        ps.setString(8, phoneState.getBatteryPower());
        ps.setString(9, phoneState.getLatitude());
        ps.setString(10, phoneState.getLongitude());
        ps.setString(11, phoneState.getAddress());
        res = ps.executeUpdate();
        ps.close();
        return res;
	}
	
	/**
	 * 
	* @title Select 
	* @description 查询并返回当前手机对应的一组记录，最多5条
	* @param conn 数据库连接对象
	* @param phoneID 手机标识
	* @param begin 第一个返回记录行的偏移量
	* @param offset 返回记录行的最大数目，-1表示返回从偏移量到记录集结束行之间所有的记录行
	* @return 查询结果的对象队列
	* @throws SQLException
	 */
	public static List<PhoneState> Select(Connection conn, String phoneID , int begin, int offset) throws SQLException {
		List<PhoneState> psList = new ArrayList<PhoneState>();
		Statement statement = conn.createStatement();
		String sql = "select * from phonestate where phoneID = '" + phoneID + "'limit " + begin + "," + offset;
		ResultSet rs = statement.executeQuery(sql);
		while(rs.next()) {
			PhoneState phoneState = new PhoneState();
			phoneState.setPhoneID(rs.getString("phoneID"));
			phoneState.setRecordTime(rs.getString("recordTime"));
			phoneState.setAvailRAM(rs.getString("availRAM"));
			phoneState.setTotalRAM(rs.getString("totalRAM"));
			phoneState.setAvailROM(rs.getString("availROM"));
			phoneState.setTotalROM(rs.getString("totalROM"));
			phoneState.setSignalStrength(rs.getString("signalStrength"));
			phoneState.setBatteryPower(rs.getString("batteryPower"));
			phoneState.setLatitude(rs.getString("latitude"));
			phoneState.setLongitude(rs.getString("longitude"));
			phoneState.setAddress(rs.getString("address"));
			psList.add(phoneState);
		}
		rs.close();
		statement.close();
		return psList;
	}
	
	/**
	* @title getRowNum 
	* @description 获取表中属于当前手机的记录的总行数
	* @param conn 数据库连接对象
	* @param phoneID 手机标识
	* @return 查询到的记录行数
	* @throws SQLException
	 */
	public static int getRowNum(Connection conn, String phoneID) throws SQLException {
		int rowNum = 0;
		String sql = "select count(phoneID) as result from phonestate where phoneID = '" + phoneID + "'";
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(sql);
		while(rs.next()) {
			rowNum = rs.getInt("result");
		}
		rs.close();
		statement.close();
		return rowNum;
	}
}
