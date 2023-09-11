package com.example.client1.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.client1.config.FeignConfig;
import com.example.client1.parameter.UpdateAccountParameter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = "client-2", configuration = FeignConfig.class)
public interface SampleFeignClient {

    Logger logger = LoggerFactory.getLogger(SampleFeignClient.class);

    @GetMapping("/client2/get_port/{second}")
    @CircuitBreaker(name = "cbSample", fallbackMethod = "getFallback")
    public String getClient2Port(@RequestHeader("Authorization") String authorization,
            @RequestParam("client1Port") String client1Port, @PathVariable("second") int second);

    default String getFallback(String authorization, String client1Port, int second, Exception e) {
        logger.info(e.getMessage());
        return "error from fallback (port: " + client1Port + ")";
    }
    
    /**
     * 口座情報を更新する処理のtryフェーズ
     * @param authorization github_token
     * @param param accountId, payAmount
     * @return 処理結果
     */
    @PostMapping("/client2/try_accounts")
    public ResponseEntity<String> tryUpdateAccount(@RequestHeader("Authorization") String authorization, @RequestBody UpdateAccountParameter param);
    
    /**
     * 口座情報を更新する処理のconfirmフェーズ
     * @param authorization github_token
     * @param param accountId, payAmount
     * @return 処理結果
     */
    @PostMapping("/client2/confirm_accounts")
    public ResponseEntity<String> confirmUpdateAccount(@RequestHeader("Authorization") String authorization, @RequestBody UpdateAccountParameter param);

    /**
     * 口座情報を更新する処理のcancelフェーズ
     * @param authorization github_token
     * @param param accountId, payAmount
     * @return 処理結果
     */
    @PostMapping("/client2/cancel_accounts")
    public ResponseEntity<String> cancelUpdateAccount(@RequestHeader("Authorization") String authorization, @RequestBody UpdateAccountParameter param);
}
