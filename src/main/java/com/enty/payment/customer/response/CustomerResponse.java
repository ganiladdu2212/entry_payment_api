package com.enty.payment.customer.response;

import com.enty.payment.customer.entity.Customer;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long custId,
        String name,
        String orgName,
        String logo,
        String mobileNumber,
        String email,
        LocalDateTime createdDate) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getCustId(), customer.getName(), customer.getOrgName(),
                customer.getLogo(), customer.getMobileNumber(), customer.getEmail(), customer.getCreatedDate());
    }
}
