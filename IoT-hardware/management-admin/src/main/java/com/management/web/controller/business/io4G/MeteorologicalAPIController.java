package com.management.web.controller.business.io4G;

import com.management.common.core.domain.AjaxResult;
import com.management.io.api.IoAPIComstant;
import com.management.io.domain.vo.TokenResponseVo;
import com.management.io.domain.vo.group.ApiResponse;
import com.management.io.service.IotMeteorologyRegisterDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * @Description: 气象第三方API接口
 * @Author: xlsky
 */
@RestController
@RequestMapping("/meteorology_api")
public class MeteorologicalAPIController {

    private static final Logger logger = LoggerFactory.getLogger(MeteorologicalAPIController.class);

    @Value("${spring.4gio.loginName}")
    private String loginName;

    @Value("${spring.4gio.password}")
    private String password;

    @Autowired
    private IotMeteorologyRegisterDataService iotMeteorologyRegisterDataService;

    /**
     * 获取气象站token
     * @return
     */
    @GetMapping("/meteorology_token")
    public AjaxResult getToken(){
        AjaxResult result = new AjaxResult();
        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> params = new HashMap<>();
            params.put("loginName", loginName);
            params.put("password", password);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(IoAPIComstant.tokenApi);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }

            TokenResponseVo response = restTemplate.getForObject(builder.toUriString(), TokenResponseVo.class);

            result.put("code", 200);
            result.put("data", response.getData().getToken());
            result.put("message", "获取token成功");
            logger.info("获取token成功");
            return result;
        }catch (Exception e){
            result.put("code", 500);
            result.put("message", "获取token失败: " + e.getMessage());
            logger.error("获取token失败", e);
            return result;
        }
    }

    /**
     * 获取气象站数据
     * @param token
     * @return
     */
    @GetMapping("/get_meteorology_api_data")
    public AjaxResult get_meteorology_api_data(String token){
        AjaxResult result = new AjaxResult();
        try {
            // 创建RestTemplate实例
            RestTemplate restTemplate = new RestTemplate();

            // 添加拦截器统一设置Token
            restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
                request.getHeaders().set("Authorization", token);
                return execution.execute(request, body);
            }));

            // 构建带参数的URL groupld为空时查询全部
            String url = UriComponentsBuilder.fromHttpUrl(IoAPIComstant.realApi)
                    .queryParam("groupld", "") // 请求参数
                    .toUriString();

            // 发送GET请求并将响应映射到实体类
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
            // 处理响应
            if (response != null && response.getCode() == 1000) {

            } else if (response != null) {
                System.out.println("请求失败: " + response.getMessage());
            }
            result.put("code", 200);
            result.put("data", response.getData());
            result.put("message", "获取气象站数据成功");
            logger.info("获取气象站数据成功");
            return result;
        }catch (Exception e){
            result.put("code", 500);
            result.put("message", "请求发生异常: " + e.getMessage());
            logger.error("请求发生异常", e);
            return result;
        }
    }

    /**
     * 更新气象站数据
     * @return
     */
    @GetMapping("/update_meteorology_api_data")
    public AjaxResult update_meteorology_api_data(){
        AjaxResult result = new AjaxResult();
        try {
            boolean success = iotMeteorologyRegisterDataService.updateMeteorologyApiData();
            if (success) {
                result.put("code", 200);
                result.put("message", "更新气象站数据成功");
                logger.info("更新气象站数据成功");
                return result;
            } else {
                result.put("code", 500);
                result.put("message", "更新气象站数据失败");
                logger.error("更新气象站数据失败");
                return result;
            }
        }catch (Exception e){
            result.put("code", 500);
            result.put("message", "更新气象站数据失败: " + e.getMessage());
            logger.error("更新气象站数据失败", e);
            return result;
        }
    }
}
