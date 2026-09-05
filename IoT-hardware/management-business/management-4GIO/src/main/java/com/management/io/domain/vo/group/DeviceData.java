package com.management.io.domain.vo.group;

import java.util.List;

public class DeviceData {
    private String systemCode;
    private String deviceAddr;
    private String deviceName;
    private double lat;
    private double lng;
    private String deviceStatus;
    private String relayStatus;
    private List<RelayStatusItem> relayStatusItems;
    private List<DataNode> dataItem;
    private long timeStamp;
    
    // Getters and Setters
    public String getSystemCode() {
        return systemCode;
    }
    
    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }
    
    public String getDeviceAddr() {
        return deviceAddr;
    }
    
    public void setDeviceAddr(String deviceAddr) {
        this.deviceAddr = deviceAddr;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public double getLat() {
        return lat;
    }
    
    public void setLat(double lat) {
        this.lat = lat;
    }
    
    public double getLng() {
        return lng;
    }
    
    public void setLng(double lng) {
        this.lng = lng;
    }
    
    public String getDeviceStatus() {
        return deviceStatus;
    }
    
    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
    
    public String getRelayStatus() {
        return relayStatus;
    }
    
    public void setRelayStatus(String relayStatus) {
        this.relayStatus = relayStatus;
    }
    
    public List<RelayStatusItem> getRelayStatusItems() {
        return relayStatusItems;
    }
    
    public void setRelayStatusItems(List<RelayStatusItem> relayStatusItems) {
        this.relayStatusItems = relayStatusItems;
    }
    
    public List<DataNode> getDataItem() {
        return dataItem;
    }
    
    public void setDataItem(List<DataNode> dataItem) {
        this.dataItem = dataItem;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }
    
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    @Override
    public String toString() {
        return "DeviceData{" +
               "systemCode='" + systemCode + '\'' +
               ", deviceAddr=" + deviceAddr +
               ", deviceName='" + deviceName + '\'' +
               ", lat=" + lat +
               ", lng=" + lng +
               ", deviceStatus='" + deviceStatus + '\'' +
               ", relayStatus='" + relayStatus + '\'' +
               ", relayStatusItems=" + relayStatusItems +
               ", dataItem=" + dataItem +
               ", timeStamp=" + timeStamp +
               '}';
    }
}