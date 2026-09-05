package com.management.io.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "jy_management.iot_meteorology_node")
public class IotMeteorologyNode {

    /**
     * 节点id
     */
    private Long nodeId;

    /**
     * 节点编号
     */
    private String nodeCodeId;

    /**
     * 节点json
     */
    private String nodeJson;

    private String deviceAdr;

    /**
     * 创建时间
     */
    private Data createTime;

}
