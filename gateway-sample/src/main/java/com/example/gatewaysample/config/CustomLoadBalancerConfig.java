package com.example.gatewaysample.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.gatewaysample.loadbalancer.CustomLoadBalancer;

@Configuration
@LoadBalancerClients({
        @LoadBalancerClient(name = "CustomLB", configuration = CustomLoadBalancerConfig.class)
})
public class CustomLoadBalancerConfig {

    @Bean
    @RefreshScope
    public ReactorLoadBalancer<ServiceInstance> reactorLoadBalancer(DiscoveryClient discoveryClient) {
        return new CustomLoadBalancer(discoveryClient);
    }
}
