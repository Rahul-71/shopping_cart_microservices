package com.microservices.orderservice.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microservices.orderservice.entity.Order;
import com.microservices.orderservice.external.client.ProductService;
import com.microservices.orderservice.model.OrderRequest;
import com.microservices.orderservice.repository.OrderRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductService productService;

    @Override
    public long placeOrder(OrderRequest orderReq) {
        // Order Entity -> Save the data with Status Order Created
        // Product Entity -> Block Products (Reduce the Quantity)
        // Payment Service -> Payments -> If payment SUCCESS -> Complete, else CANCELLED

        // before placing order through orderservice, check if desired quantity is
        // present or not

        // performing RESTAPI call through productService
        productService.reduceQuantity(orderReq.getProductId(), orderReq.getQuantity());

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

        log.info("Order Placed successfully with orderid: {}", order.getId());

        return order.getId();
    }

}
