package com.enty.payment.payment.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.service.CustomerApiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = PaymentReceiptController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentReceiptExceptionHandler {
    @ExceptionHandler(CustomerApiException.class)
    ResponseEntity<CustomerApiResponse<Void>> apiError(CustomerApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(CustomerApiResponse.failure(ex.getStatus().value(), ex.getMessage()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<CustomerApiResponse<Void>> duplicate(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CustomerApiResponse.failure(409, "this transaction ID/UTR is already used"));
    }
}
