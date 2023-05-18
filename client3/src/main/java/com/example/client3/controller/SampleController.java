package com.example.client3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.client3.client.SampleFeignClient;

@RestController
public class SampleController {

    @Value("${server.port}")
    private String port;

    @Autowired
    private SampleFeignClient sampleFeignClient;

    @GetMapping("/sample/{second}")
    public String getSample(@PathVariable int second) {
        return sampleFeignClient.getClient4Port(port, second);
    }

}
