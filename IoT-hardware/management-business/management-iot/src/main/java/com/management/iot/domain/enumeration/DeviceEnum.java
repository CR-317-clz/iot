package com.management.iot.domain.enumeration;

public enum DeviceEnum {

    FAN(1, 49, "一号位"),           // ASCII 49 = '1'
    WATER_CURTAIN(2, 50, "二号位"), // ASCII 50 = '2'
    SPRINKLER(3, 51, "三号位"),     // ASCII 51 = '3'
    LIGHT(4, 52, "四号位");          // ASCII 52 = '4'

    private final int index;
    private final int asciiValue;
    private final String name;

    DeviceEnum(int index, int asciiValue, String name) {
        this.index = index;
        this.asciiValue = asciiValue;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public int getAsciiValue() {
        return asciiValue;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据ASCII值获取设备类型
     */
    public static DeviceEnum getByAsciiValue(int asciiValue) {
        for (DeviceEnum device : values()) {
            if (device.getAsciiValue() == asciiValue) {
                return device;
            }
        }
        throw new IllegalArgumentException("无效的设备ASCII值: " + asciiValue);
    }

    /**
     * 根据索引获取设备类型
     */
    public static DeviceEnum getByIndex(int index) {
        for (DeviceEnum device : values()) {
            if (device.getIndex() == index) {
                return device;
            }
        }
        throw new IllegalArgumentException("无效的设备索引: " + index);
    }

}
