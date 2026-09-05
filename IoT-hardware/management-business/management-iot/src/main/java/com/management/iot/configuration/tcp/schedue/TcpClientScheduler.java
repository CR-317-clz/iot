package com.management.iot.configuration.tcp.schedue;

import com.management.iot.configuration.tcp.TcpClientManager;
import com.management.iot.domain.IotProtocol;
import com.management.iot.service.IotProtocolService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务组件，用于每30秒检查数据库配置变化
 */
@Component
public class TcpClientScheduler {

    private final TcpClientManager clientManager; // TCP客户端管理器
    private final IotProtocolService iotProtocolService; // 协议数据库仓库

    public TcpClientScheduler(TcpClientManager clientManager, IotProtocolService iotProtocolService) {
        this.clientManager = clientManager;
        this.iotProtocolService = iotProtocolService;
    }

    /**
     * 每30秒执行一次，刷新TCP客户端
     */
    @Scheduled(fixedDelay = 30000)
    public void refreshClients() {
        System.out.println("🕒 开始检查 TCP 配置变化...");
        System.out.println("📊 查询数据库中的TCP协议配置...");
        
        // 查询所有协议配置
        List<IotProtocol> protocols = iotProtocolService.getProtocolsByTCP();
        System.out.println("✅ 数据库查询完成，找到 " + protocols.size() + " 个TCP协议配置");
        
        // 刷新TCP客户端列表，自动重连变化的配置
        clientManager.refreshClients(protocols);
        System.out.println("🔄 TCP客户端刷新任务完成");
    }
}