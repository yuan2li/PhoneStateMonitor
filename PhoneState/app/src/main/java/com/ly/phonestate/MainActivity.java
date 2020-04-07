package com.ly.phonestate;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

public class MainActivity extends AppCompatActivity{
    PhoneState app;
    private TelephonyManager tm;
    private MyPhoneStateListener mpsListener;
    private BatteryReceiver receiver;
    private Integer intervalTime; //采集周期
    private boolean isStop;
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    // 线程类，设置一个单独的线程处理数据交互
    @SuppressWarnings("InfiniteLoopStatement") //忽略无限循环异常退出警告
    class ThreadShow implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    //根据采集周期启动线程任务
                    Thread.sleep(intervalTime);
                    //监测程序仍在前台运行
                    if(isStop){
                        //定时获取内存信息
                        getTotalMemoryInfo();

                        //获取当前时间记为采集时间
                        Date date = new Date(System.currentTimeMillis());
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                        String recordTime = formatter.format(date);
                        app.setRecordTime(recordTime);

                        //打印采集到的手机运行状态数据
                        Log.d("TAG", app.getPhoneID());
                        Log.d("TAG", app.getRecordTime());
                        Log.d("TAG", app.getAvailRAM()+" "+app.getTotalRAM()
                                +" "+app.getAvailROM()+" "+app.getTotalROM());
                        Log.d("TAG", app.getSignalStrength());
                        Log.d("TAG", app.getBatteryPower());
                        Log.d("TAG", app.getLatitude()+" "+app.getLongitude()
                                +" "+app.getAddress());

                        //定时向服务器发送数据
                        MyHttpClient myHttpClient = new MyHttpClient();
                        String interval = myHttpClient.sendToServer(app);
                        //根据服务器发送的返回值设置新的采集周期
                        intervalTime = Integer.valueOf(interval);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* 创建一个应用对象实例app，存储实际采集的数据 */
        app = (PhoneState) getApplication();

        /* 获取手机标识，包括品牌、型号和GUID */
        String brand = Build.BRAND;
        String model = Build.MODEL;
        String uniqueID = UUID.randomUUID().toString();
        String phoneID = brand + " " + model + " " + uniqueID;
        app.setPhoneID(phoneID);

        /* 获取信号强度 */
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mpsListener = new MyPhoneStateListener();
        //开启信号强度状态监听
        tm.listen(mpsListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        /* 获取电池电量 */
        receiver = new BatteryReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        /* 注册广播接收器，持续监听ACTION_BATTERY_CHANGED事件 */
        registerReceiver(receiver, filter);

        /* 获取地理位置，包括纬度，经度，具体地址*/
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        //设置使用gps
        option.setOpenGps(true);
        //定位SDK内部是一个service，并放到了独立进程，设置在stop的时候杀死这个进程
        option.setIgnoreKillProcess(false);
        //设置发起定位请求的间隔，int类型，单位ms
        option.setScanSpan(3000);
        //设置获得当前点的位置信息描述
        option.setIsNeedLocationDescribe(true);
        //将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocationClient.setLocOption(option);
        //处理用户权限后进行定位
        if (XXPermissions.isHasPermission(MainActivity.this, Permission.Group.STORAGE)) {
            Toast.makeText(MainActivity.this, "Get permission!", Toast.LENGTH_SHORT).show();
            //发起定位请求
            mLocationClient.start();
        }else {
            Toast.makeText(MainActivity.this, "Need permission!", Toast.LENGTH_SHORT).show();
            XXPermissions.gotoPermissionSettings(MainActivity.this);
            mLocationClient.restart();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        isStop = true;
        intervalTime = 3000; //采集周期默认值
        ThreadShow threadShow = new ThreadShow();
        Thread thread = new Thread(threadShow);
        thread.start();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(isStop){
            isStop = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁广播
        unregisterReceiver(receiver);
        //停止监听
        tm.listen(mpsListener, PhoneStateListener.LISTEN_NONE);
        //关闭定位请求
        mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
    }

    /* 获取手机内存使用情况，包括可用RAM，总RAM，可用ROM，总ROM */
    public void getTotalMemoryInfo() {
        ActivityManager.MemoryInfo Info = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(am != null)
        {
            am.getMemoryInfo(Info);
            app.setAvailRAM(Formatter.formatFileSize(getBaseContext(), Info.availMem));
            app.setTotalRAM(Formatter.formatFileSize(getBaseContext(), Info.totalMem));
        }
        //获取磁盘信息
        final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long totalCounts = statFs.getBlockCount();
        long availableCounts = statFs.getAvailableBlocks();
        long size = statFs.getBlockSize();
        long availROMSize = availableCounts * size;
        long totalROMSize = totalCounts * size;
        app.setAvailROM(Formatter.formatFileSize(getBaseContext(), availROMSize));
        app.setTotalROM(Formatter.formatFileSize(getBaseContext(), totalROMSize));
    }

    //重写PhoneStateListener的onSignalStrengthsChanged()方法，当信号强度发生改变的时候就会触发这个事件
    class MyPhoneStateListener extends PhoneStateListener {
        private int dbm = 0;
        private String strength;
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            //通过反射获取当前信号值
            try {
                Method method = signalStrength.getClass().getMethod("getDbm");
                dbm = (int) method.invoke(signalStrength);
                strength = dbm + "dBm";
                app.setSignalStrength(strength);
            } catch (NoSuchMethodException e){
                e.printStackTrace();
            } catch (IllegalAccessException e){
                e.printStackTrace();
            } catch (InvocationTargetException e){
                e.printStackTrace();
            }
        }
    }

    //使用百度地图API获取地理位置
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            String locationDescribe = location.getLocationDescribe();    //获取位置描述信息
            app.setLatitude(String.valueOf(latitude));
            app.setLongitude(String.valueOf(longitude));
            app.setAddress(locationDescribe);
        }
    }
}
