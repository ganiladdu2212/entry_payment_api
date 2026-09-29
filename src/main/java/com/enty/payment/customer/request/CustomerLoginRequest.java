package com.enty.payment.customer.request;

public record CustomerLoginRequest(String mobileNumber, String email, String pwd) { }
