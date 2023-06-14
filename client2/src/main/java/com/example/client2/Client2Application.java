package com.example.client2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Client2Application {

	public static void main(String[] args) {
		SpringApplication.run(Client2Application.class, args);
	}

	@Value("${server.port}")
	private String port;

	@GetMapping("/get_port/{second}")
	public String getPort(@RequestHeader("Authorization") String authorization,
			@RequestParam("client1Port") String client1Port, @PathVariable int second) {
		try {
			Thread.sleep(second * 1000);
			return "success from client-2(port: " + port + ") via client-1(port: " + client1Port + ")";
		} catch (Exception e) {
			return "exception";
		}
	}

}
