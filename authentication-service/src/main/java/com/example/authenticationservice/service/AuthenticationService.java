package com.example.authenticationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.authenticationservice.config.JwtBuilder;

@Service
public class AuthenticationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtBuilder jwtBuilder;

    @Value("${github-oauth.client-id}")
    private String clientId;

    @Value("${github-oauth.client-secret}")
    private String clientSecret;

    public String getJwt(String code) {

        String url = "https://github.com/login/oauth/access_token?client_id=" + clientId + "&client_secret="
                + clientSecret + "&code=" + code;

        String res = restTemplate.postForObject(url, null, String.class);

        if (res != null && res.contains("access_token")) {
            String jwt = jwtBuilder.build();
            return jwt;
        }
        return "error";

    }
}
