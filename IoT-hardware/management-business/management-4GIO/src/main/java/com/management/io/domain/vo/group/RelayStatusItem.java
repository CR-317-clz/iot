package com.management.io.domain.vo.group;

public class RelayStatusItem {
    private int relayNo;
    private int relayStatus;
    
    // Getters and Setters
    public int getRelayNo() {
        return relayNo;
    }
    
    public void setRelayNo(int relayNo) {
        this.relayNo = relayNo;
    }
    
    public int getRelayStatus() {
        return relayStatus;
    }
    
    public void setRelayStatus(int relayStatus) {
        this.relayStatus = relayStatus;
    }
    
    @Override
    public String toString() {
        return "RelayStatusItem{" +
               "relayNo=" + relayNo +
               ", relayStatus=" + relayStatus +
               '}';
    }
}