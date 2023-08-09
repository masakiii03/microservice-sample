package com.example.gatewaysample.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                .route(r -> r.path("/service/client-1/**")
                        .filters(f -> f.rewritePath("/service/client-1/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://client-1"))
                .build();
    }
}
