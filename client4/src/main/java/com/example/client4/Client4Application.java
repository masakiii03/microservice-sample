package com.example.client4;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Client4Application {

	public static void main(String[] args) {
		SpringApplication.run(Client4Application.class, args);
	}

	@Value("${server.port}")
	private String port;

	@GetMapping("/get_port/{second}")
	public String getPort(@RequestParam("client3Port") String client3Port, @PathVariable int second) {
		try {
			Thread.sleep(second * 1000);
			return "success from client-4(port: " + port + ") via client-3(port: " + client3Port + ")";
		} catch (Exception e) {
			return "exception";
		}
	}

}
