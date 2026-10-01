package com.enty.payment.attendance.controller;

import com.enty.payment.attendance.response.AttendanceEventResponse;
import com.enty.payment.attendance.service.AttendanceService;
import com.enty.payment.customer.response.CustomerApiResponse;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/attendance")
public class AttendanceQueryController {
    private final AttendanceService service;
    public AttendanceQueryController(AttendanceService service) { this.service=service; }
    @GetMapping("/getCheckInCheckOut")
    public CustomerApiResponse<List<AttendanceEventResponse>> get(
            @RequestParam(required=false) Long userId, Principal principal) {
        return CustomerApiResponse.success("Attendance records fetched successfully",
            service.getEvents(userId,principal.getName()));
    }
}
