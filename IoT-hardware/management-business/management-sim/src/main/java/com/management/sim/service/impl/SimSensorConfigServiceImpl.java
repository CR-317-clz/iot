package com.management.sim.service.impl;

import com.management.sim.domain.SimSensorConfig;
import com.management.sim.domain.SimSerialPortConfig;
import com.management.sim.service.SimSensorConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimSensorConfigServiceImpl implements SimSensorConfigService {
    @Override
    public List<SimSensorConfig> getAllSimSensorConfig() {
        return null;
    }
}
