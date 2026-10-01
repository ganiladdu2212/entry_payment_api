package com.enty.payment.attendance.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.service.CustomerApiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
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
    @ExceptionHandler({DataIntegrityViolationException.class,CannotAcquireLockException.class})
    ResponseEntity<CustomerApiResponse<Void>> conflict(RuntimeException ex) {
        return ResponseEntity.status(409).body(CustomerApiResponse.failure(409,"Attendance request conflicts with another scan; please retry"));
    }
}
