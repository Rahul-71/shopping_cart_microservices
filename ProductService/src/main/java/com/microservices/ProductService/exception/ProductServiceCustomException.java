package com.microservices.ProductService.exception;

import lombok.Data;

@Data
public class ProductServiceCustomException extends RuntimeException {
    private String errorCode;

    public ProductServiceCustomException(String arg0, String errorCode) {
        super(arg0);
        this.errorCode = errorCode;
    }

}
