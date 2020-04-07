package com.ly;

/**
 * 
* @name PhoneState
* @description 信息实体类，存储需要采集的各项手机数据
 */
public class PhoneState {
    private String phoneID;         //手机标识
    private String recordTime;      //采集时间
    private String availRAM;        //可用运行内存
    private String totalRAM;        //总运行内存
    private String availROM;        //可用存储容量
    private String totalROM;        //总存储容量
    private String signalStrength;  //信号强度
    private String batteryPower;    //电池电量
    private String latitude;        //纬度
    private String longitude;       //经度
    private String address;         //地址信息
    
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
