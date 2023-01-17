package com.microservices.orderservice.service;

import com.microservices.orderservice.model.OrderRequest;
import com.microservices.orderservice.model.OrderResponse;

public interface OrderService {

    long placeOrder(OrderRequest orderReq);

    OrderResponse getOrderDetails(long orderId);

}
