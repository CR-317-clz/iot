package com.management.sim.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 传感器阈值校验工具类 - 最终版
 */
public class SensorThresholdValidator {
    
    // 更宽松的正则表达式，支持更多格式
    private static final Pattern RANGE_PATTERN = Pattern.compile(
        "^\\s*(-?\\d+(\\.\\d+)?)\\s*([-~到]|到)\\s*(-?\\d+(\\.\\d+)?)\\s*$"
    );
    
    // 数字范围正则表达式（用于判断是否是数字区间）
    private static final Pattern NUMBER_RANGE_PATTERN = Pattern.compile(
        "^\\s*-?\\d+(\\.\\d+)?\\s*[-~到]\\s*-?\\d+(\\.\\d+)?\\s*$"
    );
    
    // 状态值正则表达式（用于判断是否是开关量）
    private static final Pattern STATE_VALUE_PATTERN = Pattern.compile(
        "^(ON|OFF|开|关|0|1|true|false)$",
        Pattern.CASE_INSENSITIVE
    );
    
    // 状态区间正则表达式（如 "ON-OFF"）
    private static final Pattern STATE_RANGE_PATTERN = Pattern.compile(
        "^\\s*(ON|OFF|开|关|0|1|true|false)\\s*[-~到]\\s*(ON|OFF|开|关|0|1|true|false)\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    
    // 支持更多传感器（预定义的传感器类型）
    private static final Map<String, SensorRange> PREDEFINED_SENSORS = new HashMap<>();
    
    // 动态传感器配置缓存（可以根据需要从数据库加载）
    private static final Map<String, SensorRange> DYNAMIC_SENSORS = new HashMap<>();
    
    static {
        // ========== 模拟量传感器（连续值范围） ==========
        PREDEFINED_SENSORS.put("温度", new SensorRange(-40.0, 80.0, "温度阈值应在-40℃到80℃之间", "℃", SensorType.ANALOG));
        PREDEFINED_SENSORS.put("湿度", new SensorRange(0.0, 100.0, "湿度阈值应在0%到100% RH之间", "% RH", SensorType.ANALOG));
        PREDEFINED_SENSORS.put("压力", new SensorRange(0.0, 10.0, "压力阈值应在0到10MPa之间", "MPa", SensorType.ANALOG));
        PREDEFINED_SENSORS.put("流量", new SensorRange(0.0, 100.0, "流量阈值应在0到100m³/h之间", "m³/h", SensorType.ANALOG));
        PREDEFINED_SENSORS.put("液位", new SensorRange(0.0, 5.0, "液位阈值应在0到5m之间", "m", SensorType.ANALOG));
        
        // ========== 开关量传感器（继电器）- 离散值 ==========
        PREDEFINED_SENSORS.put("继电器", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器1", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器2", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器3", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器4", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器5", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器6", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器7", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("继电器8", new SensorRange(0.0, 1.0, "继电器阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        
        // 通用开关量传感器
        PREDEFINED_SENSORS.put("开关", new SensorRange(0.0, 1.0, "开关阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("报警", new SensorRange(0.0, 1.0, "报警阈值应为状态值：ON/OFF、开/关、0/1", "", SensorType.DIGITAL));
        
        // 执行器类设备（如窗帘）
        PREDEFINED_SENSORS.put("窗帘", new SensorRange(0.0, 1.0, "窗帘阈值应为状态值：0-1 或 ON/OFF", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("窗帘执行器", new SensorRange(0.0, 1.0, "窗帘执行器阈值应为状态值：0-1 或 ON/OFF", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("卷帘", new SensorRange(0.0, 1.0, "卷帘阈值应为状态值：0-1 或 ON/OFF", "", SensorType.DIGITAL));
        PREDEFINED_SENSORS.put("幕布", new SensorRange(0.0, 1.0, "幕布阈值应为状态值：0-1 或 ON/OFF", "", SensorType.DIGITAL));
    }
    
    /**
     * 传感器类型枚举
     */
    public enum SensorType {
        ANALOG,  // 模拟量传感器（连续值）
        DIGITAL  // 数字量传感器（离散值，如继电器、开关）
    }
    
    /**
     * 传感器范围类
     */
    public static class SensorRange {
        private final double minValue;
        private final double maxValue;
        private final String errorTemplate;
        private final String unit;
        private final SensorType type;
        
        public SensorRange(double minValue, double maxValue, String errorTemplate, String unit, SensorType type) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.errorTemplate = errorTemplate;
            this.unit = unit;
            this.type = type;
        }
        
        public double getMinValue() { return minValue; }
        public double getMaxValue() { return maxValue; }
        public String getErrorTemplate() { return errorTemplate; }
        public String getUnit() { return unit; }
        public SensorType getType() { return type; }
    }
    
    /**
     * 动态添加传感器配置
     * @param sensorName 传感器名称
     * @param minValue 最小值
     * @param maxValue 最大值
     * @param unit 单位
     * @param type 传感器类型
     */
    public static void addDynamicSensor(String sensorName, double minValue, double maxValue, String unit, SensorType type) {
        String errorTemplate;
        if (type == SensorType.ANALOG) {
            errorTemplate = String.format("%s阈值应在%.2f%s到%.2f%s之间", sensorName, minValue, unit, maxValue, unit);
        } else {
            errorTemplate = String.format("%s阈值应为状态值：ON/OFF、开/关、0/1", sensorName);
        }
        
        SensorRange range = new SensorRange(minValue, maxValue, errorTemplate, unit, type);
        DYNAMIC_SENSORS.put(sensorName, range);
    }
    
    /**
     * 从数据库加载传感器配置（示例方法）
     * 实际使用时，应该从数据库查询
     */
    public static void loadSensorsFromDatabase() {
        // 这里应该是从数据库查询的代码
        // 例如：
        // List<SensorConfig> sensors = sensorConfigMapper.selectAll();
        // for (SensorConfig config : sensors) {
        //     addDynamicSensor(config.getName(), config.getMinValue(), 
        //                      config.getMaxValue(), config.getUnit(), 
        //                      SensorType.valueOf(config.getType()));
        // }
        
        // 示例：动态添加窗帘执行器
        addDynamicSensor("窗帘", 0.0, 1.0, "", SensorType.DIGITAL);
        addDynamicSensor("窗帘执行器", 0.0, 1.0, "", SensorType.DIGITAL);
        addDynamicSensor("卷帘", 0.0, 100.0, "%", SensorType.ANALOG); // 卷帘可以设置开度百分比
    }
    
    /**
     * 智能判断传感器类型（基于阈值格式）
     * @param thresholdValue 阈值字符串
     * @return 推测的传感器类型
     */
    private static SensorType guessSensorType(String thresholdValue) {
        String cleanedValue = thresholdValue.trim();
        
        // 检查是否是状态值（如 ON、OFF、0、1）
        if (STATE_VALUE_PATTERN.matcher(cleanedValue).matches()) {
            return SensorType.DIGITAL;
        }
        
        // 检查是否是状态区间（如 ON-OFF）
        if (STATE_RANGE_PATTERN.matcher(cleanedValue).matches()) {
            return SensorType.DIGITAL;
        }
        
        // 检查是否是数字区间
        if (NUMBER_RANGE_PATTERN.matcher(cleanedValue).matches()) {
            // 尝试解析数字，判断是否为0和1的特殊情况
            String[] parts = cleanedValue.split("[-~到]");
            try {
                double num1 = Double.parseDouble(parts[0].trim());
                double num2 = Double.parseDouble(parts[1].trim());
                
                // 如果两个数字都是0或1，则可能是数字量
                if ((num1 == 0 || num1 == 1) && (num2 == 0 || num2 == 1)) {
                    return SensorType.DIGITAL;
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
            return SensorType.ANALOG;
        }
        
        // 默认返回模拟量
        return SensorType.ANALOG;
    }
    
    /**
     * 获取传感器配置
     * 优先从预定义配置获取，然后从动态配置获取，最后根据阈值格式推断
     */
    private static SensorRange getSensorRange(String sensorName, String thresholdValue) {
        // 1. 先从预定义传感器中查找
        SensorRange range = PREDEFINED_SENSORS.get(sensorName);
        if (range != null) {
            return range;
        }
        
        // 2. 从动态传感器中查找
        range = DYNAMIC_SENSORS.get(sensorName);
        if (range != null) {
            return range;
        }
        
        // 3. 根据阈值格式推断类型
        SensorType guessedType = guessSensorType(thresholdValue);
        
        // 4. 创建动态的传感器范围
        String unit = "";
        String errorTemplate;
        double minValue, maxValue;
        
        if (guessedType == SensorType.DIGITAL) {
            minValue = 0.0;
            maxValue = 1.0;
            errorTemplate = String.format("%s阈值应为状态值：ON/OFF、开/关、0/1 或区间如 '0-1'", sensorName);
        } else {
            // 尝试从阈值中提取范围来设置合理的默认值
            try {
                String cleanedValue = thresholdValue.trim();
                if (NUMBER_RANGE_PATTERN.matcher(cleanedValue).matches()) {
                    String[] parts = cleanedValue.split("[-~到]");
                    minValue = Double.parseDouble(parts[0].trim());
                    maxValue = Double.parseDouble(parts[1].trim());
                    // 稍微扩大范围以允许一定的误差
                    minValue = Math.min(minValue, minValue - 10);
                    maxValue = Math.max(maxValue, maxValue + 10);
                } else {
                    // 默认范围
                    minValue = -100.0;
                    maxValue = 100.0;
                }
            } catch (Exception e) {
                minValue = -100.0;
                maxValue = 100.0;
            }
            errorTemplate = String.format("%s阈值应在%.2f到%.2f之间", sensorName, minValue, maxValue);
            unit = "";
        }
        
        return new SensorRange(minValue, maxValue, errorTemplate, unit, guessedType);
    }
    
    /**
     * 增强的校验方法，返回详细结果
     * @param sensorName 传感器名称
     * @param thresholdValue 阈值字符串
     * @return ValidationResult 包含校验状态和详细信息
     */
    public static ValidationResult validateThresholdDetailed(String sensorName, String thresholdValue) {
        ValidationResult result = new ValidationResult();
        result.setSensorName(sensorName);
        result.setThresholdValue(thresholdValue);
        
        // 1. 基础校验
        if (sensorName == null || sensorName.trim().isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("传感器名称不能为空");
            return result;
        }
        
        if (thresholdValue == null) {
            result.setValid(false);
            result.setErrorMessage("阈值不能为null");
            return result;
        }
        
        String cleanedValue = thresholdValue.trim();
        if (cleanedValue.isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("阈值不能为空");
            return result;
        }
        
        // 2. 获取传感器配置（动态判断）
        SensorRange range = getSensorRange(sensorName, cleanedValue);
        result.setSensorRange(range);
        
        // 3. 根据传感器类型进行不同的校验
        if (range.type == SensorType.DIGITAL) {
            return validateDigitalThreshold(cleanedValue, result, range);
        } else {
            return validateAnalogThreshold(cleanedValue, result, range);
        }
    }
    
    /**
     * 校验数字量传感器阈值
     */
    private static ValidationResult validateDigitalThreshold(String cleanedValue, ValidationResult result, SensorRange range) {
        // 检查是否是有效的状态值
        String upperValue = cleanedValue.toUpperCase();
        
        // 单状态值校验
        if (isValidDigitalState(upperValue)) {
            result.setValid(true);
            result.setDigitalValue(cleanedValue);
            result.setSuccessMessage(String.format("%s状态值校验通过", result.getSensorName()));
            return result;
        }
        
        // 区间状态值校验
        String[] delimiters = {"-", "~", "到", "—", "–"};
        for (String delim : delimiters) {
            if (cleanedValue.contains(delim)) {
                String[] parts = cleanedValue.split(Pattern.quote(delim));
                if (parts.length == 2) {
                    String state1 = parts[0].trim().toUpperCase();
                    String state2 = parts[1].trim().toUpperCase();
                    
                    if (isValidDigitalState(state1) && isValidDigitalState(state2)) {
                        result.setValid(true);
                        result.setDigitalValue(cleanedValue);
                        result.setDelimiter(delim);
                        result.setSuccessMessage(String.format("%s状态区间校验通过", result.getSensorName()));
                        return result;
                    }
                    
                    // 特殊处理 0-1 的情况
                    if (isZeroOrOne(state1) && isZeroOrOne(state2)) {
                        result.setValid(true);
                        result.setDigitalValue(cleanedValue);
                        result.setDelimiter(delim);
                        result.setSuccessMessage(String.format("%s状态区间校验通过", result.getSensorName()));
                        return result;
                    }
                }
            }
        }
        
        // 检查是否是数字区间，但实际上是开关量（如 0-1）
        if (NUMBER_RANGE_PATTERN.matcher(cleanedValue).matches()) {
            String[] parts = cleanedValue.split("[-~到]");
            try {
                double num1 = Double.parseDouble(parts[0].trim());
                double num2 = Double.parseDouble(parts[1].trim());
                
                // 如果是 0-1 的范围，当作开关量处理
                if ((num1 == 0 || num1 == 1) && (num2 == 0 || num2 == 1)) {
                    result.setValid(true);
                    result.setDigitalValue(cleanedValue);
                    result.setSuccessMessage(String.format("%s状态区间校验通过", result.getSensorName()));
                    return result;
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 如果都不匹配，返回错误
        result.setValid(false);
        result.setErrorMessage(String.format("%s阈值格式错误，应为状态值：ON/OFF、开/关、0/1 或区间如 '0-1'、'ON-OFF'", 
                result.getSensorName()));
        return result;
    }
    
    /**
     * 判断是否为有效的数字状态（0或1）
     */
    private static boolean isZeroOrOne(String value) {
        try {
            double num = Double.parseDouble(value);
            return num == 0 || num == 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 判断是否为有效的数字状态值
     */
    private static boolean isValidDigitalState(String state) {
        return state.matches("^(ON|OFF|开|关|0|1|TRUE|FALSE)$");
    }
    
    /**
     * 校验模拟量传感器阈值
     */
    private static ValidationResult validateAnalogThreshold(String cleanedValue, ValidationResult result, SensorRange range) {
        // 格式校验
        if (!RANGE_PATTERN.matcher(cleanedValue).matches()) {
            // 尝试多种分隔符
            String[] parts = null;
            String delimiter = null;
            
            String[] delimiters = {"-", "~", "到", "—", "–"};
            for (String delim : delimiters) {
                if (cleanedValue.contains(delim)) {
                    parts = cleanedValue.split(Pattern.quote(delim));
                    delimiter = delim;
                    break;
                }
            }
            
            if (parts == null || parts.length != 2) {
                result.setValid(false);
                result.setErrorMessage(String.format("阈值格式错误，应为'最小值-最大值'格式，例如：'-40-80'，当前值：'%s'", cleanedValue));
                return result;
            }
            
            result.setDelimiter(delimiter);
            
            // 提取数字
            String minStr = parts[0].trim();
            String maxStr = parts[1].trim();
            
            try {
                double minValue = Double.parseDouble(minStr);
                double maxValue = Double.parseDouble(maxStr);
                result.setMinValue(minValue);
                result.setMaxValue(maxValue);
                
                // 检查最小值和最大值关系
                if (minValue > maxValue) {
                    result.setValid(false);
                    result.setErrorMessage("阈值最小值不能大于最大值");
                    return result;
                }
                
                // 检查范围
                if (minValue < range.minValue) {
                    result.setValid(false);
                    result.setErrorMessage(String.format("最小值%.2f%s低于允许的最小值%.2f%s", 
                            minValue, range.unit, range.minValue, range.unit));
                    return result;
                }
                
                if (maxValue > range.maxValue) {
                    result.setValid(false);
                    result.setErrorMessage(String.format("最大值%.2f%s高于允许的最大值%.2f%s", 
                            maxValue, range.unit, range.maxValue, range.unit));
                    return result;
                }
                
                result.setValid(true);
                result.setSuccessMessage("阈值校验通过");
                return result;
                
            } catch (NumberFormatException e) {
                result.setValid(false);
                result.setErrorMessage(String.format("阈值必须为有效的数字格式，当前值：'%s'", cleanedValue));
                return result;
            }
        }
        
        // 使用正则匹配的情况
        java.util.regex.Matcher matcher = RANGE_PATTERN.matcher(cleanedValue);
        if (matcher.matches()) {
            try {
                double minValue = Double.parseDouble(matcher.group(1));
                double maxValue = Double.parseDouble(matcher.group(4));
                result.setMinValue(minValue);
                result.setMaxValue(maxValue);
                result.setDelimiter(matcher.group(3));
                
                // 检查逻辑
                if (minValue > maxValue) {
                    result.setValid(false);
                    result.setErrorMessage("阈值最小值不能大于最大值");
                    return result;
                }
                
                // 检查范围
                if (minValue < range.minValue) {
                    result.setValid(false);
                    result.setErrorMessage(String.format("最小值%.2f%s低于允许的最小值%.2f%s", 
                            minValue, range.unit, range.minValue, range.unit));
                    return result;
                }
                
                if (maxValue > range.maxValue) {
                    result.setValid(false);
                    result.setErrorMessage(String.format("最大值%.2f%s高于允许的最大值%.2f%s", 
                            maxValue, range.unit, range.maxValue, range.unit));
                    return result;
                }
                
                result.setValid(true);
                result.setSuccessMessage("阈值校验通过");
                return result;
                
            } catch (NumberFormatException e) {
                result.setValid(false);
                result.setErrorMessage(String.format("阈值必须为有效的数字格式，当前值：'%s'", cleanedValue));
                return result;
            }
        }
        
        result.setValid(false);
        result.setErrorMessage("阈值格式无法识别");
        return result;
    }
    
    /**
     * 原来的校验方法（兼容）
     */
    public static String validateThreshold(String sensorName, String thresholdValue) {
        ValidationResult result = validateThresholdDetailed(sensorName, thresholdValue);
        if (result.isValid()) {
            return null;
        } else {
            return result.getErrorMessage();
        }
    }
    
    /**
     * 校验结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String sensorName;
        private String thresholdValue;
        private Double minValue;
        private Double maxValue;
        private String digitalValue;
        private String delimiter;
        private SensorRange sensorRange;
        private String errorMessage;
        private String successMessage;
        
        // getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getSensorName() { return sensorName; }
        public void setSensorName(String sensorName) { this.sensorName = sensorName; }
        
        public String getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(String thresholdValue) { this.thresholdValue = thresholdValue; }
        
        public Double getMinValue() { return minValue; }
        public void setMinValue(Double minValue) { this.minValue = minValue; }
        
        public Double getMaxValue() { return maxValue; }
        public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }
        
        public String getDigitalValue() { return digitalValue; }
        public void setDigitalValue(String digitalValue) { this.digitalValue = digitalValue; }
        
        public String getDelimiter() { return delimiter; }
        public void setDelimiter(String delimiter) { this.delimiter = delimiter; }
        
        public SensorRange getSensorRange() { return sensorRange; }
        public void setSensorRange(SensorRange sensorRange) { this.sensorRange = sensorRange; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getSuccessMessage() { return successMessage; }
        public void setSuccessMessage(String successMessage) { this.successMessage = successMessage; }
        
        @Override
        public String toString() {
            if (valid) {
                if (sensorRange != null && sensorRange.type == SensorType.DIGITAL) {
                    return String.format("✓ 校验通过 - %s，状态值：%s",
                            sensorName, digitalValue != null ? digitalValue : thresholdValue);
                } else {
                    return String.format("✓ 校验通过 - %s，阈值：%s，范围：[%.2f%s - %.2f%s]",
                            sensorName, thresholdValue, minValue, 
                            sensorRange != null ? sensorRange.unit : "", 
                            maxValue, sensorRange != null ? sensorRange.unit : "");
                }
            } else {
                return String.format("✗ 校验失败 - %s，阈值：%s，错误：%s",
                        sensorName, thresholdValue, errorMessage);
            }
        }
    }
    
    /**
     * 快速测试方法
     */
    public static void testThresholdValidation() {
        System.out.println("=== 阈值校验测试（动态判断）===");
        
        // 测试用例 - 包括窗帘执行器
        String[][] testCases = {
            // 预定义传感器测试
            {"温度", "-40-80", "通过"},
            {"温度", "20-30", "通过"},
            
            // 窗帘执行器测试（动态判断）
            {"窗帘执行器", "0-1", "通过"},
            {"窗帘执行器", "ON", "通过"},
            {"窗帘执行器", "OFF", "通过"},
            {"窗帘执行器", "开", "通过"},
            {"窗帘执行器", "关", "通过"},
            {"窗帘执行器", "ON-OFF", "通过"},
            {"窗帘执行器", "开-关", "通过"},
            {"窗帘执行器", "0", "通过"},
            {"窗帘执行器", "1", "通过"},
            {"窗帘执行器", "2-3", "失败（应该是开关量）"},
            
            // 动态传感器测试
            {"卷帘", "0-100", "通过"},
            {"卷帘", "20-80", "通过"},
            {"卷帘", "ON-OFF", "失败（应该是模拟量）"},
            
            // 未知传感器 - 根据阈值格式推断
            {"未知设备1", "0-1", "通过"},  // 推断为数字量
            {"未知设备2", "20-30", "通过"}, // 推断为模拟量
            {"未知设备3", "ON", "通过"},    // 推断为数字量
            {"未知设备4", "abc", "失败"},   // 无效格式
        };
        
        for (String[] testCase : testCases) {
            String sensor = testCase[0];
            String threshold = testCase[1];
            String expected = testCase[2];
            
            ValidationResult result = validateThresholdDetailed(sensor, threshold);
            String actual = result.isValid() ? "通过" : "失败";
            
            System.out.printf("传感器：%-12s 阈值：%-10s 期望：%-15s 实际：%-5s %s%n",
                    sensor, threshold, expected, actual,
                    actual.equals(expected.split("（")[0]) ? "✓" : "✗");
            
            if (!result.isValid()) {
                System.out.println("   错误信息：" + result.getErrorMessage());
            }
        }
    }
    
    /**
     * 在Controller中使用的方法
     */
    public static boolean isThresholdValid(String sensorName, String thresholdValue) {
        ValidationResult result = validateThresholdDetailed(sensorName, thresholdValue);
        return result.isValid();
    }
    
    /**
     * 获取校验的详细信息
     */
    public static String getValidationInfo(String sensorName, String thresholdValue) {
        ValidationResult result = validateThresholdDetailed(sensorName, thresholdValue);
        return result.toString();
    }

}