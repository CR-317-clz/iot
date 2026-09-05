package com.management.web.controller.business.iot;

import com.management.common.core.domain.AjaxResult;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.iot.configuration.mqtt.MqttConnection;
import com.management.iot.configuration.mqtt.MqttManager;
import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.IotCurtainMachineStatus;
import com.management.iot.domain.IotDevice;
import com.management.iot.domain.IotProtocol;
import com.management.iot.domain.IotRelayDeviceStatus;
import com.management.iot.domain.dto.RelayControlDto;
import com.management.iot.service.IotCurtainMachineStatusService;
import com.management.iot.service.IotDeviceService;
import com.management.iot.service.IotProtocolService;
import com.management.iot.service.IotRelayDeviceStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/install")
public class IotMqttController {

    private static final Logger logger = LoggerFactory.getLogger(IotMqttController.class);

    @Autowired
    MqttManager mqttManager;

    @Autowired
    private TcpClientManager tcpClientManager;

    @Autowired
    private IotCurtainMachineStatusService iotCurtainMachineStatusService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotProtocolService iotProtocolService;

    @GetMapping("/sendTcp/{relayProtocolId}")
    public AjaxResult sendTcp(@PathVariable Long relayProtocolId) {
        logger.info("开始发送 TCP 命令到协议: {}", relayProtocolId);

        // 使用新的等待机制，确保第一次请求获取最新数据
        List<IotRelayDeviceStatus> relayDeviceStatuses =
                tcpClientManager.sendMessageAndWaitResponse(relayProtocolId, "qa");

        if (relayDeviceStatuses.isEmpty()) {
            return AjaxResult.error("获取设备状态失败或超时");
        }

        IotDevice device = iotDeviceService.selectDeviceByRelayId(relayProtocolId);
        if (device == null){
            return AjaxResult.error("未找到该设备");
        }
        return AjaxResult.success(relayDeviceStatuses);
    }

    @GetMapping("/test")
    public int test() {
        mqttManager.publish(null,"1", "test", 0, false);
        return 1;
    }

    @GetMapping("/getTcpClientCount/{relayProtocolId}")
    public String getTcpClientCount(@PathVariable Long relayProtocolId) {
        return tcpClientManager.getClientStatus(relayProtocolId);
    }

    /**
     * 实时查看所有 MQTT 最新数据
     * @return key=topic, value=数据内容
     */
    @GetMapping("/latest")
    public Map<String, Map<String, Object>> getLatestMqttData() {
        // 直接从内存缓存中取出当前最新的消息
        return MqttConnection.getLatestMessages();
    }

    /**
     * 可选：查看指定 topic 的最新数据
     */
    @GetMapping("/latest/{topic}")
    public Map<String, Object> getLatestMqttDataByTopic(@PathVariable String topic) {
        Map<String, Map<String, Object>> allData = MqttConnection.getLatestMessages();
        Map<String, Object> message = allData.getOrDefault(topic, Map.of("message", "No data for topic: " + topic));
        return message;
    }

