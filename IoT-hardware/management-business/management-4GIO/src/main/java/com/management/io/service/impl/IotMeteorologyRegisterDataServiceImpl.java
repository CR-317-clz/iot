package com.management.io.service.impl;

import com.alibaba.fastjson2.JSON;
import com.management.common.utils.snowFlake.SnowflakeIdGenerator;
import com.management.io.api.IOTokenMethod;
import com.management.io.api.IoAPIComstant;
import com.management.io.domain.IotMeteorologyDevice;
import com.management.io.domain.IotMeteorologyNode;
import com.management.io.domain.IotMeteorologyRegisterData;
import com.management.io.domain.IotMeteorologyRelayStatus;
import com.management.io.domain.dto.StatisticsOfTemperatureAndHumidityDto;
import com.management.io.domain.vo.StatisticsOfTemperatureAndHumidityVo;
import com.management.io.domain.vo.group.*;
import com.management.io.mapper.IotMeteorologyRegisterDataMapper;
import com.management.io.service.IotMeteorologyDeviceService;
import com.management.io.service.IotMeteorologyNodeService;
import com.management.io.service.IotMeteorologyRegisterDataService;
import com.management.io.service.IotMeteorologyRelayStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IotMeteorologyRegisterDataServiceImpl implements IotMeteorologyRegisterDataService {

    @Autowired
    private IOTokenMethod tokenMethod;

    @Autowired
    private IotMeteorologyRegisterDataMapper iotMeteorologyRegisterDataMapper;

    @Autowired
    private IotMeteorologyDeviceService iotMeteorologyDeviceService;

    @Autowired
    private IotMeteorologyRelayStatusService iotMeteorologyRelayStatusService;

    @Autowired
    private IotMeteorologyNodeService iotMeteorologyNodeService;

    @Autowired
    private IotMeteorologyRegisterDataService iotMeteorologyRegisterDataService;

    /**
     * 插入数据
     *
     * @param registerData 数据
     */
    @Override
    public boolean insert(IotMeteorologyRegisterData registerData) {
        try {
            registerData.setDataId(SnowflakeIdGenerator.nextId());
            int result = iotMeteorologyRegisterDataMapper.insertMeteorologyRegisterData(registerData);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加气象站设备寄存器数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备ID获取测点数据
     *
     * @param deviceId 设备ID
     * @return 测点数据列表
     */
    @Override
    public List<IotMeteorologyRegisterData> getMeteorologyRegisterDataByDeviceId(Long deviceId) {
        try {
            return iotMeteorologyRegisterDataMapper.getMeteorologyRegisterDataByDeviceId(deviceId);
        } catch (Exception e) {
            throw new RuntimeException("获取气象站数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定设备下的温湿度测点数据
     *
     * @param deviceId 设备ID
     * @return 测点数据列表
     */
    @Override
    public List<StatisticsOfTemperatureAndHumidityDto> getStatisticsOfTemperatureAndHumidity(String deviceId) {
        try {
            List<StatisticsOfTemperatureAndHumidityVo> list =
                    iotMeteorologyRegisterDataMapper.getStatisticsOfTemperatureAndHumidity(deviceId);

            if (list == null || list.isEmpty()) {
                throw new RuntimeException("暂无数据");
            }

            // 按星期分组
            Map<String, List<StatisticsOfTemperatureAndHumidityVo>> groupedByDay = list.stream()
                    .collect(Collectors.groupingBy(StatisticsOfTemperatureAndHumidityVo::getWeekDay));

            // 创建返回的DTO列表（每天一个DTO）
            List<StatisticsOfTemperatureAndHumidityDto> resultList = new ArrayList<>();

            // 按星期顺序处理（周一、周二、周三...）
            List<String> dayOrder = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");

            for (String day : dayOrder) {
                if (groupedByDay.containsKey(day)) {
                    List<StatisticsOfTemperatureAndHumidityVo> dayData = groupedByDay.get(day);

                    StatisticsOfTemperatureAndHumidityDto dto = new StatisticsOfTemperatureAndHumidityDto();
                    dto.setWeekDay(day);

                    List<StatisticsOfTemperatureAndHumidityDto.TemperatureAndHumidity> thList =
                            dayData.stream().map(r -> {
                                StatisticsOfTemperatureAndHumidityDto.TemperatureAndHumidity th =
                                        dto.new TemperatureAndHumidity();
                                th.setRegisterName(r.getRegisterName());
                                th.setAvgValue(r.getAvgValue());
                                return th;
                            }).collect(Collectors.toList());

                    dto.setTemperatureAndHumidityList(thList);
                    resultList.add(dto);
                }
            }

            return resultList;

        } catch (Exception e) {
            throw new RuntimeException("获取气象站统计数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新API数据
     *
     * @return 是否成功
     */
    @Override
    public boolean updateMeteorologyApiData() {
        // 创建RestTemplate实例
        RestTemplate restTemplate = new RestTemplate();

        // 添加拦截器统一设置Token
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().set("Authorization", tokenMethod.getToken());
            return execution.execute(request, body);
        }));

        // 构建带参数的URL groupld为空时查询全部
        String url = UriComponentsBuilder.fromHttpUrl(IoAPIComstant.realApi)
                .queryParam("groupld", "") // 请求参数
                .toUriString();

        try {
            // 发送GET请求并将响应映射到实体类
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
            // 处理响应
            if (response != null && response.getCode() == 1000) {
                System.out.println("成功获取 " + response.getData().size() + " 个设备数据");

                for (DeviceData device : response.getData()) {
                    try {
                        System.out.println("处理设备: " + device.getDeviceName() + ", 状态: " + device.getDeviceStatus());

                        // 1. 检查设备是否存在，存在则更新状态，不存在则新增
                        boolean deviceExists = checkAndUpdateDeviceMain(device);

                        // 如果设备存在且在线，保存动态变化的数据
                        if (deviceExists && "normal".equals(device.getDeviceStatus())) {
                            // 2. 保存继电器状态数据（记录状态变化历史）
                            saveRelayStatus(device);

                            // 3. 保存设备节点和寄存器数据（实时监控数据）
                            saveNodeAndRegisterData(device);
                        } else {
                            System.out.println("设备 " + device.getDeviceName() + " 离线或不存在，跳过数据保存");
                        }
                    } catch (Exception e) {
                        System.err.println("处理设备 " + device.getDeviceName() + " 时发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return true;
            } else if (response != null) {
                System.out.println("API请求失败，返回码: " + response.getCode() + ", 消息: " + response.getMessage());
            } else {
                System.out.println("API请求返回空响应");
            }
        } catch (Exception e) {
            System.err.println("请求发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 检查设备是否存在，存在则更新状态，不存在则新增设备
     */
    private boolean checkAndUpdateDeviceMain(DeviceData device) {
        try {
            // 根据设备地址码查询设备是否已存在于数据库
            IotMeteorologyDevice existingDevice = iotMeteorologyDeviceService.getMeteorologyDeviceByDeviceName(device.getDeviceName());

            if (existingDevice != null) {
                // 如果状态或位置有变化，则更新设备信息
                boolean needsUpdate = !existingDevice.getDeviceStatus().equals(device.getDeviceStatus()) ||
                        !Objects.equals(existingDevice.getLat(), device.getLat()) ||
                        !Objects.equals(existingDevice.getLng(), device.getLng()) ||
                        !Objects.equals(existingDevice.getSystemCode(), device.getSystemCode());

                if (needsUpdate) {
                    IotMeteorologyDevice deviceMain = new IotMeteorologyDevice();
                    deviceMain.setDeviceId(existingDevice.getDeviceId());
                    deviceMain.setDeviceAddr(String.valueOf(device.getDeviceAddr()));
                    deviceMain.setDeviceName(device.getDeviceName());
                    deviceMain.setDeviceStatus(device.getDeviceStatus());
                    deviceMain.setLat(device.getLat());
                    deviceMain.setLng(device.getLng());
                    deviceMain.setSystemCode(device.getSystemCode());
                    deviceMain.setTimestamp(device.getTimeStamp());

                    // 执行更新操作
                    iotMeteorologyDeviceService.update(deviceMain);

                    // 记录状态变化日志
                    if (!existingDevice.getDeviceStatus().equals(device.getDeviceStatus())) {
                        System.out.println("设备 " + device.getDeviceName() + " 状态从 " +
                                existingDevice.getDeviceStatus() + " 变为 " + device.getDeviceStatus());
                    }
                }
                return true;
            } else {
                // 设备不存在，新增设备到主表
                IotMeteorologyDevice deviceMain = new IotMeteorologyDevice();
                deviceMain.setDeviceAddr(String.valueOf(device.getDeviceAddr()));
                deviceMain.setDeviceName(device.getDeviceName());
                deviceMain.setDeviceStatus(device.getDeviceStatus());
                deviceMain.setLat(device.getLat());
                deviceMain.setLng(device.getLng());
                deviceMain.setSystemCode(device.getSystemCode());
                deviceMain.setTimestamp(device.getTimeStamp());

                // 执行插入操作
                iotMeteorologyDeviceService.insert(deviceMain);
                System.out.println("新增设备: " + device.getDeviceName());
                return false;
            }
        } catch (Exception e) {
            System.err.println("处理设备主数据失败 - 设备: " + device.getDeviceName() + ", 错误: " + e.getMessage());
            throw new RuntimeException("处理设备主数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存继电器状态数据到设备继电器状态表
     */
    private void saveRelayStatus(DeviceData device) {
        // 检查继电器状态数据是否存在且不为空
        if (device.getRelayStatusItems() != null && !device.getRelayStatusItems().isEmpty()) {
            try {
                // 遍历所有继电器状态
                for (RelayStatusItem relayItem : device.getRelayStatusItems()) {
                    Integer relayNo = relayItem.getRelayNo();
                    Integer relayStatus = relayItem.getRelayStatus();

                    if (relayNo != null && relayStatus != null) {
                        // 查询该设备指定继电器的最新状态记录
                        IotMeteorologyRelayStatus latestRelay = iotMeteorologyRelayStatusService
                                .selectLatestByDeviceAdrAndRelayNo(String.valueOf(device.getDeviceAddr()), relayNo);

                        // 如果状态有变化或者没有历史记录，则保存新的继电器状态
                        if (latestRelay == null || !latestRelay.getRelayStatus().equals(relayStatus)) {
                            IotMeteorologyRelayStatus relay = new IotMeteorologyRelayStatus();
                            relay.setDeviceAdr(String.valueOf(device.getDeviceAddr()));
                            relay.setRelayNo(relayNo);
                            relay.setRelayStatus(relayStatus);

                            // 保存到数据库
                            iotMeteorologyRelayStatusService.insert(relay);

                            if (latestRelay != null) {
                                System.out.println("设备 " + device.getDeviceName() + " 继电器" + relayNo +
                                        " 状态从 " + latestRelay.getRelayStatus() + " 变为 " + relayStatus);
                            } else {
                                System.out.println("设备 " + device.getDeviceName() + " 继电器" + relayNo + " 初始状态: " + relayStatus);
                            }
                        } else {
                            System.out.println("设备 " + device.getDeviceName() + " 继电器" + relayNo + " 状态无变化: " + relayStatus);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("处理继电器状态失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("设备 " + device.getDeviceName() + " 无继电器状态数据");
        }
    }

    /**
     * 保存设备节点和寄存器数据
     */
    private void saveNodeAndRegisterData(DeviceData device) {
        // 检查设备节点数据是否存在
        if (device.getDataItem() != null && !device.getDataItem().isEmpty()) {
            // 遍历所有设备节点
            for (DataNode node : device.getDataItem()) {
                try {
                    // 检查节点是否已存在（根据设备地址和节点ID）
                    IotMeteorologyNode existingNode = iotMeteorologyNodeService.selectByDeviceAddrAndNodeId(
                            String.valueOf(device.getDeviceAddr()), node.getNodeId());

                    Long nodeId;
                    if (existingNode != null) {
                        // 节点存在，更新节点信息
                        nodeId = existingNode.getNodeId();
                        IotMeteorologyNode deviceNode = new IotMeteorologyNode();
                        deviceNode.setNodeId(nodeId);
                        deviceNode.setNodeCodeId(JSON.toJSONString(device.getRelayStatusItems()));
                        deviceNode.setNodeJson(JSON.toJSONString(node));
                        deviceNode.setDeviceAdr(String.valueOf(device.getDeviceAddr()));

                        iotMeteorologyNodeService.update(deviceNode);
                        System.out.println("更新节点数据 - 设备:" + device.getDeviceName() + ", 节点:" + node.getNodeId());
                    } else {
                        // 节点不存在，新增节点记录
                        nodeId = SnowflakeIdGenerator.nextId();
                        IotMeteorologyNode deviceNode = new IotMeteorologyNode();
                        deviceNode.setNodeId(nodeId);
                        deviceNode.setNodeCodeId(JSON.toJSONString(device.getRelayStatusItems()));
                        deviceNode.setDeviceAdr(String.valueOf(device.getDeviceAddr()));
                        deviceNode.setNodeJson(JSON.toJSONString(node));
                        iotMeteorologyNodeService.insert(deviceNode);
                        System.out.println("新增节点数据 - 设备:" + device.getDeviceName() + ", 节点:" + node.getNodeId());
                    }

                    // 保存寄存器数据
                    saveRegisterData(node, String.valueOf(device.getDeviceAddr()), nodeId);

                } catch (Exception e) {
                    System.err.println("保存节点数据失败 - 设备:" + device.getDeviceName() +
                            ", 节点:" + node.getNodeId() + ", 错误:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("设备 " + device.getDeviceName() + " 无节点数据");
        }
    }

    /**
     * 保存寄存器数据到设备寄存器数据表
     */
    private void saveRegisterData(DataNode node, String deviceAddr, Long nodeId) {
        // 检查寄存器数据是否存在
        if (node.getRegisterItem() != null && !node.getRegisterItem().isEmpty()) {
            // 遍历所有寄存器项
            for (RegisterItem register : node.getRegisterItem()) {
                try {
                    IotMeteorologyRegisterData registerData = new IotMeteorologyRegisterData();
                    registerData.setDataId(SnowflakeIdGenerator.nextId());
                    registerData.setNodeId(nodeId);
                    registerData.setRegisterId(register.getRegisterId());
                    registerData.setDeviceAddr(deviceAddr);
                    registerData.setDataValue(register.getData());
                    registerData.setValueNum(register.getValue());
                    registerData.setAlarmLevel(register.getAlarmLevel());
                    registerData.setAlarmColor(register.getAlarmColor());
                    registerData.setAlarmInfo(register.getAlarmInfo());
                    registerData.setUnit(register.getUnit());
                    registerData.setCreateTime(new Date());
                    registerData.setRegisterName(register.getRegisterName());

                    // 保存到数据库
                    iotMeteorologyRegisterDataService.insert(registerData);

                    System.out.println("保存寄存器数据 - 设备:" + deviceAddr +
                            ", 节点:" + node.getNodeId() +
                            ", 寄存器:" + register.getRegisterName() +
                            ", 值:" + register.getData());

                } catch (Exception e) {
                    System.err.println("保存寄存器数据失败 - 设备:" + deviceAddr +
                            ", 节点:" + node.getNodeId() +
                            ", 寄存器:" + register.getRegisterId() +
                            ", 错误:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("节点 " + node.getNodeId() + " 无寄存器数据");
        }
    }

    /**
     * 根据设备地址码获取设备节点数据
     *
     * @param deviceAddr 设备地址码
     * @return 设备节点数据列表
     */
    @Override
    public List<IotMeteorologyRegisterData> iotMeteorologyRegisterDataByAddress(String deviceAddr) {
        try {
            return iotMeteorologyRegisterDataMapper.iotMeteorologyRegisterDataByAddress(deviceAddr);
        } catch (Exception e) {
            throw new RuntimeException("根据地址获取气象站设备数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据设备地址码获取设备节点数据
     */
    @Override
    public List<IotMeteorologyRegisterData> SelectIotMeteorologyRegisterDataByAddress(String deviceAddr) {
        try {
            return iotMeteorologyRegisterDataMapper.SelectIotMeteorologyRegisterDataByAddress(deviceAddr);
        } catch (Exception e) {
            throw new RuntimeException("根据地址查询气象站设备数据失败: " + e.getMessage(), e);
        }
    }
}
