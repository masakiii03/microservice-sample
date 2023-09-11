package com.example.client1.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.client1.entity.ProductEntity;
import com.example.client1.parameter.BuyProductParameter;

@Mapper
public interface ProductMapper extends BaseMapper<ProductEntity> {

    public List<ProductEntity> searchProducts(); // プロダクト情報の全件検索
    public Integer searchQuantity(Integer productId); // 在庫数の検索
    public Integer searchReservedQuantity(Integer productId); // 購入予定在庫数の検索
    public Integer searchPrice(Integer productId); // 値段の検索
    public void tryUpdateProduct(@Param("param") BuyProductParameter param); // プロダクト情報更新のtryフェーズ
    public void confirmUpdateProduct(@Param("param") BuyProductParameter param); // プロダクト情報更新のconfirmフェーズ
    public void cancelUpdateProduct(@Param("param") BuyProductParameter param); // プロダクト情報更新のcancelフェーズ
    public void rollbackUpdateProduct(@Param("param") BuyProductParameter param); // プロダクト情報更新後のロールバック処理
}
