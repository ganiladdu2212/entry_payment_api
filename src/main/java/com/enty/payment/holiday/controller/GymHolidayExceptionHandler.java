package com.enty.payment.holiday.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.service.CustomerApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes=GymHolidayController.class)
public class GymHolidayExceptionHandler {
    @ExceptionHandler(CustomerApiException.class)
    ResponseEntity<CustomerApiResponse<Void>> api(CustomerApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(CustomerApiResponse.failure(ex.getStatus().value(),ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<CustomerApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
        String message=ex.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField()+" "+error.getDefaultMessage()).orElse("Invalid holiday details");
        return ResponseEntity.badRequest().body(CustomerApiResponse.failure(400,message));
    }
}
