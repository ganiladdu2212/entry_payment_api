package com.enty.payment.attendance.request;

public record CheckInCheckOutRequest(Long custId, String deviceUniqueId, String mobileNumber, String pin) {}
