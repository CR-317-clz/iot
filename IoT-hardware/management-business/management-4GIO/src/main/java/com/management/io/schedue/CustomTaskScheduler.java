package com.management.io.schedue;

import com.management.io.service.IotMeteorologyRegisterDataService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CustomTaskScheduler {

    @Autowired
    private IotMeteorologyRegisterDataService iotMeteorologyRegisterDataService;


    /**
     * 定时任务每5分钟新增一次数据
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void addTask(){
        iotMeteorologyRegisterDataService.updateMeteorologyApiData();
    }

}
