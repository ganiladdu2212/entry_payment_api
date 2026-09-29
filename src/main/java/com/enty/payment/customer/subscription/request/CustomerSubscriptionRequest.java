package com.enty.payment.customer.subscription.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerSubscriptionRequest(
        @NotNull(message = "custId is mandatory") Long custId,
        Boolean active,
        @NotNull(message = "basePriceMinor is mandatory")
        @Min(value = 0, message = "basePriceMinor cannot be negative") Long basePriceMinor,
        @NotBlank(message = "currency is mandatory")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter code") String currency,
        @NotBlank(message = "durationUnit is mandatory") String durationUnit,
        @NotNull(message = "durationValue is mandatory")
        @Min(value = 1, message = "durationValue must be at least 1") Integer durationValue,
        @NotBlank(message = "planName is mandatory")
        @Size(max = 150, message = "planName cannot exceed 150 characters") String planName,
        @NotBlank(message = "typeOfPlan is mandatory") String typeOfPlan) {
}
