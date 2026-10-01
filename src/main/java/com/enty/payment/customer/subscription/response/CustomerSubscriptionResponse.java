package com.enty.payment.customer.subscription.response;

import com.enty.payment.customer.subscription.entity.CustomerSubscription;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerSubscriptionResponse(
        Long subscriptionId,
        Long custId,
        Boolean active,
        Long basePriceMinor,
        Long savingsMinor,
        BigDecimal savingsPercentage,
        String currency,
        String durationUnit,
        Integer durationValue,
        String planName,
        String typeOfPlan,
        LocalDateTime createdDate) {

    public static CustomerSubscriptionResponse from(CustomerSubscription subscription) {
        return new CustomerSubscriptionResponse(subscription.getSubscriptionId(), subscription.getCustomer().getCustId(),
                subscription.getActive(), subscription.getBasePriceMinor(), subscription.getSavingsMinor(),
                subscription.getSavingsPercentage(), subscription.getCurrency(),
                subscription.getDurationUnit(), subscription.getDurationValue(), subscription.getPlanName(),
                subscription.getTypeOfPlan(),
                subscription.getCreatedDate());
    }
}
