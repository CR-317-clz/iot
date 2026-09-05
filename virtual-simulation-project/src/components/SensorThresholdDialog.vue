<template>
    <el-dialog :title="dialogTitle" :visible.sync="visible" :width="'400px'" :close-on-click-modal="false"
        :close-on-press-escape="false" @close="handleCancel">
        <el-form :model="thresholdConfig" label-width="100px" v-loading="connecting">
            <el-form-item label="传感器名称" :error="nameError">
                <el-input v-model="thresholdConfig.name" @blur="validateName" placeholder="请输入传感器名称" />
            </el-form-item>

            <!-- 开关型传感器 -->
            <div v-if="isSwitchSensor">
                <el-form-item label="传感器状态">
                    <el-radio-group v-model="thresholdConfig.status">
                        <el-radio :label="0">关闭</el-radio>
                        <el-radio :label="1">开启</el-radio>
                    </el-radio-group>
                </el-form-item>
            </div>

            <!-- 数值型传感器 -->
            <div v-else-if="isNumericSensor">
                <el-form-item label="传感器数值" :error="valueError">
                    <el-input v-model="thresholdConfig.value" @blur="validateValue" placeholder="请输入传感器数值" />
                </el-form-item>
                <el-form-item>
                    <div class="range-hint">{{ sensorRangeHint }}</div>
                </el-form-item>
            </div>

            <!-- 温湿度传感器 -->
            <div v-else-if="isTemperatureHumiditySensor">
                <div class="temp-humidity-form">
                    <div class="form-row">
                        <el-form-item label="温度" :error="temperatureError" class="temp-item">
                            <div class="input-with-unit">
                                <el-input v-model="thresholdConfig.temperature" @blur="validateTemperature"
                                    placeholder="请输入温度" />
                                <span class="unit">°C</span>
                            </div>
                        </el-form-item>
                    </div>
                    <div class="form-row">
                        <el-form-item label="湿度" :error="humidityError" class="humidity-item">
                            <div class="input-with-unit">
                                <el-input v-model="thresholdConfig.humidity" @blur="validateHumidity"
                                    placeholder="请输入湿度" />
                                <span class="unit">%</span>
                            </div>
                        </el-form-item>
                    </div>
                </div>
                <el-form-item>
                    <div class="range-hint">温度范围: -40~80°C | 湿度范围: 0~100%</div>
                </el-form-item>
            </div>
        </el-form>

        <div slot="footer" class="dialog-footer">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleConfirm" :loading="connecting">确认</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
    name: "SensorThresholdDialog",
    props: {
        visible: {
            type: Boolean,
            default: false,
        },
        sensorType: {
            type: String,
            default: "",
        },
        sensorName: {
            type: String,
            default: "",
        },
        sensorId: {
            type: String,
            default: "",
        },
        connecting: {
            type: Boolean,
            default: false,
        },
        existingThreshold: {
            type: Object,
            default: () => ({}),
        },
    },
    data() {
        return {
            thresholdConfig: {
                name: "", // 传感器名称
                status: 0, // 开关状态
                value: 0, // 数值型传感器值
                temperature: 25, // 温度值
                humidity: 50, // 湿度值
                configured: false,
            },
            nameError: "",
            valueError: "",
            temperatureError: "",
            humidityError: "",
        };
    },
    computed: {
        dialogTitle() {
            return `配置 ${this.thresholdConfig.name || this.sensorName}`;
        },
        // 判断是否为开关型传感器
        isSwitchSensor() {
            const switchSensors = ["人体红外传感器", "火焰传感器"];
            return switchSensors.some(
                (sensor) =>
                    this.sensorName.includes(sensor) ||
                    (this.thresholdConfig.name &&
                        this.thresholdConfig.name.includes(sensor)),
            );
        },
        // 判断是否为数值型传感器
        isNumericSensor() {
            const numericSensors = ["可燃气体传感器", "光照度传感器", "PM2.5传感器"];
            return numericSensors.some(
                (sensor) =>
                    this.sensorName.includes(sensor) ||
                    (this.thresholdConfig.name &&
                        this.thresholdConfig.name.includes(sensor)),
            );
        },
        // 判断是否为温湿度传感器
        isTemperatureHumiditySensor() {
            return (
                this.sensorName.includes("湿度传感器") ||
                (this.thresholdConfig.name &&
                    this.thresholdConfig.name.includes("湿度传感器"))
            );
        },
        // 数值型传感器最小值
        sensorMinValue() {
            const sensorName = this.thresholdConfig.name || this.sensorName;
            if (sensorName.includes("可燃气体传感器")) {
                return 0;
            } else if (sensorName.includes("光照度传感器")) {
                return 0;
            } else if (sensorName.includes("PM2.5传感器")) {
                return 0;
            }
            return 0;
        },
        // 数值型传感器最大值
        sensorMaxValue() {
            const sensorName = this.thresholdConfig.name || this.sensorName;
            if (sensorName.includes("可燃气体传感器")) {
                return 100;
            } else if (sensorName.includes("光照度传感器")) {
                return 65535;
            } else if (sensorName.includes("PM2.5传感器")) {
                return 1000;
            }
            return 100;
        },
        // 数值型传感器范围提示
        sensorRangeHint() {
            const sensorName = this.thresholdConfig.name || this.sensorName;
            if (sensorName.includes("可燃气体传感器")) {
                return "范围: 0-100%";
            } else if (sensorName.includes("光照度传感器")) {
                return "范围: 0-65535 lux";
            } else if (sensorName.includes("PM2.5传感器")) {
                return "范围: 0-1000";
            }
            return "";
        },
    },
    watch: {
        visible: {
            handler(newVal) {
                if (newVal) {
                    this.initConfig();
                }
            },
            immediate: true,
        },
    },
    methods: {
        // 初始化配置
        initConfig() {
            // 重置错误信息
            this.nameError = "";
            this.valueError = "";
            this.temperatureError = "";
            this.humidityError = "";

            // 如果有现有配置，则使用现有配置
            if (this.existingThreshold) {
                // 深度复制现有配置，确保完全独立
                this.thresholdConfig = this.deepCloneConfig(this.existingThreshold);
            } else {
                // 重置为默认值
                this.thresholdConfig = {
                    name: this.sensorName,
                    status: 0,
                    value: 0,
                    temperature: 25,
                    humidity: 50,
                    configured: false,
                };
            }
        },
        // 深度复制配置对象，确保完全独立
        deepCloneConfig(config) {
            return JSON.parse(
                JSON.stringify({
                    name: this.sensorName,
                    status: 0,
                    value: 0,
                    temperature: 25,
                    humidity: 50,
                    configured: false,
                    ...config,
                }),
            );
        },
        // 验证传感器名称
        validateName() {
            this.nameError = "";
            if (!this.thresholdConfig.name.trim()) {
                this.nameError = "传感器名称不能为空";
            }
        },
        // 验证数值型传感器值
        validateValue() {
            this.valueError = "";
            const value = parseFloat(this.thresholdConfig.value);
            if (isNaN(value)) {
                this.valueError = "请输入有效的数值";
            } else if (
                value < this.sensorMinValue ||
                value > this.sensorMaxValue
            ) {
                this.valueError = `数值必须在 ${this.sensorMinValue} 到 ${this.sensorMaxValue} 之间`;
            }
        },
        // 验证温度值
        validateTemperature() {
            this.temperatureError = "";
            const temperature = parseFloat(this.thresholdConfig.temperature);
            if (isNaN(temperature)) {
                this.temperatureError = "请输入有效的温度值";
            } else if (
                temperature < -40 ||
                temperature > 80
            ) {
                this.temperatureError = "温度必须在 -40 到 80 °C 之间";
            }
        },
        // 验证湿度值
        validateHumidity() {
            this.humidityError = "";
            const humidity = parseFloat(this.thresholdConfig.humidity);
            if (isNaN(humidity)) {
                this.humidityError = "请输入有效的湿度值";
            } else if (
                humidity < 0 ||
                humidity > 100
            ) {
                this.humidityError = "湿度必须在 0 到 100% 之间";
            }
        },
        // 处理确认
        handleConfirm() {
            // 验证传感器名称
            this.validateName();
            if (this.nameError) {
                return;
            }

            // 验证输入
            if (this.isNumericSensor) {
                this.validateValue();
                if (this.valueError) {
                    return;
                }
            } else if (this.isTemperatureHumiditySensor) {
                this.validateTemperature();
                this.validateHumidity();
                if (this.temperatureError || this.humidityError) {
                    return;
                }
            }

            // 标记为已配置
            this.thresholdConfig.configured = true;

            // 发送确认事件
            this.$emit("confirm", {
                sensorId: this.sensorId,
                sensorName: this.thresholdConfig.name,
                thresholdConfig: this.thresholdConfig,
            });
        },
        // 处理取消
        handleCancel() {
            this.$emit("update:visible", false);
            this.$emit("cancel");
        },
    },
};
</script>

<style scoped>
.dialog-footer {
    display: flex;
    justify-content: flex-end;
}

.range-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}

.unit {
    margin-left: 10px;
    color: #909399;
}

.temp-humidity-form {
    width: 100%;
}

.form-row {
    margin-bottom: 15px;
    display: flex;
    align-items: center;
}

.input-with-unit {
    display: flex;
    align-items: center;
    width: 100%;
}

.input-with-unit .el-input {
    flex: 1;
}

.temp-item,
.humidity-item {
    width: 100%;
    margin-bottom: 0;
}
</style>
