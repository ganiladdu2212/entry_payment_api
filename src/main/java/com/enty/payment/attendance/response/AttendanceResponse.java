package com.enty.payment.attendance.response;

import java.time.LocalDateTime;

public record AttendanceResponse(Long attendanceEventId, Long userId,
    String name, String mobileNumber, String actionType, LocalDateTime eventTime, String nextAction,
    boolean deviceRegistered) {}
