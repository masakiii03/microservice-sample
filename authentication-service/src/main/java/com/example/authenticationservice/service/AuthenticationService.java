package com.example.authenticationservice.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.authenticationservice.client.GithubFeignClient;

@Service
public class AuthenticationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GithubFeignClient githubFeignClient;

    @Value("${github-oauth.client-id}")
    private String clientId;

    @Value("${github-oauth.client-secret}")
    private String clientSecret;

    /**
     * 認可コードからアクセストークンの取得
     * 
     * @param code 認可コード
     * @return accessToken or "invalid"
     */
    public String getAccessToken(String code) {

        String url = "https://github.com/login/oauth/access_token?client_id=" + clientId + "&client_secret="
                + clientSecret + "&code=" + code;

        Map<String, String> res = restTemplate.postForObject(url, null, Map.class);

        return (res != null && res.get("access_token") != null) ? res.get("access_token") : "error";

    }

    /**
     * アクセストークンが有効かGithubに問い合わせる
     * 
     * @param accessToken
     * @return ユーザー情報 or "invalid"
     */
    public String checkAccessToken(String accessToken) {

        // バリデーションを追加する
        try {
            String res = githubFeignClient.getUserInfo(accessToken);
            return res;
        } catch (Exception e) {
            return "invalid";
        }

    }
}
