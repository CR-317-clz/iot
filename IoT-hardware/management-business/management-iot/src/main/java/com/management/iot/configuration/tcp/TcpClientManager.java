package com.management.iot.configuration.tcp;

import com.fasterxml.jackson.databind.ObjectMapper; // JSON解析
import com.management.iot.domain.IotProtocol;
import com.management.iot.domain.IotRelayDeviceStatus;
import com.management.iot.domain.enumeration.DeviceEnum;
import com.management.iot.domain.enumeration.DeviceStatusEnum;
import com.management.iot.service.IotDeviceService;
import com.management.iot.service.IotRelayDeviceStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理所有设备的TCP客户端
 */
@Slf4j
@Service
public class TcpClientManager {

    private final Map<Long, TcpClient> clientMap = new HashMap<>();
    private final Map<Long, String> configHashMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 状态版本控制核心数据结构
    private final Map<Long, Long> protocolStatusVersion = new ConcurrentHashMap<>(); // 协议ID -> 状态版本号
    private final Map<Long, Long> lastQueryVersion = new ConcurrentHashMap<>();     // 协议ID -> 最后查询版本号
    private final Map<Long, List<IotRelayDeviceStatus>> protocolStatusMap = new ConcurrentHashMap<>(); // 协议ID -> 状态列表
    private final List<IotRelayDeviceStatus> currentDeviceStatuses = new CopyOnWriteArrayList<>();
    private final Object statusLock = new Object(); // 状态更新锁

    // 等待机制 - 确保第一次请求获取最新数据
    private final Map<Long, Object> protocolWaitLocks = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> protocolDataReceived = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastMessageTime = new ConcurrentHashMap<>();

    private final IotDeviceService deviceService;
    private final IotRelayDeviceStatusService relayDeviceStatusService;

    public TcpClientManager(IotDeviceService deviceService, IotRelayDeviceStatusService relayDeviceStatusService) {
        this.deviceService = deviceService;
        this.relayDeviceStatusService = relayDeviceStatusService;
    }

    /**
     * 发送消息并等待响应（确保第一次请求获取最新数据）
     */
    public List<IotRelayDeviceStatus> sendMessageAndWaitResponse(Long protocolId, String message) {
        System.out.println("🚀 发送消息并等待响应 - 协议:" + protocolId + ", 消息:" + message);

        // 检查客户端是否存在
        if (!clientMap.containsKey(protocolId)) {
            System.out.println("❌ 协议 " + protocolId + " 的客户端不存在");
            return new ArrayList<>();
        }

        // 初始化等待锁
        Object waitLock = protocolWaitLocks.computeIfAbsent(protocolId, k -> new Object());
        protocolDataReceived.put(protocolId, false);
        lastMessageTime.put(protocolId, System.currentTimeMillis());

        // 发送消息
        boolean sendResult = sendMessage(protocolId, message);
        if (!sendResult) {
            System.out.println("❌ 发送消息失败");
            return new ArrayList<>();
        }

        // 等待响应（最多等待2秒）
        return waitForResponse(protocolId, 2000);
    }

    /**
     * 等待TCP响应
     */
    private List<IotRelayDeviceStatus> waitForResponse(Long protocolId, long timeoutMs) {
        Object waitLock = protocolWaitLocks.get(protocolId);
        if (waitLock == null) {
            return getCurrentRelayDeviceStatusByProtocol(protocolId);
        }

        long startTime = System.currentTimeMillis();

        synchronized (waitLock) {
            while (!protocolDataReceived.getOrDefault(protocolId, false)) {
                try {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remaining = timeoutMs - elapsed;

                    if (remaining <= 0) {
                        System.out.println("⏰ 等待响应超时，返回当前数据");
                        break;
                    }

                    System.out.println("⏳ 等待TCP响应，剩余时间: " + remaining + "ms");
                    waitLock.wait(remaining);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("❌ 等待被中断");
                    break;
                }
            }
        }

        // 获取最新状态
        List<IotRelayDeviceStatus> result = getCurrentRelayDeviceStatusByProtocol(protocolId);
        System.out.println("✅ 获取到响应数据，设备数量: " + result.size());
        return result;
    }

    /**
     * 通知数据已接收
     */
    private void notifyDataReceived(Long protocolId) {
        Object waitLock = protocolWaitLocks.get(protocolId);
        if (waitLock != null) {
            synchronized (waitLock) {
                protocolDataReceived.put(protocolId, true);
                waitLock.notifyAll();
                System.out.println("🔔 通知数据已接收 - 协议:" + protocolId);
            }
        }
    }

