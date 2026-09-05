package com.management.web.controller.business.sim;

import com.management.common.core.controller.BaseController;
import com.management.common.core.page.TableDataInfo;
import com.management.sim.constant.EquipmentConfigurationConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/equipment_configuration")
public class EquipmentConfigurationController extends BaseController {

    @GetMapping("/constants/list")
    public TableDataInfo getEquipmentConstantsList() {
        List<String> constants = Arrays.asList(
                EquipmentConfigurationConstant.COORDINATOR,
                EquipmentConfigurationConstant.ROUTER,
                EquipmentConfigurationConstant.TERMINATOR
        );
        return getDataTable(constants);
    }

}
