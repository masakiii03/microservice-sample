package com.example.client3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.client3.loadbalancer.CustomLoadBalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;

@Configuration
public class FeignConfig {

    private LoadBalancerClientFactory loadBalancerClientFactory;

    public FeignConfig(LoadBalancerClientFactory loadBalancerClientFactory) {
        this.loadBalancerClientFactory = loadBalancerClientFactory;
    }

    @Bean
    @RefreshScope
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer() {
        return new CustomLoadBalancer(loadBalancerClientFactory);
    }

}
