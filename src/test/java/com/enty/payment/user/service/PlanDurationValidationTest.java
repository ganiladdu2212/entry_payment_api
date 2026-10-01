package com.enty.payment.user.service;

import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.user.entity.UserPlanSnapshot;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PlanDurationValidationTest {
    private final LocalDate start = LocalDate.of(2026, 9, 30);
    private UserPlanSnapshot plan(String unit, int value) {
        var plan = new UserPlanSnapshot();
        plan.setDurationUnit(unit);
        plan.setDurationValue(value);
        return plan;
    }
    @Test void rejectsLongerTraining() {
        assertThrows(CustomerApiException.class, () -> UserSubscriptionService.validatePlanDurations(plan("DAY", 1), plan("MONTH", 1), start));
    }
    @Test void acceptsEqualAndShorterTraining() {
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("MONTH", 1), plan("MONTH", 1), start));
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("MONTH", 1), plan("WEEK", 1), start));
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("YEAR", 1), plan("MONTH", 12), start));
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("WEEK", 1), plan("DAY", 7), start));
    }
    @Test void trainingRequiresMembershipButIsOptional() {
        assertThrows(CustomerApiException.class, () -> UserSubscriptionService.validatePlanDurations(null, plan("DAY", 1), start));
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("DAY", 1), null, start));
    }
    @Test void respectsCalendarMonthEnds() {
        var january = LocalDate.of(2026, 1, 31);
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("MONTH", 1), plan("DAY", 28), january));
        assertThrows(CustomerApiException.class, () -> UserSubscriptionService.validatePlanDurations(plan("MONTH", 1), plan("DAY", 29), january));
    }
    @Test void acceptsStoredPresetAndPluralUnits() {
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("MONTHLY", 1), plan("DAYS", 7), start));
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("QUARTERLY", 3), plan("MONTHS", 2), start));
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("HALF_YEARLY", 6), plan("WEEKS", 20), start));
        assertDoesNotThrow(() -> UserSubscriptionService.validatePlanDurations(plan("YEARLY", 12), plan("MONTHLY", 12), start));
    }
}
