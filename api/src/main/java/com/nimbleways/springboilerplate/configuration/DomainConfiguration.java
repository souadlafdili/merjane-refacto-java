package com.nimbleways.springboilerplate.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbleways.springboilerplate.domain.product.ProductProcessingPolicy;

@Configuration
public class DomainConfiguration {

    @Bean
    public ProductProcessingPolicy productProcessingPolicy() {
        return new ProductProcessingPolicy();
    }
}
