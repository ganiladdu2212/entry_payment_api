package com.enty.payment.customer.response;

public record CustomerApiResponse<T>(boolean status, int statusCode, String message, T data) {
    public static <T> CustomerApiResponse<T> success(String message, T data) {
        return new CustomerApiResponse<>(true, 200, message, data);
    }

    public static <T> CustomerApiResponse<T> failure(int statusCode, String message) {
        return new CustomerApiResponse<>(false, statusCode, message, null);
    }
}
