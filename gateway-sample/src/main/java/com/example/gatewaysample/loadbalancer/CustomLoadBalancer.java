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
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;

import reactor.core.publisher.Mono;

/**
 * カナリアリリースを実現するロードバランサー
 */
public class CustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private Logger logger = LoggerFactory.getLogger(CustomLoadBalancer.class);

    @Value("${client-1.new-version-weight}")
    private int newVersionWeight;

    private DiscoveryClient discoveryClient;

    private String serviceName;

    private Random random;

    public CustomLoadBalancer(DiscoveryClient discoveryClient, String serviceName) {
        this.discoveryClient = discoveryClient;
        this.serviceName = serviceName;
        this.random = new Random();
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        // インスタンスを選択
        ServiceInstance instance = getClient1Instance(instances);

        Response<ServiceInstance> response = new DefaultResponse(instance);
        return Mono.just(response);
    }

    /**
     * client-1のインスタンスを取得metadata.versionとweightによるcanary対応)
     * 
     * @param instances client-1のインスタンスリスト
     * @return client-1のターゲットインスタンス
     */
    private ServiceInstance getClient1Instance(List<ServiceInstance> instances) {

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
