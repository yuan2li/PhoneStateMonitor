-- 数据库初始化脚本

-- 创建数据库
CREATE DATABASE phonestate;

-- 使用数据库
USE phonestate;

-- 创建手机状态表
CREATE TABLE phonestate (
 `phoneID` varchar(255) NOT NULL COMMENT '手机ID',
 `recordTime` varchar(255) NOT NULL COMMENT '记录时间',
 `availRAM` varchar(255) NOT NULL COMMENT '可用RAM',
 `totalRAM` varchar(255) NOT NULL COMMENT '总RAM',
 `availROM` varchar(255) NOT NULL COMMENT '可用ROM',
 `totalROM` varchar(255) NOT NULL COMMENT '总ROM',
 `signalStrength` varchar(255) NOT NULL COMMENT '信号强度',
 `batteryPower` varchar(255) NOT NULL COMMENT '电池电量',
 `latitude` varchar(255) NOT NULL COMMENT '纬度',
 `longitude` varchar(255) NOT NULL COMMENT '经度',
 `address` varchar(255) NOT NULL COMMENT '地址',
 primary key (phoneID, recordTime)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='手机状态表';