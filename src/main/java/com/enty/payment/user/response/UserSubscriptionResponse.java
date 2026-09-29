package com.enty.payment.user.response;
import com.enty.payment.user.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserSubscriptionResponse(Long userId, Long custId, String name, String mobileNumber,
        String countryCode, LocalDateTime createdDate, Subscription subscription) {
    public static UserSubscriptionResponse from(MemberUser user) {
        UserSubscription s=user.getSubscription();
        return new UserSubscriptionResponse(user.getUserId(), user.getCustomer().getCustId(), user.getName(),
            user.getMobileNumber(), user.getCountryCode(), user.getCreatedDate(),
            new Subscription(s.getSubscriptionId(), s.getDiscountType(), s.getDiscountValue(), s.getPaymentMode(),
                s.getCreatedDate(), Plan.from(s.getMembershipPlan()), Plan.from(s.getTrainingPlan()), s.getPaymentStatus()));
    }
    public record Subscription(Long subscriptionId, String discountType, BigDecimal discountValue,
        String paymentMode, LocalDateTime createdDate, Plan membershipPlan, Plan trainingPlan, String paymentStatus) {}
    public record Plan(Long subscriptionId, Long custId, Boolean active, Long basePriceMinor, String currency,
        String durationUnit, Integer durationValue, String planName, String typeOfPlan, Long savingsMinor,
        BigDecimal savingsPercentage, LocalDateTime createdDate) {
        static Plan from(UserPlanSnapshot p) {
            return p==null ? null : new Plan(p.getSubscriptionId(), p.getCustomer().getCustId(), p.getActive(),
                p.getBasePriceMinor(), p.getCurrency(), p.getDurationUnit(), p.getDurationValue(),
                p.getPlanName(), p.getTypeOfPlan(), p.getSavingsMinor(), p.getSavingsPercentage(), p.getCreatedDate());
        }
    }
}
