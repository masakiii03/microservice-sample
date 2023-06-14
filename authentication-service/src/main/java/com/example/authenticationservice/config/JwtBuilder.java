package com.example.authenticationservice.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class JwtBuilder {

    @Value("${github-oauth.user-id}")
    private String userId;

    private static final Long EXPIRATION_TIME = 1000L * 60L * 60L * 1L;

    public String build() {

        Date issuedAt = new Date();
        Date notBefore = new Date(issuedAt.getTime());
        Date expiresAt = new Date(issuedAt.getTime() + EXPIRATION_TIME);

        Algorithm algorithm = Algorithm.HMAC256("secret");

        String token = JWT.create()
                .withIssuer("github")
                .withSubject("GithubOAuth2")
                .withAudience(userId)
                .withIssuedAt(issuedAt)
                .withNotBefore(notBefore)
                .withExpiresAt(expiresAt)

                .sign(algorithm);

        return token;
    }
}