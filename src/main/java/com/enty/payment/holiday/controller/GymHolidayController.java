package com.enty.payment.holiday.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.holiday.request.GymHolidayRequest;
import com.enty.payment.holiday.response.GymHolidayResponse;
import com.enty.payment.holiday.service.GymHolidayService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/customers/holidays")
public class GymHolidayController {
    private final GymHolidayService service;
    public GymHolidayController(GymHolidayService service) { this.service=service; }
    @GetMapping("/{custId}")
    public CustomerApiResponse<List<GymHolidayResponse>> get(@PathVariable Long custId,Principal principal) {
        return CustomerApiResponse.success("Gym holidays fetched successfully",service.get(custId,principal.getName()));
    }
    @PostMapping
    public CustomerApiResponse<GymHolidayResponse> create(@Valid @RequestBody GymHolidayRequest request,Principal principal) {
        return CustomerApiResponse.success("Gym holiday created successfully",service.create(request,principal.getName()));
    }
    @DeleteMapping("/{holidayId}")
    public CustomerApiResponse<Void> delete(@PathVariable Long holidayId,Principal principal) {
        service.delete(holidayId,principal.getName());
        return CustomerApiResponse.success("Gym holiday deleted successfully",null);
    }
}
