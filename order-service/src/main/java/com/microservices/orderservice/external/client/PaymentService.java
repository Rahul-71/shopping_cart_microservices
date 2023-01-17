package com.microservices.orderservice.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.microservices.orderservice.external.request.PaymentRequest;

@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {

    // @PostMapping
    // ResponseEntity<Long> doPayment(@RequestBody PaymentService paymentService);
    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);


}
