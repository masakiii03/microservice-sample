package com.example.client3.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authentication-service")
public interface AuthenticationFeignClient {

    @GetMapping("/authentication")
    public String getAuthentication(@RequestHeader("Authorization") String token);

}