    /**
     * 刷新客户端列表
     */
    public void refreshClients(List<IotProtocol> protocols) {
        System.out.println("🔄 开始刷新TCP客户端列表，共 " + protocols.size() + " 个协议");

        for (IotProtocol protocol : protocols) {
            try {
                System.out.println("🔍 检查协议 ID: " + protocol.getProtocolId());

                String configJson = protocol.getConfigJson();
                String newHash = String.valueOf(configJson.hashCode());
                String oldHash = configHashMap.get(protocol.getProtocolId());

                System.out.println("   📝 配置哈希: 旧=" + oldHash + ", 新=" + newHash);

                if (!newHash.equals(oldHash)) {

                    if (oldHash != null) {
                        System.out.println("   ⚠️ 检测到配置变化，重新连接协议 " + protocol.getProtocolId());
                    } else {
                        System.out.println("   🆕 首次连接协议 " + protocol.getProtocolId());
                    }

                    // 先断开旧连接
                    TcpClient client = clientMap.get(protocol.getProtocolId());
                    if (client != null) {
                        System.out.println("   🔌 断开旧连接");
                        client.disconnect();
                    }

                    // 解析JSON配置，获取host和topic
                    System.out.println("   📄 解析JSON配置: " + configJson);
                    Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
                    String host = getStringFromConfig(config, "host");
                    int topic = getIntFromConfig(config, "topic");

                    System.out.println("   🌐 解析配置 - 主机: " + host + ", 端口: " + topic);

                    // 验证配置是否有效
                    if (host == null || host.trim().isEmpty()) {
                        System.out.println("   ❌ 主机地址不能为空，跳过协议 " + protocol.getProtocolId());
                        continue;
                    }

                    if (topic <= 0 || topic > 65535) {
                        System.out.println("   ❌ 端口号无效: " + topic + "，跳过协议 " + protocol.getProtocolId());
                        continue;
                    }

                    // 初始化协议版本
                    initializeProtocolVersion(protocol.getProtocolId());

                    // 创建新的TCP客户端，带回调处理接收数据
                    client = new TcpClient(host, topic, message -> {
                        System.out.println("📨 收到协议 " + protocol.getProtocolId() + " 的数据: " + message);
                        log.info("Received from protocol {}: {}", protocol.getProtocolId(), message);

                        try {
                            // 解析ASCII数据
                            parseAndUpdateDeviceStatus(protocol.getProtocolId(), message);
                        } catch (Exception e) {
                            System.out.println("❌ 解析设备数据失败: " + e.getMessage());
                            log.error("Failed to parse device data for protocol {}", protocol.getProtocolId(), e);
                        }
                    });

                    // 建立TCP连接
                    client.connect();

                    // 保存客户端和配置哈希
                    clientMap.put(protocol.getProtocolId(), client);
                    configHashMap.put(protocol.getProtocolId(), newHash);

                    System.out.println("   ✅ 协议 " + protocol.getProtocolId() + " 客户端已更新");
                } else {
                    System.out.println("   ✅ 协议 " + protocol.getProtocolId() + " 配置未变化，保持现有连接");
                }

            } catch (Exception e) {
                System.out.println("💥 刷新协议 " + protocol.getProtocolId() + " 客户端时出错: " + e.getMessage());
                log.error("Error refreshing client for protocol {}", protocol.getProtocolId(), e);
            }
        }

        System.out.println("🎊 TCP客户端刷新完成，当前管理 " + clientMap.size() + " 个活跃连接");
    }

    /**
     * 发送控制消息到指定协议设备
     */
    public boolean sendMessage(Long protocolId, String message) {
        System.out.println("📤 发送消息到协议 " + protocolId + ": " + message);

        TcpClient client = clientMap.get(protocolId);
        if (client != null && client.isActive()) {
            System.out.println("✅ 找到活跃客户端，开始发送消息");
            client.sendMessage(message);
            return true;
        } else {
            System.out.println("❌ 客户端 " + protocolId + " 未激活或不存在，无法发送消息");
            log.warn("Client {} is not active, cannot send message.", protocolId);
            return false;
        }
    }

