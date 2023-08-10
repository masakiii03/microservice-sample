package com.example.client1.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.RetryableRequestContext;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * カナリアリリースを実現するカスタムロードバランサー
 */
public class CustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    @Value("${client-2.new-version-weight}")
    private int newVersionWeight;

    private Random random;

    private Logger logger = LoggerFactory.getLogger(CustomLoadBalancer.class);

    private ServiceInstanceListSupplier client2Supplier;

    private ServiceInstanceListSupplier authenticationSupplier;

    public CustomLoadBalancer(LoadBalancerClientFactory loadBalancerClientFactory) {

        this.client2Supplier = loadBalancerClientFactory
                .getLazyProvider("client-2", ServiceInstanceListSupplier.class)
                .getIfAvailable();

        this.authenticationSupplier = loadBalancerClientFactory
                .getLazyProvider("authentication-service", ServiceInstanceListSupplier.class)
                .getIfAvailable();

        this.random = new Random();

    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {

        RetryableRequestContext o = (RetryableRequestContext) request.getContext();
        String host = o.getClientRequest().getUrl().getHost();

        if (host.equals("authentication-service")) {
            return authenticationSupplier.get().next()
                    .map(this::getAuthenticationInstance)
                    .flatMap(serviceInstance -> Mono.just(new DefaultResponse(serviceInstance)));
        } else {
            return client2Supplier.get().next()
                    .map(this::getClient2Instance)
                    .flatMap(serviceInstance -> serviceInstance == null
                            ? Mono.empty()
                            : Mono.just(new DefaultResponse(serviceInstance)));
        }

    }

    /**
     * client-2のインスタンスを取得(metadata.versionとweightによるcanary対応)
     * 
     * @param instances
     * @return client-2のインスタンス
     */
    private ServiceInstance getClient2Instance(List<ServiceInstance> instances) {

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

        List<ServiceInstance> targetInstances = instances.stream()
                .filter(instance -> instance.getMetadata().get("version")
                        .equals(num <= newVersionWeight ? newVersion : currentVersion))
                .collect(Collectors.toList());

        return targetInstances.isEmpty() ? null : targetInstances.get(random.nextInt(targetInstances.size()));

    }

    /**
     * authentication-serviceのインスタンスを取得
     * 
     * @param instances
     * @return authentication-serviceのインスタンス
     */
    private ServiceInstance getAuthenticationInstance(List<ServiceInstance> instances) {
        return instances.get(random.nextInt(instances.size()));
    }
}
