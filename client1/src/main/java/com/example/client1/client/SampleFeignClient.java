package com.example.client1.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = "client-2")
public interface SampleFeignClient {

    Logger logger = LoggerFactory.getLogger(SampleFeignClient.class);

    @GetMapping("/client2/get_port/{second}")
    @CircuitBreaker(name = "cbSample", fallbackMethod = "getFallback")
    public String getClient2Port(@RequestParam("client1Port") String client1Port, @PathVariable("second") int second);

    default String getFallback(String client1Port, int second, Exception e) {
        logger.info(e.getMessage());
        return "error from fallback (port: " + client1Port + ")";
    }
}
