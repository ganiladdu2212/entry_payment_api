package com.enty.payment.holiday.response;

import com.enty.payment.holiday.entity.GymHoliday;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GymHolidayResponse(Long holidayId, Long custId, LocalDate holidayDate,
    String purpose, LocalDateTime createdDate) {
    public static GymHolidayResponse from(GymHoliday holiday) {
        return new GymHolidayResponse(holiday.getHolidayId(),holiday.getCustomer().getCustId(),
            holiday.getHolidayDate(),holiday.getPurpose(),holiday.getCreatedDate());
    }
}
