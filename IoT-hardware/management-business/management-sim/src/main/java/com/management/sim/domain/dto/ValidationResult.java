package com.management.sim.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationResult {
    private boolean valid = true;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (!errors.isEmpty()) {
            sb.append("错误：").append(String.join("；", errors));
        }
        if (!warnings.isEmpty()) {
            if (sb.length() > 0) sb.append("；");
            sb.append("警告：").append(String.join("；", warnings));
        }
        return sb.toString();
    }
}