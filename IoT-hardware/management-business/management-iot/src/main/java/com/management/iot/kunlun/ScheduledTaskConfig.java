package com.management.iot.kunlun;
import com.management.common.core.domain.AjaxResult;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableScheduling
public class ScheduledTaskConfig {

    @Autowired
    private DeviceDataCollectService deviceDataCollectService;

    /**
     * 定时收集和发送设备数据（每5分钟执行一次）
     */
    @Scheduled(fixedDelay = 50000)
    // 1分钟
    public void scheduledDataCollection() {
        System.out.println("\n⏰ 定时任务开始: 收集设备数据");
        AjaxResult result = deviceDataCollectService.collectAndSendDeviceData();
        System.out.println("✅ 定时任务完成: " + result.get("msg"));
    }

    /**
     * 心跳检查（每分钟执行一次）
     */
//    @Scheduled(fixedDelay = 60000) // 1分钟
    public void heartbeatCheck() {
        System.out.println("💓 系统运行正常，等待下次数据收集...");
    }

}