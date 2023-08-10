package com.example.client3.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.client3.config.FeignConfig;

@FeignClient(name = "authentication-service", configuration = FeignConfig.class)
public interface AuthenticationFeignClient {

    @GetMapping("/authentication")
    public String getAuthentication(@RequestHeader("Authorization") String token);

}