    /**
     * 解析ASCII数据并更新设备状态
     */
    private void parseAndUpdateDeviceStatus(Long protocolId, String message) {
        if (message == null || message.length() < 10) {
            System.out.println("⚠️ 数据长度不足，需要至少10个字符，实际: " + (message == null ? "null" : message.length()));
            return;
        }

        System.out.println("🔍 开始解析数据: " + message);

        // 将字符串转换为ASCII码值
        int[] asciiValues = new int[message.length()];
        for (int i = 0; i < message.length(); i++) {
            asciiValues[i] = (int) message.charAt(i);
        }

        // 打印ASCII值用于调试
        printAsciiDebugInfo(asciiValues);

        // 解析设备状态（跳过前两个字符）
        parseDeviceStates(protocolId, message, asciiValues);

        // 通知数据已接收
        notifyDataReceived(protocolId);
    }

    /**
     * 解析设备状态（状态+设备交替模式）
     */
    private void parseDeviceStates(Long protocolId, String rawMessage, int[] asciiValues) {
        System.out.println("🚀 开始解析协议 " + protocolId + " 的设备状态");

        List<IotRelayDeviceStatus> newStatuses = new ArrayList<>();

        for (int i = 2; i < 10; i += 2) {
            if (i + 1 >= asciiValues.length) {
                System.out.println("❌ 数据长度不足，期望至少10个字符");
                return;
            }

            int statusAscii = asciiValues[i];
            int deviceAscii = asciiValues[i + 1];

            try {
                DeviceEnum deviceType = getDeviceTypeByAscii(deviceAscii);
                DeviceStatusEnum deviceStatus = DeviceStatusEnum.getByAsciiValue(statusAscii);

                System.out.println("   🎯 设备" + ((i-2)/2 + 1) + ": " +
                        deviceType.getName() + " (设备ASCII:" + deviceAscii + ")" +
                        " 状态: " + deviceStatus.getDescription() + " (状态ASCII:" + statusAscii + ")");

                // 更新设备状态到数据库
                updateDeviceStatusInDB(protocolId, deviceType, deviceStatus, statusAscii, deviceAscii);

                // 创建设备状态对象
                IotRelayDeviceStatus deviceStatusEntity = createDeviceStatus(
                        protocolId, deviceType, deviceStatus, statusAscii, deviceAscii
                );

                newStatuses.add(deviceStatusEntity);
            } catch (IllegalArgumentException e) {
                System.out.println("❌ 解析设备状态失败: " + e.getMessage());
                log.error("Failed to parse device state for protocol {}", protocolId, e);
            }
        }

        // 原子性更新状态和版本
        updateProtocolStatus(protocolId, newStatuses);

        printDeviceStatusSummary(protocolId, rawMessage);
    }

    /**
     * 原子性更新协议状态
     */
    private void updateProtocolStatus(Long protocolId, List<IotRelayDeviceStatus> newStatuses) {
        synchronized (statusLock) {
            // 生成新的版本号（使用纳秒级时间戳确保更高精度）
            long newVersion = System.nanoTime();

            // 更新版本号和状态数据
            protocolStatusVersion.put(protocolId, newVersion);
            protocolStatusMap.put(protocolId, createStatusListCopy(newStatuses));
            updateCurrentDeviceStatuses();

            System.out.println("✅ 协议 " + protocolId + " 状态已原子性更新，版本: " + newVersion +
                    ", 设备数量: " + newStatuses.size());
        }
    }

    /**
     * 创建状态列表的深拷贝
     */
    private List<IotRelayDeviceStatus> createStatusListCopy(List<IotRelayDeviceStatus> original) {
        if (original == null) {
            return new ArrayList<>();
        }

        List<IotRelayDeviceStatus> copy = new ArrayList<>();
        for (IotRelayDeviceStatus status : original) {
            copy.add(createStatusCopy(status));
        }
        return copy;
    }

    /**
     * 创建设备状态对象的深拷贝
     */
    private IotRelayDeviceStatus createStatusCopy(IotRelayDeviceStatus original) {
        IotRelayDeviceStatus copy = new IotRelayDeviceStatus();
        copy.setProtocolId(original.getProtocolId());
        copy.setRelayDeviceName(original.getRelayDeviceName());
        copy.setStatus(original.getStatus());
        copy.setCreateTime(original.getCreateTime() != null ?
                new Date(original.getCreateTime().getTime()) : new Date());
        return copy;
    }

    /**
     * 更新全局状态列表
     */
    private void updateCurrentDeviceStatuses() {
        List<IotRelayDeviceStatus> allStatuses = new ArrayList<>();
        for (List<IotRelayDeviceStatus> statusList : protocolStatusMap.values()) {
            allStatuses.addAll(statusList);
        }

        currentDeviceStatuses.clear();
        currentDeviceStatuses.addAll(allStatuses);
        System.out.println("🔄 全局状态列表已更新，当前共有 " + currentDeviceStatuses.size() + " 个设备状态");
    }

