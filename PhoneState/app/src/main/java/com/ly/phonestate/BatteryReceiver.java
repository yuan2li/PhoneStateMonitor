package com.ly.phonestate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 广播接收器类，用来监测电池电量变化
 * 重写BroadcastReceiver类的onReceive()方法来接收以监测电池的意图对象为参数的消息
 */
public class BatteryReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String power;
        final PhoneState app = (PhoneState)context.getApplicationContext();
        final String action = intent.getAction();
        if (action != null && action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)){
            /* 获取电池的最大电量值 */
            int scale = intent.getIntExtra("scale", -1);
            /* 获取电池的当前电量，应位于0-scale之间 */
            int level = intent.getIntExtra("level", -1);
            /* 转化为电量百分比的形式 */
            int ratio = level * 100 / scale;
            power = ratio + "%";
            app.setBatteryPower(power);
        }
    }
}
