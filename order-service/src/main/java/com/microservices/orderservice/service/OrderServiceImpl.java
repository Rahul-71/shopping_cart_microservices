package com.microservices.orderservice.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.orderservice.entity.Order;
import com.microservices.orderservice.exception.CustomException;
import com.microservices.orderservice.external.client.PaymentService;
import com.microservices.orderservice.external.client.ProductService;
import com.microservices.orderservice.external.request.PaymentRequest;
import com.microservices.orderservice.external.response.ErrorResponse;
import com.microservices.orderservice.external.response.PaymentDetails;
import com.microservices.orderservice.model.OrderRequest;
import com.microservices.orderservice.model.OrderResponse;
import com.microservices.orderservice.model.ProductDetails;
import com.microservices.orderservice.repository.OrderRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class OrderServiceImpl implements OrderService {

        @Autowired
        private OrderRepository orderRepo;

        @Autowired
        private ProductService productService;

        @Autowired
        private PaymentService paymentService;

        @Autowired
        private RestTemplate restTemplate;

        @Override
        public long placeOrder(OrderRequest orderReq) {
                // Order Entity -> Save the data with Status Order Created
                // Product Entity -> Block Products (Reduce the Quantity)
                // Payment Service -> Payments -> If payment SUCCESS -> Complete, else CANCELLED

                // before placing order through orderservice, check if desired quantity is
                // present or not

                // performing RESTAPI call through productService
                log.info("Process for creating order started...");
                this.productService.reduceQuantity(orderReq.getProductId(), orderReq.getQuantity());

                log.info("Product quantity blocked for this order, proceeding to create order...");

                // placing order of requested quantity, for required product
                log.info("Placing Order Request: {}", orderReq);
                Order order = Order.builder()
                                .productId(orderReq.getProductId())
                                .amount(orderReq.getTotalAmount())
                                .quantity(orderReq.getQuantity())
                                .orderStatus("CREATED")
                                .orderDate(Instant.now())
                                .build();

                order = this.orderRepo.save(order);

                // order completed, need to complete the payment
                log.info("Calling Payment Service to complete the payment for orderId: {}", order.getId());

                PaymentRequest paymentRequest = PaymentRequest.builder()
                                .orderId(order.getId())
                                .paymentMode(orderReq.getPaymentMode())
                                .amount(orderReq.getTotalAmount())
                                .build();

                String orderStatus = null;

                try {
                        this.paymentService.doPayment(paymentRequest);
                        log.info("Payment done Successfully. Changing order status to PLACED");
                        orderStatus = "PLACED";
                } catch (Exception e) {
                        log.error("Error occured during payment. Changing order status to PAYMENT_FAILED.");
                        orderStatus = "PAYMENT_FAILED";
                }

                order.setOrderStatus(orderStatus);
                this.orderRepo.save(order);

                log.info("Order Placed successfully with orderid: {}", order.getId());

                return order.getId();
        }

        @Override
        public OrderResponse getOrderDetails(long orderId) {
                log.info("Getting order details for orderId: {}", orderId);
                Order orderDetails = this.orderRepo.findById(orderId).orElseThrow(
                                () -> new CustomException("Order not found for the orderId: " + orderId, "NOT_FOUND",
                                                404));

                log.info("Invoking PRODUCT SERVICE to fetch the productDetaisl for productId: {}",
                                orderDetails.getProductId());

                ProductDetails productDetails = this.restTemplate.getForObject(
                                "http://PRODUCT-SERVICE/product/" + orderDetails.getProductId(),
                                ProductDetails.class);

                productDetails.setQuantity(orderDetails.getQuantity());

                // fetching the payment details
                log.info("Getting payment information from the Payment Service");
                PaymentDetails paymentDetails = null;
                try {
                        paymentDetails = this.restTemplate.getForObject(
                                        "http://PAYMENT-SERVICE/payment/order/" + orderDetails.getId(),
                                        PaymentDetails.class);
                        log.info("Response for orderId : {} from Payment service : {}", orderId, paymentDetails);
                } catch (HttpStatusCodeException httpException) {
                        String err = httpException.getResponseBodyAsString();
                        ObjectMapper objectMapper = new ObjectMapper();
                        log.warn("Error occured during PaymentService API invokation. {}", err);
                        try {
                                ErrorResponse errResponse = objectMapper.readValue(err, ErrorResponse.class);
                                throw new CustomException(errResponse.getErrorMessage(), errResponse.getErrorCode(),
                                                httpException.getRawStatusCode());
                        } catch (JsonProcessingException e) {
                                log.error("Error occured while parsing PaymentService ErrorResponse: {}",
                                                e.getOriginalMessage());
                                throw new CustomException(e.getMessage(), e.getLocalizedMessage(), 500);
                        }
                }

                OrderResponse orderResponse = OrderResponse.builder()
                                .orderId(orderDetails.getId())
                                .amount(orderDetails.getAmount())
                                .orderStatus(orderDetails.getOrderStatus())
                                .orderDate(orderDetails.getOrderDate())
                                .productDetails(productDetails)
                                .paymentDetails(paymentDetails)
                                .build();

                log.info("Orderdetails fetched for orderId: {}, returing back the details.", orderId);
                return orderResponse;
        }

}
