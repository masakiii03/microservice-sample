package com.example.client2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.client2.entity.AccountEntity;
import com.example.client2.mapper.AccountMapper;
import com.example.client2.parameter.UpdateAccountParameter;

@SpringBootTest
class ServiceTests {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMapper accountMapper;

    @Test
    void searchAccountsTest() throws Exception {

        List<AccountEntity> results = accountService.searchAccounts();

        assertEquals(1, results.get(0).getAccountId());
        assertEquals("user1", results.get(0).getName());

        
        assertEquals(2, results.get(1).getAccountId());
        assertEquals("user2", results.get(1).getName());
        assertEquals(50000, results.get(1).getBalance());

        assertEquals(3, results.get(2).getAccountId());
        assertEquals("user3", results.get(2).getName());
        assertEquals(100, results.get(2).getBalance());

    }

    @Test
    void tryUpdateAccountTestSuccess() throws Exception {

        UpdateAccountParameter param = new UpdateAccountParameter();
        param.setAccountId(1);
        param.setPayAmount(300);

        accountService.tryUpdateAccount(param);

        Integer reservedBalance = accountMapper.searchReservedBalance(param.getAccountId());
        assertEquals(300, reservedBalance);

    }

    @Test
    void tryUpdateAccountTestFailure() throws Exception {

        UpdateAccountParameter param = new UpdateAccountParameter();
        param.setAccountId(1);
        param.setPayAmount(100000);

        try {
            accountService.tryUpdateAccount(param);
        } catch (Exception e) {
            assertEquals("error at try phase(client-2)", e.getMessage());
        }
    }

    @Test
    void confirmUpdateAccountTest() throws Exception {

        UpdateAccountParameter param = new UpdateAccountParameter();
        param.setAccountId(1);
        param.setPayAmount(100);

        accountService.confirmUpdateAccount(param);

        List<AccountEntity> results = accountService.searchAccounts();
        assertEquals(2900, results.get(0).getBalance());
    }

    @Test
    void confirmUpdateAccountTestFailure() throws Exception {

        UpdateAccountParameter param = new UpdateAccountParameter();
        param.setAccountId(1);
        param.setPayAmount(100000);

        try {
            accountService.confirmUpdateAccount(param);
        } catch (Exception e) {
            assertEquals("error at confirm phase(client-2)", e.getMessage());
        }
    }

    @Test
    void cancelUpdateAccountTest1() throws Exception {

        UpdateAccountParameter param = new UpdateAccountParameter();
        param.setAccountId(1);
        param.setPayAmount(100);

        // rollback処理
        accountService.cancelUpdateAccount(param);

        List<AccountEntity> results = accountMapper.searchAccounts();

        assertEquals(3100, results.get(0).getBalance());
    }

    @Test
    void cancelUpdateAccountTest2() throws Exception {

        UpdateAccountParameter param = new UpdateAccountParameter();
        param.setAccountId(1);
        param.setPayAmount(100);

        // reservedBalanceを更新
        accountService.tryUpdateAccount(param);
        Integer reservedBalance = accountMapper.searchReservedBalance(param.getAccountId());
        assertEquals(100, reservedBalance);

        // reservedBalanceを'0'に戻す処理
        accountService.cancelUpdateAccount(param);
        reservedBalance = accountMapper.searchReservedBalance(param.getAccountId());
        assertEquals(0, reservedBalance);
    }
}