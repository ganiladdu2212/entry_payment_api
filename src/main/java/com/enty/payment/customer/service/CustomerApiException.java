package com.enty.payment.customer.service;

import org.springframework.http.HttpStatus;

public class CustomerApiException extends RuntimeException {
    private final HttpStatus status;

    public CustomerApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