    /**
     * 初始化协议版本
     */
    private void initializeProtocolVersion(Long protocolId) {
        synchronized (statusLock) {
            if (!protocolStatusVersion.containsKey(protocolId)) {
                long initialVersion = System.nanoTime();
                protocolStatusVersion.put(protocolId, initialVersion);
                protocolStatusMap.put(protocolId, new ArrayList<>());
                System.out.println("🎯 初始化协议 " + protocolId + " 版本: " + initialVersion);
            }
        }
    }

    /**
     * 获取指定协议的当前设备状态（确保第一次请求获取最新数据）
     */
    public List<IotRelayDeviceStatus> getCurrentRelayDeviceStatusByProtocol(Long protocolId) {
        // 先检查客户端是否存在
        if (!clientMap.containsKey(protocolId)) {
            System.out.println("❌ 协议 " + protocolId + " 的客户端不存在");
            return new ArrayList<>();
        }

        synchronized (statusLock) {
            Long currentVersion = protocolStatusVersion.get(protocolId);
            Long lastVersion = lastQueryVersion.get(protocolId);

            System.out.println("📥 查询协议 " + protocolId + " 状态 - 当前版本: " + currentVersion + ", 最后查询版本: " + lastVersion);

            // 如果还没有状态数据，返回空列表
            if (currentVersion == null) {
                System.out.println("⚠️ 协议 " + protocolId + " 尚无状态数据，返回空列表");
                return new ArrayList<>();
            }

            List<IotRelayDeviceStatus> result;

            // 如果是第一次查询或者版本有更新，直接返回最新数据
            if (lastVersion == null || !lastVersion.equals(currentVersion)) {
                result = getLatestData(protocolId, currentVersion);
            } else {
                // 如果版本相同，返回当前数据
                result = getCurrentData(protocolId, currentVersion);
            }

            // 记录查询日志
            logQueryResult(protocolId, result);
            return result;
        }
    }

    /**
     * 获取最新数据并更新查询版本
     */
    private List<IotRelayDeviceStatus> getLatestData(Long protocolId, Long currentVersion) {
        List<IotRelayDeviceStatus> statuses = protocolStatusMap.get(protocolId);

        // 确保返回的是最新的数据副本
        List<IotRelayDeviceStatus> result = createStatusListCopy(statuses);

        lastQueryVersion.put(protocolId, currentVersion);
        System.out.println("🔄 版本变化 " + lastQueryVersion.get(protocolId) + " -> " + currentVersion +
                ", 返回最新数据，设备数量: " + result.size());
        return result;
    }

    /**
     * 获取当前数据
     */
    private List<IotRelayDeviceStatus> getCurrentData(Long protocolId, Long version) {
        List<IotRelayDeviceStatus> statuses = protocolStatusMap.get(protocolId);
        List<IotRelayDeviceStatus> result = createStatusListCopy(statuses);
        int count = result != null ? result.size() : 0;
        System.out.println("ℹ️ 返回当前数据，版本: " + version + ", 设备数量: " + count);
        return result;
    }

    /**
     * 记录查询结果
     */
    private void logQueryResult(Long protocolId, List<IotRelayDeviceStatus> result) {
        System.out.println("📤 返回协议 " + protocolId + " 的查询结果:");
        if (result.isEmpty()) {
            System.out.println("   - 无数据");
        } else {
            for (IotRelayDeviceStatus status : result) {
                System.out.println("   - " + status.getRelayDeviceName() + ": " + status.getStatus() +
                        " (创建时间: " + status.getCreateTime() + ")");
            }
        }
    }

    // 其他辅助方法保持不变
    private String getStringFromConfig(Map<String, Object> config, String key) {
        try {
            Object value = config.get(key);
            if (value == null) {
                System.out.println("   ⚠️ 配置项 " + key + " 不存在");
                return null;
            }
            return value.toString();
        } catch (Exception e) {
            System.out.println("   ⚠️ 获取配置项 " + key + " 时出错: " + e.getMessage());
            return null;
        }
    }

