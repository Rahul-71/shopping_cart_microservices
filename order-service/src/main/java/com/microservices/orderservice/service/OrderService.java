package com.microservices.orderservice.service;

import com.microservices.orderservice.model.OrderRequest;

public interface OrderService {

    long placeOrder(OrderRequest orderReq);

}
