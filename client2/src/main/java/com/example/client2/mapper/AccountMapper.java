package com.example.client2.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.client2.entity.AccountEntity;
import com.example.client2.parameter.UpdateAccountParameter;

@Mapper
public interface AccountMapper extends BaseMapper<AccountEntity> {
    
    public List<AccountEntity> searchAccounts(); // 口座情報の全件検索
    public Integer searchBalance(Integer accountId); // 口座残高の検索
    public Integer searchReservedBalance(Integer accountId); // 口座の支払予定額の検索
    public void tryUpdateAccount(@Param("param") UpdateAccountParameter param); // 口座情報更新のtryフェーズ
    public void confirmUpdateAccount(@Param("param") UpdateAccountParameter param); // 口座情報更新のconfirmフェーズ
    public void cancelUpdateAccount(@Param("param") UpdateAccountParameter param); // 口座情報更新のcancelフェーズ
    public void rollbackUpdateAccount(@Param("param") UpdateAccountParameter param); // 口座情報更新後のロールバック処理
}