    @PostMapping("/tcp")
    public AjaxResult getTcpClientCount(@RequestBody RelayControlDto dto) {

        AjaxResult result = new AjaxResult();

        try{

            // 手动控制
            if (dto.getAutomation() == 1){

                //判断协议是否存在
                if (dto.getRelayProtocolId() == null && dto.getRelayProtocolId() != 0) {
                    throw new IllegalArgumentException("设备协议不能为空");
                }

                //判断风扇数据是否为null
                if (dto.getBlower() != null && dto.getBlower() == 0){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "b2");
                    logger.info("设置风扇关闭成功，设备协议ID: {}", dto.getRelayProtocolId());
                }
                else if (dto.getBlower() != null && dto.getBlower() == 1){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "a2");
                    logger.info("设置风扇开启成功，设备协议ID: {}", dto.getRelayProtocolId());
                }

                //判断紫外线灯数据是否为null
                if (dto.getUltravioletRadiator() != null && dto.getUltravioletRadiator() == 0){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "b3");
                    logger.info("设置紫外线灯关闭成功，设备协议ID: {}", dto.getRelayProtocolId());
                }
                else if (dto.getUltravioletRadiator() != null && dto.getUltravioletRadiator() == 1){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "a3");
                    logger.info("设置紫外线灯开启成功，设备协议ID: {}", dto.getRelayProtocolId());
                }

                //判断喷淋器数据是否为null
                if (dto.getSpray() != null && dto.getSpray() == 0){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "b1");
                    logger.info("设置喷淋器关闭成功，设备协议ID: {}", dto.getRelayProtocolId());
                }
                else if (dto.getSpray() != null && dto.getSpray() == 1){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "a1");
                    logger.info("设置喷淋器开启成功，设备协议ID: {}", dto.getRelayProtocolId());
                }

                //判断水帘幕数据是否为null
                if (dto.getWaterCurtain() != null && dto.getWaterCurtain() == 0){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "b4");
                    logger.info("设置水帘幕关闭成功，设备协议ID: {}", dto.getRelayProtocolId());
                }
                else if (dto.getWaterCurtain() != null && dto.getWaterCurtain() == 1){
                    tcpClientManager.sendMessage(dto.getRelayProtocolId(), "a4");
                    logger.info("设置水帘幕开启成功，设备协议ID: {}", dto.getRelayProtocolId());
                }

                if (dto.getCurtainMachine()!= null){
                    //使用继电器编号查询到相对应的设备
                    IotDevice iotDevice = iotDeviceService.selectDeviceByRelayId(dto.getRelayProtocolId());
                    if (iotDevice == null) {
                        throw new IllegalArgumentException("设备不存在");
                    }else {
                        IotProtocol protocol = iotProtocolService.getProtocolById(iotDevice.getProtocolId());

                        if (protocol == null){
                            throw new IllegalArgumentException("MQTT设备协议不存在");
                        }
                        IotCurtainMachineStatus machineStatus = new IotCurtainMachineStatus();
                        machineStatus.setCurtainMachineId(SnowflakeIdGenerator.nextId());
                        machineStatus.setCurtainMachineName("窗帘机");
                        //判断窗帘数据是否为null
                        if (dto.getCurtainMachine() != null && dto.getCurtainMachine() == 0){
                            int publish = mqttManager.publish(null, String.valueOf(protocol.getSendTopic()), "0", 0, false);
                            machineStatus.setProtocolId(protocol.getProtocolId());
                            machineStatus.setStatus("0");
                            addCurtainMachineStatusService(machineStatus);
                            if (publish == 1) {
                                logger.info("✅ 发布成功：" + dto.getRelayProtocolId());
                            } else {
                                logger.info("❌ 发布失败：" + dto.getRelayProtocolId());
                            }
                            logger.info("设置窗帘关闭成功，设备协议ID: {}", dto.getRelayProtocolId());
                        }
                        else if (dto.getCurtainMachine() != null && dto.getCurtainMachine() == 1){
                            int publish = mqttManager.publish(null, String.valueOf(protocol.getSendTopic()), "1", 0, false);
                            machineStatus.setProtocolId(protocol.getProtocolId());
                            machineStatus.setStatus("1");
                            addCurtainMachineStatusService(machineStatus);
                            if (publish == 1) {
                                logger.info("✅ 发布成功：" + dto.getRelayProtocolId());
                            } else {
                                logger.info("❌ 发布失败：" + dto.getRelayProtocolId());
                            }
                            logger.info("设置窗帘开启成功，设备协议ID: {}", dto.getRelayProtocolId());
                        }else if (dto.getCurtainMachine() != null && dto.getCurtainMachine() == 2){
                            int publish = mqttManager.publish(null, String.valueOf(protocol.getSendTopic()), "2", 0, false);
                            machineStatus.setProtocolId(protocol.getProtocolId());
                            machineStatus.setStatus("2");
                            addCurtainMachineStatusService(machineStatus);
                            if (publish == 1) {
                                logger.info("✅ 发布成功：" + dto.getRelayProtocolId());
                            } else {
                                logger.info("❌ 发布失败：" + dto.getRelayProtocolId());
                            }
                            logger.info("设置窗帘暂停成功，设备协议ID: {}", dto.getRelayProtocolId());
                        }
                    }
                }
                result.put("code", 200);
                result.put("data", dto.getRelayProtocolId());
                result.put("message", "手动化设置成功");
                logger.info("手动化设置成功，设备协议ID: {}", dto.getRelayProtocolId());
                return result;
            }
            // 自动化控制
            else if (dto.getAutomation() == 2) {

                //判断设备数据是否为null
                if (dto.getAutomationConfig() == null){
                    throw new IllegalArgumentException("设备数据不能为空");
                }

                System.out.println(dto.getAutomationConfig());

                result.put("code", 200);
                result.put("data", dto.getRelayProtocolId());
                result.put("message", "自动化设置成功");
                logger.info("自动化设置成功，设备协议ID: {}", dto.getRelayProtocolId());
                return result;
            }
            // 错误
            else {
                result.put("code", 500);
                result.put("message", "请选择正确的自动化模式");
                logger.error("请选择正确的自动化模式");
                return result;
            }
        }catch (Exception e){
            result.put("code", 500);
            result.put("message", "设置失败: " + e.getMessage());
            logger.error("设置失败，设备协议ID: {}", dto.getRelayProtocolId(), e);
            return result;
        }
    }

    public void addCurtainMachineStatusService(IotCurtainMachineStatus iotCurtainMachineStatus){
        iotCurtainMachineStatusService.addCurtainMachineStatus(iotCurtainMachineStatus);
    }


}
