package com.example.gatewaysample.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.http.HttpHeaders;

import reactor.core.publisher.Mono;

/**
 * カナリアリリースを実現するカスタムロードバランサー
 */
public class CustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private Logger logger = LoggerFactory.getLogger(CustomLoadBalancer.class);

    // サービス問わずに新バージョンのweightの設定
    @Value("${gw.new-version-weight}")
    private int newVersionWeight;

    private DiscoveryClient discoveryClient;

    private Random random;

    public CustomLoadBalancer(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.random = new Random();
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {

        // リクエストヘッダーからServiceNameを取得
        DefaultRequestContext rc = (DefaultRequestContext) request.getContext();
        RequestData rd = (RequestData) rc.getClientRequest();
        HttpHeaders headers = rd.getHeaders();
        String serviceName = headers.get("ServiceName").get(0);

        logger.info("target ServiceName: {}", serviceName);

        // サービス名から全てのインスタンスを取得
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        // ルーティングするインスタンスを取得
        ServiceInstance instance = getTargetInstance(instances);

        Response<ServiceInstance> response = new DefaultResponse(instance);
        return Mono.just(response);
    }

    /**
     * ルーティングするインスタンスを取得(metadata.versionとweightによるcanary対応)
     * 
     * @param instances インスタンスリスト
     * @return ターゲットインスタンス
     */
    private ServiceInstance getTargetInstance(List<ServiceInstance> instances) {

        logger.info("newVersionWeight: {}", newVersionWeight);
        instances.forEach(instance -> logger.info("metadata: {}", instance.getMetadata()));

        // バージョン情報を取得
        List<Integer> versionList = new ArrayList<>();
        instances.forEach(instance -> versionList.add(Integer.parseInt(instance.getMetadata().get("version"))));
        String currentVersion = Collections.min(versionList).toString();
        String newVersion = Collections.max(versionList).toString();

        logger.info("currentVersion: {}", currentVersion);
        logger.info("newVersion: {}", newVersion);

        int num = (int) (Math.random() * 100) + 1;
        logger.info("num: {}", num);
        logger.info(num <= newVersionWeight ? "call new version" : "call current version");

        // 特定バージョンの全てのインスタンスを取得
        List<ServiceInstance> targetInstances = instances.stream()
                .filter(instance -> instance.getMetadata().get("version")
                        .equals(num <= newVersionWeight ? newVersion : currentVersion))
                .collect(Collectors.toList());

        // 特定バージョンのインスタンスリストからランダムにインスタンスを選択
        return targetInstances.isEmpty() ? null : targetInstances.get(random.nextInt(targetInstances.size()));

    }
}
