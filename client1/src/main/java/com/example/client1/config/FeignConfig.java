package com.example.client1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.client1.loadbalancer.CustomLoadBalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;

@Configuration
public class FeignConfig {

    private LoadBalancerClientFactory loadBalancerClientFactory;

    public FeignConfig(LoadBalancerClientFactory loadBalancerClientFactory) {
        this.loadBalancerClientFactory = loadBalancerClientFactory;
    }

    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer() {
        return new CustomLoadBalancer(loadBalancerClientFactory);
    }

}
