package com.example.authenticationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "github", url = "https://api.github.com")
public interface GithubFeignClient {

    @GetMapping("/user")
    public String getUserInfo(@RequestHeader("Authorization") String token);

}
