package com.ly.phonestate;

import android.app.Application;
import java.io.Serializable;

/**
 * 信息实体类，存储需要采集的各项手机数据
 * 实现Serializable接口：保存内存中的对象的状态，便于不同Activity共享
 */
public class PhoneState extends Application implements Serializable {
    private String phoneID;
    private String recordTime;
    private String availRAM;
    private String totalRAM;
    private String availROM;
    private String totalROM;
    private String signalStrength;
    private String batteryPower;
    private String latitude;
    private String longitude;
    private String address;

    public String getPhoneID() {
        return this.phoneID;
    }

    public void setPhoneID(String phoneID) {
        this.phoneID = phoneID;
    }

    public String getRecordTime() {
        return this.recordTime;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }

    public String getSignalStrength() {
        return this.signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getBatteryPower() {
        return this.batteryPower;
    }

    public void setBatteryPower(String batteryPower) {
        this.batteryPower = batteryPower;
    }

    public String getAvailRAM() {
        return this.availRAM;
    }

    public void setAvailRAM(String availRAM) {
        this.availRAM = availRAM;
    }

    public String getTotalRAM() {
        return this.totalRAM;
    }

    public void setTotalRAM(String totalRAM) {
        this.totalRAM = totalRAM;
    }

    public String getAvailROM() {
        return this.availROM;
    }

    public void setAvailROM(String availROM) {
        this.availROM = availROM;
    }

    public String getTotalROM() {
        return this.totalROM;
    }

    public void setTotalROM(String totalROM) {
        this.totalROM = totalROM;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
