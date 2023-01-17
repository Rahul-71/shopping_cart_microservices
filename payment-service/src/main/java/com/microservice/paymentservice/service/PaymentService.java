package com.microservice.paymentservice.service;

import com.microservice.paymentservice.model.PaymentRequest;
import com.microservice.paymentservice.model.PaymentResponse;

public interface PaymentService {

    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(long orderId);
    
}
