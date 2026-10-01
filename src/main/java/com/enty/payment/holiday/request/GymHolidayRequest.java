package com.enty.payment.holiday.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record GymHolidayRequest(@NotNull Long custId, @NotNull LocalDate holidayDate,
    @NotBlank @Size(max=250) String purpose) {}
