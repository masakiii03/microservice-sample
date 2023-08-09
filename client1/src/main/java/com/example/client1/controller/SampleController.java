package com.example.client1.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.example.client1.client.SampleFeignClient;

@RestController
@RefreshScope
public class SampleController {

    @Value("${server.port}")
    private String port;

    @Value("${cl1.value}")
    private String value;

    @Autowired
    private SampleFeignClient sampleFeignClient;

    private Logger logger = LoggerFactory.getLogger(SampleController.class);

    @GetMapping("/sample/{second}")
    public String getClient2(@RequestHeader("Authorization") String authorization, @PathVariable int second) {
        return sampleFeignClient.getClient2Port(authorization, port, second);
    }

    @GetMapping("/value")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> getValue() {
        logger.info("[client-1] getValue() called.");
        return new ResponseEntity<>(value + "(" + port + ")", HttpStatus.OK);
    }

}
