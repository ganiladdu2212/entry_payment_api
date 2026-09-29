package com.enty.payment.attendance.response;

import com.enty.payment.attendance.entity.UserAttendanceEvent;
import java.time.LocalDateTime;

public record AttendanceEventResponse(Long attendanceEventId, Long custId, Long userId, String userName,
    String mobileNumber, String deviceUniqueId, String actionType, LocalDateTime createdDate) {
    public static AttendanceEventResponse from(UserAttendanceEvent event) {
        return new AttendanceEventResponse(event.getAttendanceEventId(), event.getCustomer().getCustId(),
            event.getUser().getUserId(), event.getUserName(), event.getMobileNumber(), event.getDeviceUniqueId(),
            event.getActionType(), event.getCreatedDate());
    }
}
