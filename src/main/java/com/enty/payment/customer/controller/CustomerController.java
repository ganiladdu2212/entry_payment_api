package com.enty.payment.customer.controller;

import com.enty.payment.customer.request.CustomerLoginRequest;
import com.enty.payment.customer.request.CustomerRegistrationRequest;
import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.customer.response.CustomerLoginResponse;
import com.enty.payment.customer.response.CustomerResponse;
import com.enty.payment.customer.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping(value = "/registerCustomer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerApiResponse<CustomerResponse>> registerCustomer(
            @ModelAttribute CustomerRegistrationRequest request) {
        boolean create = "CREATE".equalsIgnoreCase(request.getTypeOfAction());
        CustomerResponse data = service.register(request);
        return ResponseEntity.ok(CustomerApiResponse.success(create ? "register success" : "update success", data));
    }

    @PostMapping("/loginCustomer")
    public ResponseEntity<CustomerApiResponse<CustomerLoginResponse>> loginCustomer(
            @RequestBody CustomerLoginRequest request) {
        return ResponseEntity.ok(CustomerApiResponse.success("login success", service.login(request)));
    }
}
