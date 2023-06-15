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

        http.csrf().disable()
                .authorizeExchange()
                .pathMatchers("/**").permitAll().and()
                .addFilterAfter(this::validateCustomHeader, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();

    }

    private Mono<Void> validateCustomHeader(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().toString();
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        // "/authentication-service/jwt"は認証前のため許可
        // "*/actuator/refresh"はconfigのrefresh用に許可
        // methodが"OPTIONS"の場合は許可(カスタムヘッダーが含まれないため)
        if ((path.equals("/authentication-service/jwt") || path.endsWith("/actuator/refresh")
                || method.equals("OPTIONS"))
                || (token != null && !(token.contains("null")))) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

}
