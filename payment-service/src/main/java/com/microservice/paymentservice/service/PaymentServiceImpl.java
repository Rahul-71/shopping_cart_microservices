package com.microservice.paymentservice.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microservice.paymentservice.entity.TransactionDetails;
import com.microservice.paymentservice.exception.CustomException;
import com.microservice.paymentservice.model.PaymentMode;
import com.microservice.paymentservice.model.PaymentRequest;
import com.microservice.paymentservice.model.PaymentResponse;
import com.microservice.paymentservice.repository.TransactionDetailsRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private TransactionDetailsRepository transactionRepository;

    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording Payment Details : {}", paymentRequest);
        TransactionDetails transactionDetails = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .orderId(paymentRequest.getOrderId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();

        this.transactionRepository.save(transactionDetails);
        log.info("Transaction completed with transactionId: {}", transactionDetails.getId());

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(long orderId) {
        log.info("Getting payment details for orderId: {}", orderId);

        TransactionDetails transactionDetails = null;
        PaymentResponse paymentResponse = null;
        try {
            transactionDetails = this.transactionRepository.findByOrderId(orderId);
            log.info("Transaction details found, for orderId: {} => transactionDetails: {}", orderId,
                    transactionDetails);

            paymentResponse = PaymentResponse.builder()
                    .paymentId(transactionDetails.getId())
                    .status(transactionDetails.getPaymentStatus())
                    .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                    .amount(transactionDetails.getAmount())
                    .paymentDate(transactionDetails.getPaymentDate())
                    .orderId(transactionDetails.getOrderId())
                    .build();
        } catch (Exception e) {
            log.error("Transaction details not found, for orderId: {}", orderId);
            throw new CustomException("Transaction details not found for orderId: " + orderId, "NOT_FOUND", 404);
        }

        log.info("Payment details of orderId: {} fetched from server, returning back to controller.", orderId);
        return paymentResponse;
    }

}
 