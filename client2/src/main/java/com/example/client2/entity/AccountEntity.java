package com.example.client2.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("ACCOUNT")
public class AccountEntity {

    @TableId(value = "account_id")
    private Integer accountId;
    
    private String name;

    private Integer balance;

    private Integer reservedBalance;

    public Integer getAccountId() {
        return this.accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBalance() {
        return this.balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getReservedBalance() {
        return this.reservedBalance;
    }

    public void setReservedBalance(Integer reservedBalance) {
        this.reservedBalance = reservedBalance;
    }
    
}
