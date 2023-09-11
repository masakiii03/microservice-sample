package com.example.client2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.client2.entity.AccountEntity;
import com.example.client2.mapper.AccountMapper;
import com.example.client2.parameter.UpdateAccountParameter;

@Service
public class AccountService {
    
    @Autowired
    private AccountMapper accountMapper;

    /**
     * 口座情報を検索する
     * @return 口座情報のリスト
     */
    public List<AccountEntity> searchAccounts() {
        return accountMapper.searchAccounts();
    }

    /**
     * 口座情報を更新する処理のtryフェーズ
     * @param param accountId, payAmount
     * @throws Exception
     */
    public void tryUpdateAccount(UpdateAccountParameter param) throws Exception {
        try {
            // 残高がマイナスにならないか確認
            isBalanceUpdatable(param);

            // reservedBalanceを更新して仮登録する
            accountMapper.tryUpdateAccount(param);
        } catch (Exception e) {
            throw new Exception("error at try phase(client-2)");
        }
    }

    /**
     * 口座情報を更新する処理のconfirmフェーズ
     * @param param accountId, payAmount
     * @throws Exception
     */
    public void confirmUpdateAccount(UpdateAccountParameter param) throws Exception {
        try {
            // reservedBalanceを'0'に更新して、balanceを更新する
            accountMapper.confirmUpdateAccount(param);
        } catch (Exception e) {
            throw new Exception("error at confirm phase(client-2)");
        }
    }

    /**
     * 口座情報を更新する処理のcancelフェーズ
     * @param param accountId, payAmount
     * @throws Exception
     */
    public void cancelUpdateAccount(UpdateAccountParameter param) throws Exception {
        try {
            if (getReservedBalance(param.getAccountId()) == 0) {
                // rollback処理(confirm済みのため)
                accountMapper.rollbackUpdateAccount(param);
            } else {
                // reservedBalanceを'0'に戻す処理(confirm前のため)
                accountMapper.cancelUpdateAccount(param);
            }
        } catch (Exception e) {
            throw new Exception("error at cancel phase(client-2)");
        }
    }
    
    /**
     * balanceが更新可能か確認
     * @param param accountId, payAmount
     * @throws Exception
     */
    private void isBalanceUpdatable(UpdateAccountParameter param) throws Exception {
        Integer balance = accountMapper.searchBalance(param.getAccountId());
        // 残高がマイナスになる場合はエラー
        if (balance - param.getPayAmount() < 0) {
            throw new Exception();
        }
    }
    
    /**
     * reservedBalanceの取得
     * @param accountId
     * @return reservedBalance
     * @throws Exception
     */
    private Integer getReservedBalance(Integer accountId) throws Exception {
        try {
            return accountMapper.searchReservedBalance(accountId);
        } catch (Exception e) {
            throw new Exception();
        }
    }

}
