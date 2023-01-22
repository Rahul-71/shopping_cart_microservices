package com.microservices.orderservice;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import reactor.core.publisher.Flux;

public class TestServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    @Override
    public Flux<List<ServiceInstance>> get() {
        // ServiceInstance will be nothing but, the service which are registered in our
        // serviceRegistry
        // we are getting list of services, to remove the dependency of serviceRegistry
        // while performing
        // complete Integration testing of our application.
        List<ServiceInstance> result = new ArrayList<>();

        result.add(new DefaultServiceInstance(
                "PAYMENT-SERVICE",
                "PAYMENT-SERVICE",
                "localhost",
                8080,
                false));

        result.add(new DefaultServiceInstance(
                "PRODUCT-SERVICE",
                "PRODUCT-SERVICE",
                "localhost",
                8080,
                false));

        return Flux.just(result);
    }

    @Override
    public String getServiceId() {
        return null;
    }

}
