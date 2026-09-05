package com.management.io.domain.vo.group;

import java.util.List;

public class ApiResponse {
    private int code;
    private String message;
    private List<DeviceData> data;
    
    // Getters and Setters
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<DeviceData> getData() {
        return data;
    }
    
    public void setData(List<DeviceData> data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
               "code=" + code +
               ", message='" + message + '\'' +
               ", data=" + data +
               '}';
    }
}