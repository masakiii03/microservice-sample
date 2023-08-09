package com.example.gatewaysample.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.gatewaysample.loadbalancer.CustomLoadBalancer;

@Configuration
@LoadBalancerClients({
        @LoadBalancerClient(name = "client-1", configuration = CustomLoadBalancerConfig.class)
})
public class CustomLoadBalancerConfig {

    @Bean
    // TODO: refreshできるよう追加実装
    public ReactorServiceInstanceLoadBalancer reactorServiceInstanceLoadBalancer(DiscoveryClient discoveryClient) {
        return new CustomLoadBalancer(discoveryClient, "client-1");
    }
}
