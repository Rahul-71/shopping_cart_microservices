package com.microservice.paymentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.paymentservice.model.PaymentRequest;
import com.microservice.paymentservice.model.PaymentResponse;
import com.microservice.paymentservice.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest) {

        return new ResponseEntity<>(this.paymentService.doPayment(paymentRequest), HttpStatus.OK);

    }

    @GetMapping(path = "/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable long orderId) {
        return new ResponseEntity<>(this.paymentService.getPaymentDetailsByOrderId(orderId), HttpStatus.OK);

    }
}
