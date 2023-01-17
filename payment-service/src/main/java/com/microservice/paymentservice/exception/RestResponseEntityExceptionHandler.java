package com.microservice.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customExceptionHandler(CustomException exception) {
        ErrorResponse errorResponse = new ErrorResponse()
                .builder()
                .errorCode(exception.getErrorCode())
                .errorMessage(exception.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(exception.getStatus()));
    }
}
