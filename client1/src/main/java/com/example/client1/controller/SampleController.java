package com.example.client1.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.client1.client.SampleFeignClient;
import com.example.client1.entity.ProductEntity;
import com.example.client1.parameter.BuyProductParameter;
import com.example.client1.service.ProductService;

@RestController
@RefreshScope
public class SampleController {

    @Value("${server.port}")
    private String port;

    @Value("${cl1.value}")
    private String value;

    @Autowired
    private ProductService productService;

    @Autowired
    private SampleFeignClient sampleFeignClient;

    private Logger logger = LoggerFactory.getLogger(SampleController.class);

    @GetMapping("/sample/{second}")
    public String getClient2(@RequestHeader("Authorization") String authorization, @PathVariable int second) {
        return sampleFeignClient.getClient2Port(authorization, port, second);
    }

    @GetMapping("/value")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> getValue() {
        logger.info("[client-1] getValue() called.");
        return new ResponseEntity<>(value + "(" + port + ")", HttpStatus.OK);
    }
    
    /**
     * プロダクト情報を検索する
     * @return プロダクト情報のリスト
     */
    @GetMapping("/products")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<ProductEntity>> searchProducts() {
        
        List<ProductEntity> results = productService.searchProducts();
        return new ResponseEntity<>(results, HttpStatus.OK);
        
    }
    
    /**
     * プロダクト購入処理をおこなう
     * @param param accountId, productId, quantity
     * @return "succeed"
     * @throws Exception
     */
    @PostMapping("/products")
    public ResponseEntity<String> buyProduct(@RequestHeader("Authorization") String authorization, @RequestBody BuyProductParameter param) throws Exception {
        
        try {
            String result = productService.buyProduct(authorization, param);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
}
