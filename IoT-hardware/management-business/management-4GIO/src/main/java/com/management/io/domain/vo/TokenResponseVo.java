package com.management.io.domain.vo;

import lombok.Data;

@Data
public class TokenResponseVo {

    private int code;

    private String message;

    private TokenData data;

    @Data
    public class TokenData {
        private String token;
        private long expiration;

    }

}
