package com.enty.payment.customer.subscription.repository;

import com.enty.payment.customer.subscription.entity.CustomerSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSubscriptionRepository extends JpaRepository<CustomerSubscription, Long> {
    boolean existsByCustomerCustIdAndTypeOfPlanAndPlanNameIgnoreCase(
            Long custId, String typeOfPlan, String planName);
    boolean existsByCustomerCustIdAndTypeOfPlanAndPlanNameIgnoreCaseAndSubscriptionIdNot(
            Long custId, String typeOfPlan, String planName, Long subscriptionId);

    Optional<CustomerSubscription> findFirstByCustomerCustIdAndTypeOfPlanAndDurationUnitAndDurationValueOrderBySubscriptionIdAsc(
            Long custId, String typeOfPlan, String durationUnit, Integer durationValue);

    List<CustomerSubscription> findByCustomerCustIdOrderBySubscriptionIdAsc(Long custId);
    List<CustomerSubscription> findByCustomerCustIdAndTypeOfPlanOrderBySubscriptionIdAsc(Long custId, String typeOfPlan);
}
