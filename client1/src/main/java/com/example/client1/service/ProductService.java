package com.example.client1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.client1.client.SampleFeignClient;
import com.example.client1.entity.ProductEntity;
import com.example.client1.mapper.ProductMapper;
import com.example.client1.parameter.BuyProductParameter;
import com.example.client1.parameter.UpdateAccountParameter;

@Service
public class ProductService {
    
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SampleFeignClient sampleFeignClient;

    /**
     * プロダクト情報を検索する
     * @return プロダクト情報のリスト
     */
    public List<ProductEntity> searchProducts() {

        return  productMapper.searchProducts();
    }

    /**
     * プロダクトの購入処理
     * プロダクト情報を更新(client-1)
     * 口座情報を更新(client-2)
     * @param authorization github_token
     * @param param accountId, productId, quantity
     * @return "succeed"
     * @throws Exception
     */
    public String buyProduct(String authorization, BuyProductParameter param) throws Exception {

        // 購入予定のプロダクトの値段を取得
        Integer price = productMapper.searchPrice(param.getProductId());
        Integer payAmount = price * param.getQuantity();

        // 口座情報を更新用(client-2)のパラメータ
        UpdateAccountParameter accountParam = new UpdateAccountParameter();
        accountParam.setAccountId(param.getAccountId());
        accountParam.setPayAmount(payAmount);

        try {
            // tryフェーズ(client-1)
            tryUpdateProduct(param);
        } catch (Exception e) {
            throw new Exception("在庫数が足りません。");
        }

        try {
            // tryフェーズ(client-2)
            sampleFeignClient.tryUpdateAccount(authorization, accountParam);
        } catch (Exception e) {
            cancelUpdateProduct(param);
            throw new Exception("残高が足りません。");
        }
        
        try {
            // 分散トランザクションの動作確認用
            // if (true) throw new Exception("エラー(confirm前)");

            // confirmフェーズ
            confirmUpdateProduct(param);
            // if (true) throw new Exception("エラー(client-1 confirm後)");

            sampleFeignClient.confirmUpdateAccount(authorization, accountParam);
            // if (true) throw new Exception("エラー(confirm後)");

            return "succeed";

        } catch (Exception e) {
            // cancelフェーズ
            cancelUpdateProduct(param);
            sampleFeignClient.cancelUpdateAccount(authorization, accountParam);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * プロダクト情報を更新する処理のtryフェース
     * @param param accountId, productId, quantity
     * @throws Exception
     */
    public void tryUpdateProduct(BuyProductParameter param) throws Exception {
        try {
            // 在庫数がマイナスにならないか確認
            isQuantityUpdatable(param);

            // reservedQuantityを更新して仮登録する
            productMapper.tryUpdateProduct(param);
        } catch (Exception e) {
            throw new Exception("error at try phase(client-1)");
        }
    }

    /**
     * プロダクト情報を更新する処理のconfirmフェーズ
     * @param param accountId, productId, quantity
     * @throws Exception
     */
    public void confirmUpdateProduct(BuyProductParameter param) throws Exception {
        try {
            // reservedQuantityを'0'に更新して、quantityを更新する
            productMapper.confirmUpdateProduct(param);
        } catch(Exception e) {
            throw new Exception("error at confirm phase(client-1)");
        }
    }

    /**
     * プロダクト情報を更新する処理のcancelフェーズ
     * @param param accountId, productId, quantity
     * @throws Exception
     */
    public void cancelUpdateProduct(BuyProductParameter param) throws Exception {
        try {
            if (getReservedQuantity(param.getProductId()) == 0) {
                // rollback処理(confirm済みのため)
                productMapper.rollbackUpdateProduct(param);
            } else {
                // reservedQuantityを'0'に戻す処理(confirm前のため)
                productMapper.cancelUpdateProduct(param);
            }
        } catch (Exception e) {
            throw new Exception("error at cancel phase(client-1)");
        }
    }

    /**
     * プロダクトの在庫数が更新可能か確認
     * @param param accountId, productId, quantity
     * @throws Exception
     */
    private void isQuantityUpdatable(BuyProductParameter param) throws Exception {
        Integer quantity = productMapper.searchQuantity(param.getProductId());
        if (quantity - param.getQuantity() < 0) {
            throw new Exception();
        }
    }

    /**
     * reservedQuantityの取得
     * @param productId
     * @return reservedQuantity
     * @throws Exception
     */
    private Integer getReservedQuantity(Integer productId) throws Exception {
        try {
            return productMapper.searchReservedQuantity(productId);
        } catch (Exception e) {
            throw new Exception();
        }
    }

}
