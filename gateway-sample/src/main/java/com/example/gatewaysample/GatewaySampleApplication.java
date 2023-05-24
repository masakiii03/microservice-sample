package com.example.gatewaysample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RefreshScope
public class GatewaySampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewaySampleApplication.class, args);
	}

	@Value("${gw.value}")
	private String value;

	@GetMapping("/value")
	public String getValue() {
		return value;
	}

}
