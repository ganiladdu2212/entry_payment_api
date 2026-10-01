package com.enty.payment.customer.subscription.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.service.CustomerApiException;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CustomerSubscriptionController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomerSubscriptionExceptionHandler {
    @ExceptionHandler(CustomerApiException.class)
    ResponseEntity<CustomerApiResponse<Void>> subscriptionError(CustomerApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(CustomerApiResponse.failure(ex.getStatus().value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<CustomerApiResponse<Void>> validationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).filter(Objects::nonNull)
                .orElse("invalid request");
        return ResponseEntity.badRequest().body(CustomerApiResponse.failure(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<CustomerApiResponse<Void>> duplicate(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CustomerApiResponse.failure(HttpStatus.CONFLICT.value(), "subscription plan already exists"));
    }
}
