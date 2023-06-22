package com.example.gatewaysample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@RestController
@RefreshScope
@EnableFeignClients
public class GatewaySampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewaySampleApplication.class, args);
	}

	@Value("${gw.value}")
	private String value;

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/value")
	public ResponseEntity<String> getValue() {
		return new ResponseEntity<>(value, HttpStatus.OK);
	}

}
