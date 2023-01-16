package com.microservices.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.microservices.orderservice.external.decoder.CustomErrorDecoder;

import feign.codec.ErrorDecoder;

@Component
public class FeignConfig {

    @Bean
    ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
