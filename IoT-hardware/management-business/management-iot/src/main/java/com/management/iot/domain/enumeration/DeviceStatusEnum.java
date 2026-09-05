package com.management.iot.domain.enumeration;

/**
 * 设备状态枚举
 */
public enum DeviceStatusEnum {
    OFF(102, "关"),
    ON(1, "开"); // 102以外的其他值都视为开

    private final int asciiValue;
    private final String description;

    DeviceStatusEnum(int asciiValue, String description) {
        this.asciiValue = asciiValue;
        this.description = description;
    }

    public int getAsciiValue() {
        return asciiValue;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据ASCII值获取设备状态
     */
    public static DeviceStatusEnum getByAsciiValue(int asciiValue) {
        if (asciiValue == OFF.getAsciiValue()) {
            return OFF;
        }
        return ON;
    }

    /**
     * 检查是否为开启状态
     */
    public static boolean isOn(int asciiValue) {
        return getByAsciiValue(asciiValue) == ON;
    }

    /**
     * 检查是否为关闭状态
     */
    public static boolean isOff(int asciiValue) {
        return getByAsciiValue(asciiValue) == OFF;
    }
}