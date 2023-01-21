package com.microservices.orderservice.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.microservices.orderservice.exception.CustomException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "PRODUCT-SERVICE/product")
public interface ProductService {

    @PutMapping("/reduceQuantity/{productId}")
    ResponseEntity<Void> reduceQuantity(@PathVariable long productId, @RequestParam long quantity);

    default ResponseEntity<Void> fallback(Exception e) {
        System.out.println("fallback method invoked for productService");
        throw new CustomException("Product Service is not available !", "UNAVAILABLE", 500);
    }
}
