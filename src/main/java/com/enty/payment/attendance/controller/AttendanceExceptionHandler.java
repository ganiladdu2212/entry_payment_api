package com.enty.payment.attendance.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.service.CustomerApiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes={AttendanceController.class,AttendanceQueryController.class}) @Order(Ordered.HIGHEST_PRECEDENCE)
public class AttendanceExceptionHandler {
    @ExceptionHandler(CustomerApiException.class)
    ResponseEntity<CustomerApiResponse<Void>> business(CustomerApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(CustomerApiResponse.failure(ex.getStatus().value(),ex.getMessage()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<CustomerApiResponse<Void>> invalid(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(CustomerApiResponse.failure(400,"Invalid request format"));
    }
}
