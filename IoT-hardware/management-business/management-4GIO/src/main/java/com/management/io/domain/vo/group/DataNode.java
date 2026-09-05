package com.management.io.domain.vo.group;

import java.util.List;

public class DataNode {
    private Long nodeId;
    private List<RegisterItem> registerItem;
    
    // Getters and Setters
    public Long getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
    
    public List<RegisterItem> getRegisterItem() {
        return registerItem;
    }
    
    public void setRegisterItem(List<RegisterItem> registerItem) {
        this.registerItem = registerItem;
    }
    
    @Override
    public String toString() {
        return "DataNode{" +
               "nodeId=" + nodeId +
               ", registerItem=" + registerItem +
               '}';
    }
}