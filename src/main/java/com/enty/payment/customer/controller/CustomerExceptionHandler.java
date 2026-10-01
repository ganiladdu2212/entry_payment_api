package com.enty.payment.customer.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.service.CustomerApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CustomerController.class)
public class CustomerExceptionHandler {
    @ExceptionHandler(CustomerApiException.class)
    ResponseEntity<CustomerApiResponse<Void>> customerError(CustomerApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(CustomerApiResponse.failure(ex.getStatus().value(), ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<CustomerApiResponse<Void>> duplicate(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CustomerApiResponse.failure(HttpStatus.CONFLICT.value(), "email or mobileNumber already registered"));
    }
}
