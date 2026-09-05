package com.management.io.api;

import com.management.io.domain.vo.TokenResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class IOTokenMethod {

    @Value("${spring.4gio.loginName}")
    private String loginName;

    @Value("${spring.4gio.password}")
    private String password;


    /**
     * 获取用户登录token
     */
    public String getToken(){

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new HashMap<>();
        params.put("loginName", loginName);
        params.put("password", password);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(IoAPIComstant.tokenApi);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }

        TokenResponseVo response = restTemplate.getForObject(builder.toUriString(), TokenResponseVo.class);
        return response.getData().getToken();
    }


}
