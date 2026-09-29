package com.enty.payment.attendance.controller;

import com.enty.payment.attendance.request.CheckInCheckOutRequest;
import com.enty.payment.attendance.response.AttendanceResponse;
import com.enty.payment.attendance.service.AttendanceService;
import com.enty.payment.customer.response.CustomerApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/public/attendance")
public class AttendanceController {
    private final AttendanceService service;
    public AttendanceController(AttendanceService service) { this.service=service; }
    @PostMapping("/checkInCheckOut")
    public CustomerApiResponse<AttendanceResponse> scan(@RequestBody CheckInCheckOutRequest request) {
        AttendanceResponse result=service.scan(request);
        return CustomerApiResponse.success("CHECK_IN".equals(result.actionType())?"Check-in successful":"Check-out successful",result);
    }
}
