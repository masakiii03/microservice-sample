package com.example.client1.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.example.client1.entity.ProductEntity;
import com.example.client1.mapper.ProductMapper;
import com.example.client1.parameter.BuyProductParameter;

@SpringBootTest
@TestPropertySource(properties = {
    "cl1.value=val",
    "client-2.new-version-weight=20"
})
class ServiceTests {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductService productService;

    @Test
    void searchProductsTest() throws Exception {

        List<ProductEntity> results = productService.searchProducts();
        
        assertEquals(1, results.get(0).getProductId());
        assertEquals("cheese", results.get(0).getProductName());
        assertEquals(10, results.get(0).getQuantity());
        assertEquals(300, results.get(0).getPrice());

        assertEquals(2, results.get(1).getProductId());
        assertEquals("book1", results.get(1).getProductName());
        assertEquals(3, results.get(1).getQuantity());
        assertEquals(1500, results.get(1).getPrice());
    }

    @Test
    void tryUpdateProductTest() throws Exception {

        BuyProductParameter param = new BuyProductParameter();
        param.setProductId(1);
        param.setQuantity(3);

        productService.tryUpdateProduct(param);

        List<ProductEntity> results = productService.searchProducts();

        assertEquals(3, results.get(0).getReservedQuantity());
    }

    @Test
    void tryUpdateProductTestFailure() throws Exception {

        BuyProductParameter param = new BuyProductParameter();
        param.setProductId(1);
        param.setQuantity(30);

        try {
            productService.tryUpdateProduct(param);
        } catch (Exception e) {
            assertEquals("error at try phase(client-1)", e.getMessage());
        }
    }

    @Test
    void confirmUpdateProductTest() throws Exception {

        BuyProductParameter param = new BuyProductParameter();
        param.setProductId(1);
        param.setQuantity(3);

        productService.confirmUpdateProduct(param);
        
        List<ProductEntity> results = productService.searchProducts();

        assertEquals(7, results.get(0).getQuantity());
    }

    @Test
    void confirmUpdateProductTestFailure() throws Exception {

        BuyProductParameter param = new BuyProductParameter();
        param.setProductId(1);
        param.setQuantity(300);
        
        try {
            productService.confirmUpdateProduct(param);
        } catch (Exception e) {
            assertEquals("error at confirm phase(client-1)", e.getMessage());
        }
    }

    @Test
    void cancelUpdateProductTest1() throws Exception {

        BuyProductParameter param = new BuyProductParameter();
        param.setProductId(1);
        param.setQuantity(3);

        // rollback処理
        productService.cancelUpdateProduct(param);

        List<ProductEntity> results = productService.searchProducts();

        assertEquals(13, results.get(0).getQuantity());
    }

    @Test
    void cancelUpdateProductTest2() throws Exception {

        BuyProductParameter param = new BuyProductParameter();
        param.setProductId(1);
        param.setQuantity(3);

        // reservedQuantityを更新
        productService.tryUpdateProduct(param);
        Integer reservedQuantity = productMapper.searchReservedQuantity(param.getProductId());
        assertEquals(3, reservedQuantity);
        
        // reservedQuantityを'0'に戻す処理
        productService.cancelUpdateProduct(param);
        reservedQuantity = productMapper.searchReservedQuantity(param.getProductId());
        assertEquals(0, reservedQuantity);
        
    }
}