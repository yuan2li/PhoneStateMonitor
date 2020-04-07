package com.ly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ly.PhoneState;

/** 
 * 
* @name MyHttpServer
* @description 扩展HttpServlet类，重写doGet()和doPost()方法，分别实现服务器与网页端、手机客户端的数据交互
 */
@WebServlet("/MyHttpServer") //访问Servlet的url（相对路径）
public class MyHttpServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    private static String phoneID; //手机标识
    private static String intervalTime; //采集周期

	/**
	 * 处理Get方法请求，执行相应的数据库操作并返回数据，同时接收网页端设置的新的采集周期值
	* @param request 用来封装Http请求信息的对象
	* @param response 用来生成Http响应信息的对象
	* @throws ServletException
	* @throws IOException
	* @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置编码方式
		request.setCharacterEncoding("utf-8");  
		response.setCharacterEncoding("utf-8");
		//禁用缓存，确保网页信息是最新数据  
        response.setHeader("Pragma","No-cache");  
        response.setHeader("Cache-Control","no-cache");      
        response.setDateHeader("Expires", -10);
        
        //接收网页端的数据，执行数据库查询操作
		try {
			//获取最新的采集周期值
			intervalTime = request.getParameter("intervalTime");
			Connection conn = MyServerSql.ConnectToDB();
            List<PhoneState> psList = new ArrayList<PhoneState>();
            int rowNum = MyServerSql.getRowNum(conn, phoneID);
            
            //设置在网页端显示最多5条包括实时状态在内的历史信息
            if(rowNum <= 5) {
            	psList = MyServerSql.Select(conn, phoneID, 0, rowNum);
            }else {
            	psList = MyServerSql.Select(conn, phoneID, rowNum - 5, -1);
            }
            System.out.println(">>>Query " + psList.size() + " records");
            
            //创建Json对象存储查询到的结果
            JSONArray jsonArray = new JSONArray();
            String str = "";
            Integer inum = 0;
            Double fnum = 0.0;
            for(int i = 0; i < psList.size(); ++i) {
				JSONObject jsonObject = new JSONObject();
                jsonObject.put("phoneID", psList.get(i).getPhoneID());
                jsonObject.put("recordTime", psList.get(i).getRecordTime());
                str = NumFromStr(psList.get(i).getAvailRAM());
                fnum = Double.valueOf(str);
                jsonObject.put("availRAM", fnum);
                str = NumFromStr(psList.get(i).getTotalRAM());
                fnum = Double.valueOf(str);
                jsonObject.put("totalRAM", fnum);
                str = NumFromStr(psList.get(i).getAvailROM());
                fnum = Double.valueOf(str);
                jsonObject.put("availROM", fnum);
                str = NumFromStr(psList.get(i).getTotalROM());
                fnum = Double.valueOf(str);
                jsonObject.put("totalROM", fnum);
                str = NumFromStr(psList.get(i).getSignalStrength());
                inum = Integer.valueOf(str);
                jsonObject.put("signalStrength", inum);
                str = NumFromStr(psList.get(i).getBatteryPower());
                inum = Integer.valueOf(str);
                jsonObject.put("batteryPower", inum);
                jsonObject.put("latitude", psList.get(i).getLatitude());
                jsonObject.put("longitude", psList.get(i).getLongitude());
                jsonObject.put("address", psList.get(i).getAddress());
                jsonArray.put(jsonObject);
            }
            
			//向网页端返回数据
			OutputStream os = response.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os,"utf-8");
			BufferedWriter bw = new BufferedWriter(osw);
            bw.write(jsonArray.toString());
            bw.flush();
            System.out.println(jsonArray.toString());
            
            //关闭相关数据流及数据库连接
			os.close();
            osw.close();
            bw.close();
            conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理post方法请求，接收来自手机客户端的数据并将其写入数据库，同时返回新的采集周期值
	* @param request 用来封装Http请求信息的对象
	* @param response 用来生成Http响应信息的对象
	* @throws ServletException
	* @throws IOException
	* @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置编码方式
		request.setCharacterEncoding("utf-8");  
		response.setCharacterEncoding("utf-8");
		
		//获取客户端的请求，接收传输的数据进行处理
		InputStream is = request.getInputStream();
		InputStreamReader isr = new InputStreamReader(is,"utf-8");
		BufferedReader br = new BufferedReader(isr);
		String jsonData = br.readLine();
		System.out.println(">>>Data receiving...");
		//初始化接收的传输对象为空
		PhoneState phoneState = null;
		try {
			JSONArray jsonArray = new JSONArray(jsonData);
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			phoneState = new PhoneState();
			phoneState.setPhoneID(jsonObject.getString("phoneID"));
			phoneState.setRecordTime(jsonObject.getString("recordTime"));
			phoneState.setAvailRAM(jsonObject.getString("availRAM"));
			phoneState.setTotalRAM(jsonObject.getString("totalRAM"));
			phoneState.setAvailROM(jsonObject.getString("availROM"));
			phoneState.setTotalROM(jsonObject.getString("totalROM"));
			phoneState.setSignalStrength(jsonObject.getString("signalStrength"));
			phoneState.setBatteryPower(jsonObject.getString("batteryPower"));
			phoneState.setLatitude(jsonObject.getString("latitude"));
			phoneState.setLongitude(jsonObject.getString("longitude"));
			phoneState.setAddress(jsonObject.getString("address"));
			phoneID = phoneState.getPhoneID();
			//打印接收数据
			System.out.println("PhoneID:"+phoneState.getPhoneID());
			System.out.println("RecordTime:"+phoneState.getRecordTime());
			System.out.println("AvailRAM:"+phoneState.getAvailRAM()+" TotalRAM:"+phoneState.getTotalRAM()+
					" AvailROM:"+phoneState.getAvailROM()+" TotalROM:"+phoneState.getTotalROM());
			System.out.println("SignalStrength:"+phoneState.getSignalStrength());
			System.out.println("BatteryPower:"+phoneState.getBatteryPower());
			System.out.println("Latitude:"+phoneState.getLatitude()+" Longitude:"+phoneState.getLongitude());
			System.out.println("Address:"+phoneState.getAddress());
			
			//返回响应结果，即新的采集周期值
			OutputStream os = response.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os,"utf-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(intervalTime);
			bw.flush();
			
			//将接收的数据写入数据库
			Connection conn = MyServerSql.ConnectToDB();
            int res = MyServerSql.Insert(conn, phoneState);
            System.out.println(">>>Insert " + res + " row");
            
            //关闭相关数据流及数据库连接
			os.close();
            osw.close();
            is.close();
            isr.close();
            br.close();
            bw.close();
            conn.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @title NumFromStr 
	* @description 从字符串中提取数字形式的子串
	* @param str 待处理源字符串
	* @return 数字形式的字符串
	 */
	public static String NumFromStr(String str) {
		String str1 = str.trim();
		String str2 = "";
		if(str1 != null && !("".equals(str1))){
			for(int i = 0; i < str1.length(); ++i){
				if((str1.charAt(i) == '.') || (str1.charAt(i) >= '0' && str1.charAt(i) <= '9')){
					str2 += str1.charAt(i);
				}
			}
		}
		return str2;
	}

}
