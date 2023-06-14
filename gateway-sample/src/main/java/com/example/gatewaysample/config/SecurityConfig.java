package com.example.gatewaysample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http)
            throws Exception {

        http.addFilterAfter(this::validateCustomHeader, SecurityWebFiltersOrder.AUTHENTICATION).csrf().disable()
                .authorizeExchange()
                .pathMatchers("/**").permitAll();

        return http.build();

    }

    private Mono<Void> validateCustomHeader(ServerWebExchange exchange, WebFilterChain chain) {
        String method = exchange.getRequest().getMethod().toString();
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token != null && token.contains("null") && !(method.equals("OPTIONS"))) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

}
