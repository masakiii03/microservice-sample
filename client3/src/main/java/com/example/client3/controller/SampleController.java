package com.example.client3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.client3.client.SampleFeignClient;

@RestController
@RefreshScope
public class SampleController {

    @Value("${server.port}")
    private String port;

    @Value("${cl3.value}")
    private String value;

    @Autowired
    private SampleFeignClient sampleFeignClient;

    @GetMapping("/sample/{second}")
    public String getSample(@RequestHeader("Authorization") String authorization, @PathVariable int second) {
        return sampleFeignClient.getClient4Port(authorization, port, second);
    }

    @GetMapping("/value")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> getValue() {
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

}
