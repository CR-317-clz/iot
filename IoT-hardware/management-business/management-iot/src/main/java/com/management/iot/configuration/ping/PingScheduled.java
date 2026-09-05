package com.management.iot.configuration.ping;

import com.management.iot.service.ping.DeviceStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PingScheduled {

    @Autowired
    private DeviceStatusService deviceStatusService;

    /**
     * 每分钟执行一次设备状态检查
     */
    @Scheduled(fixedRate = 10000)
    public void scheduledDeviceStatusCheck() {
        log.info("🕒 === 定时Ping检测任务开始执行 ===");
        long startTime = System.currentTimeMillis();

        try {
            deviceStatusService.checkAllDevicesStatus();
            long endTime = System.currentTimeMillis();
            log.info("✅ === 定时Ping检测任务完成，耗时: {}ms ===", (endTime - startTime));
        } catch (Exception e) {
            log.error("❌ === 定时Ping检测任务执行失败 ===", e);
        }
    }

}
