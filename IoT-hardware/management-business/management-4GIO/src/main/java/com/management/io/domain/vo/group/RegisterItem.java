package com.management.io.domain.vo.group;

public class RegisterItem {
    private int registerId;
    private String data;
    private double value;
    private int alarmLevel;
    private String alarmColor;
    private String alarmInfo;
    private String unit;
    private String registerName;
    
    // Getters and Setters
    public int getRegisterId() {
        return registerId;
    }
    
    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    public int getAlarmLevel() {
        return alarmLevel;
    }
    
    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
    
    public String getAlarmColor() {
        return alarmColor;
    }
    
    public void setAlarmColor(String alarmColor) {
        this.alarmColor = alarmColor;
    }
    
    public String getAlarmInfo() {
        return alarmInfo;
    }
    
    public void setAlarmInfo(String alarmInfo) {
        this.alarmInfo = alarmInfo;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getRegisterName() {
        return registerName;
    }
    
    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }
    
    @Override
    public String toString() {
        return "RegisterItem{" +
               "registerId=" + registerId +
               ", data='" + data + '\'' +
               ", value=" + value +
               ", alarmLevel=" + alarmLevel +
               ", alarmColor='" + alarmColor + '\'' +
               ", alarmInfo='" + alarmInfo + '\'' +
               ", unit='" + unit + '\'' +
               ", registerName='" + registerName + '\'' +
               '}';
    }
}