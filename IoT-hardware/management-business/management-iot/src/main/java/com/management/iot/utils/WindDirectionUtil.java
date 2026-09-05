package com.management.iot.utils;

public class WindDirectionUtil {
    
    private static final String[] DIRECTION_NAMES = {
        "北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"
    };
    
    /**
     * 将风向角度转换为中文方位名称
     * @param angle 0-360度（0°=北风）
     * @return 中文方位，如"东北"
     */
    public static String angleToDirectionName(double angle) {
        // 归一化到 0-360
        angle = angle % 360;
        if (angle < 0) angle += 360;
        
        // 加22.5度偏移，整除45度，再对8取模
        int index = (int) ((angle + 22.5) / 45) % 8;
        return DIRECTION_NAMES[index];
    }
    
    /**
     * 将风向角度转换为方位索引（0-7）
     */
    public static int angleToDirectionIndex(double angle) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        return (int) ((angle + 22.5) / 45) % 8;
    }
}