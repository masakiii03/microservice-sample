package com.example.client1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.cloud.context.config.annotation.RefreshScope;
// import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.client1.client.SampleFeignClient;

// import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
// import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@RestController
// @RefreshScope
public class SampleController {

    // @Autowired
    // private CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${server.port}")
    private String port;

    // @Value("${resilience4j.circuitbreaker.instances.cbSample.failureRateThreshold}")
    // private int failureRateThreshold;

    @Autowired
    private SampleFeignClient sampleFeignClient;

    @GetMapping("/sample/{second}")
    public String getSample(@PathVariable int second) {
        return sampleFeignClient.getClient2Port(port, second);
    }

    // @GetMapping("/cb_config")
    // public int getCbConfig() {
    // return failureRateThreshold;
    // }

    // @PostMapping("/actuator/refresh")
    // public void refreshConfig() {
    // // refresh configs
    // // contextRefresher.refresh();

    // circuitBreakerRegistry.getConfiguration("cbSample")
    // // 設定が存在する場合
    // .ifPresent(config -> {
    // CircuitBreakerConfig newConfig = CircuitBreakerConfig.from(config)
    // .failureRateThreshold(failureRateThreshold)
    // .build();
    // circuitBreakerRegistry.addConfiguration("cbSample", newConfig);
    // });
    // }

}
