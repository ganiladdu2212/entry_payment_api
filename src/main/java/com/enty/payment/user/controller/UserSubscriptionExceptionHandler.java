package com.enty.payment.user.controller;
import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.service.CustomerApiException;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes=UserSubscriptionController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserSubscriptionExceptionHandler {
    @ExceptionHandler(CustomerApiException.class)
    public ResponseEntity<CustomerApiResponse<Void>> business(CustomerApiException e) {
        return ResponseEntity.status(e.getStatus()).body(CustomerApiResponse.failure(e.getStatus().value(),e.getMessage()));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<CustomerApiResponse<Void>> invalid(Exception e) {
        return ResponseEntity.badRequest().body(CustomerApiResponse.failure(400,"Invalid request format or identifier"));
    }
}
