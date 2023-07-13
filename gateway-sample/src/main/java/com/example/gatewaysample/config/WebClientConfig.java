package com.example.gatewaysample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(Builder builder) {
        return builder.build();
    }
}
