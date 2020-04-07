package com.ly.phonestate;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 客户端通信类，实现与服务器建立连接并进行数据交互
 * 接收服务器的应答，返回新的采集周期值供客户端重新设定
 */
@SuppressWarnings("WeakerAccess") //忽略访问权限警告
public class MyHttpClient {
    private String intervalTime; //采集周期

    public String sendToServer(PhoneState phoneState){
        try{
            //服务器url根据其当前ip预先进行调整
            URL url = new URL("http://192.168.0.102:8080/PhoneStateServer/MyHttpServer");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            //设置可读可写
            http.setDoInput(true);
            http.setDoOutput(true);
            //禁用缓存
            http.setUseCaches(false);
            //设置传输方式为post
            http.setRequestMethod("POST");
            //与服务器建立连接
            Log.d("TAG", ">>>Connecting...");
            http.connect();
            Log.d("TAG", ">>>Connected to Server!");

            //数据写入流发送至服务器
            OutputStream os = http.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            //创建json对象并写入数据
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("phoneID", phoneState.getPhoneID());
            jsonObject.put("recordTime", phoneState.getRecordTime());
            jsonObject.put("availRAM", phoneState.getAvailRAM());
            jsonObject.put("totalRAM", phoneState.getTotalRAM());
            jsonObject.put("availROM", phoneState.getAvailROM());
            jsonObject.put("totalROM", phoneState.getTotalROM());
            jsonObject.put("signalStrength", phoneState.getSignalStrength());
            jsonObject.put("batteryPower", phoneState.getBatteryPower());
            jsonObject.put("latitude", phoneState.getLatitude());
            jsonObject.put("longitude", phoneState.getLongitude());
            jsonObject.put("address", phoneState.getAddress());
            jsonArray.put(jsonObject);
            bw.write(jsonArray.toString());
            bw.flush();
            Log.d("TAG", ">>>Data sending...");

            //数据读入流接收服务器数据
            InputStream is = http.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            //接收服务器的应答，即经过管理员配置后的新的采集周期值
            if(!("error".equals(result))){
                intervalTime = result;
            }

            //关闭相关数据流
            os.close();
            osw.close();
            is.close();
            isr.close();
            br.close();
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return intervalTime;
    }
}