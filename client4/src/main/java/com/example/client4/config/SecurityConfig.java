package com.example.client4.config;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.client4.client.AuthenticationFeignClient;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationFeignClient authenticationFeignClient;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/actuator/*")).cors().and().authorizeHttpRequests()
                .requestMatchers("/actuator/*").permitAll()
                .requestMatchers("/**").access(new AuthorizationManager<RequestAuthorizationContext>() {
                    @Override
                    public AuthorizationDecision check(Supplier<Authentication> authorization,
                            RequestAuthorizationContext object) {
                        String token = object.getRequest().getHeader("Authorization");
                        String res = authenticationFeignClient.getAuthentication(token);

                        if (res != null && !(res.equals("invalid"))) {
                            return new AuthorizationDecision(true);
                        }

                        return new AuthorizationDecision(false);
                    }
                })
                .anyRequest().authenticated();

        return http.build();

    }

}
