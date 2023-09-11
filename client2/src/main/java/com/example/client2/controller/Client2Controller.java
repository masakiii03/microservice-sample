package com.example.client2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.client2.entity.AccountEntity;
import com.example.client2.parameter.UpdateAccountParameter;
import com.example.client2.service.AccountService;

@RestController
public class Client2Controller {

    @Value("${server.port}")
	private String port;

    @Autowired
    private AccountService accountService;

	@GetMapping("/get_port/{second}")
	public String getPort(@RequestHeader("Authorization") String authorization,
			@RequestParam("client1Port") String client1Port, @PathVariable int second) {
		try {
			Thread.sleep(second * 1000);
			return "success from client-2(port: " + port + ") via client-1(port: " + client1Port + ")";
		} catch (Exception e) {
			return "exception";
		}
	}

    /**
     * 口座情報を検索する
     * @return 口座情報のリスト
     */
    @GetMapping("/accounts")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<AccountEntity>> searchAccounts() {

        List<AccountEntity> results = accountService.searchAccounts();
        return new ResponseEntity<>(results, HttpStatus.OK);

    }

    /**
     * 口座情報を更新する処理のtryフェーズ
     * @param authorization github_token
     * @param param accountId, payAmount
     * @return 処理結果
     * @throws Exception
     */
    @PostMapping("/try_accounts")
    public ResponseEntity<String> tryUpdateAccount(@RequestHeader("Authorization") String authorization, @RequestBody UpdateAccountParameter param) throws Exception {
        try {
            accountService.tryUpdateAccount(param);
            return new ResponseEntity<> ("succeed", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<> (e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 口座情報を更新する処理のconfirmフェーズ
     * @param authorization github_token
     * @param param accountId, payAmount
     * @return 処理結果
     * @throws Exception
     */
    @PostMapping("/confirm_accounts")
    public ResponseEntity<String> confirmUpdateAccount(@RequestHeader("Authorization") String authorization, @RequestBody UpdateAccountParameter param) throws Exception {
        try {
            accountService.confirmUpdateAccount(param);
            return new ResponseEntity<> ("succeed", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<> (e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 口座情報を更新する処理のcancelフェーズ
     * @param authorization github_token
     * @param param accountId, payAmount
     * @return 処理結果
     * @throws Exception
     */
    @PostMapping("/cancel_accounts")
    public ResponseEntity<String> cancelUpdateAccount(@RequestHeader("Authorization") String authorization, @RequestBody UpdateAccountParameter param) throws Exception {
        try {
            accountService.cancelUpdateAccount(param);
            return new ResponseEntity<>("succeed", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
}
