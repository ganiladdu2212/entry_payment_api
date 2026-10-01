package com.enty.payment.customer.response;

public record CustomerLoginResponse(
        CustomerResponse customer,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn) { }
