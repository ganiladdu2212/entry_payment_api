package com.enty.payment.customer.subscription.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.subscription.request.CustomerSubscriptionRequest;
import com.enty.payment.customer.subscription.response.CustomerSubscriptionResponse;
import com.enty.payment.customer.subscription.service.CustomerSubscriptionService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/subscriptions")
public class CustomerSubscriptionController {
    private final CustomerSubscriptionService service;

    public CustomerSubscriptionController(CustomerSubscriptionService service) {
        this.service = service;
    }

    @PostMapping("/createCustSubscription")
    public ResponseEntity<CustomerApiResponse<CustomerSubscriptionResponse>> createCustSubscription(
            @Valid @RequestBody CustomerSubscriptionRequest request, Principal principal) {
        CustomerSubscriptionResponse data = service.create(request, principal.getName());
        return ResponseEntity.ok(CustomerApiResponse.success("customer subscription created successfully", data));
    }

    @GetMapping("/getCustSubscriptions/{custId}")
    public ResponseEntity<CustomerApiResponse<List<CustomerSubscriptionResponse>>> getCustSubscriptions(
            @PathVariable Long custId, Principal principal) {
        List<CustomerSubscriptionResponse> data = service.getByCustomerId(custId, principal.getName());
        return ResponseEntity.ok(CustomerApiResponse.success("customer subscriptions fetched successfully", data));
    }
}
