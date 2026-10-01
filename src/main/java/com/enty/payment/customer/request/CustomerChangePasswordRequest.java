package com.enty.payment.customer.request;

public record CustomerChangePasswordRequest(String mobileNumber, String oldPwd, String newPwd) { }