    private int getIntFromConfig(Map<String, Object> config, String key) {
        try {
            Object value = config.get(key);
            if (value == null) {
                System.out.println("   ⚠️ 配置项 " + key + " 不存在");
                return 0;
            }

            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                System.out.println("   ⚠️ 配置项 " + key + " 类型不支持: " + value.getClass().getSimpleName());
                return 0;
            }
        } catch (NumberFormatException e) {
            System.out.println("   ⚠️ 配置项 " + key + " 不是有效的数字: " + config.get(key));
            return 0;
        } catch (Exception e) {
            System.out.println("   ⚠️ 获取配置项 " + key + " 时出错: " + e.getMessage());
            return 0;
        }
    }

    private DeviceEnum getDeviceTypeByAscii(int deviceAscii) {
        switch (deviceAscii) {
            case 49: return DeviceEnum.FAN;
            case 50: return DeviceEnum.WATER_CURTAIN;
            case 51: return DeviceEnum.SPRINKLER;
            case 52: return DeviceEnum.LIGHT;
            default: throw new IllegalArgumentException("未知的设备ASCII值: " + deviceAscii);
        }
    }

    private void printAsciiDebugInfo(int[] asciiValues) {
        StringBuilder asciiStr = new StringBuilder("ASCII值: ");
        for (int value : asciiValues) {
            asciiStr.append(value).append(" ");
        }
        System.out.println(asciiStr.toString());
    }

    private void updateDeviceStatusInDB(Long protocolId, DeviceEnum deviceType, DeviceStatusEnum status,
                                        int statusAscii, int deviceAscii) {
        try {
            System.out.println("💾 更新数据库 - 协议:" + protocolId +
                    ", 设备:" + deviceType.getName() +
                    ", 状态:" + status.getDescription() +
                    ", 状态ASCII:" + statusAscii +
                    ", 设备ASCII:" + deviceAscii);

            IotRelayDeviceStatus deviceStatus = new IotRelayDeviceStatus();
            deviceStatus.setProtocolId(protocolId);
            deviceStatus.setRelayDeviceName(deviceType.getName());
            if (status == DeviceStatusEnum.ON) {
                deviceStatus.setStatus("1");
            } else if (status == DeviceStatusEnum.OFF) {
                deviceStatus.setStatus("0");
            }
            deviceStatus.setCreateTime(new Date());

            relayDeviceStatusService.insert(deviceStatus);
        } catch (Exception e) {
            System.out.println("❌ 更新设备状态到数据库失败: " + e.getMessage());
            log.error("Failed to update device status to database", e);
        }
    }

    private void printDeviceStatusSummary(Long protocolId, String rawMessage) {
        System.out.println("📊 设备状态摘要 - 协议:" + protocolId);
        System.out.println("   原始数据: " + rawMessage);
        System.out.println("   解析结果:");

        if (rawMessage != null && rawMessage.length() >= 10) {
            for (int i = 2; i < 10; i += 2) {
                int statusAscii = (int) rawMessage.charAt(i);
                int deviceAscii = (int) rawMessage.charAt(i + 1);

                DeviceEnum deviceType = DeviceEnum.getByAsciiValue(deviceAscii);
                DeviceStatusEnum status = DeviceStatusEnum.getByAsciiValue(statusAscii);

                System.out.println("   " + deviceType.getName() +
                        ": " + status.getDescription() +
                        " (状态:" + statusAscii + ", 设备:" + deviceAscii + ")");
            }
        }
    }

    private IotRelayDeviceStatus createDeviceStatus(Long protocolId, DeviceEnum deviceType,
                                                    DeviceStatusEnum status, int statusAscii, int deviceAscii) {
        IotRelayDeviceStatus deviceStatus = new IotRelayDeviceStatus();
        deviceStatus.setProtocolId(protocolId);
        deviceStatus.setRelayDeviceName(deviceType.getName());

        String statusValue = (status == DeviceStatusEnum.ON) ? "1" : "0";
        deviceStatus.setStatus(statusValue);
        deviceStatus.setCreateTime(new Date());

        System.out.println("🎯 创建设备状态 - " + deviceType.getName() + ": " + statusValue +
                " (ASCII状态:" + statusAscii + ", ASCII设备:" + deviceAscii + ")");

        return deviceStatus;
    }

    public int getClientCount() {
        return clientMap.size();
    }

    public String getClientStatus(Long protocolId) {
        TcpClient client = clientMap.get(protocolId);
        if (client == null) return "未创建";
        else if (client.isActive()) return "活跃";
        else return "未连接";
    }

    public List<IotRelayDeviceStatus> getCurrentRelayDeviceStatus() {
        synchronized (statusLock) {
            return createStatusListCopy(currentDeviceStatuses);
        }
    }

}