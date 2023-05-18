package com.example.client3.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = "client-4")
public interface SampleFeignClient {

    Logger logger = LoggerFactory.getLogger(SampleFeignClient.class);

    @GetMapping("/client4/get_port/{second}")
    @CircuitBreaker(name = "cbSample", fallbackMethod = "getFallback")
    public String getClient4Port(@RequestParam("client3Port") String client3Port, @PathVariable("second") int second);

    default String getFallback(String client3Port, int second, Exception e) {
        logger.info(e.getMessage());
        return "error from fallback (port: " + client3Port + ")";
    }
}
