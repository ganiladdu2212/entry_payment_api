package com.enty.payment.customer.subscription.service;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.customer.repository.CustomerRepository;
import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.customer.subscription.entity.CustomerSubscription;
import com.enty.payment.customer.subscription.repository.CustomerSubscriptionRepository;
import com.enty.payment.customer.subscription.request.CustomerSubscriptionRequest;
import com.enty.payment.customer.subscription.response.CustomerSubscriptionResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerSubscriptionService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final CustomerSubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;

    public CustomerSubscriptionService(CustomerSubscriptionRepository subscriptionRepository,
            CustomerRepository customerRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerSubscriptionResponse create(CustomerSubscriptionRequest request, String authenticatedCustomerId) {
        assertCustomerAccess(request.custId(), authenticatedCustomerId);
        String planName = request.planName().trim();
        String typeOfPlan = normalizeTypeOfPlan(request.typeOfPlan());
        if (subscriptionRepository.existsByCustomerCustIdAndTypeOfPlanAndPlanNameIgnoreCase(
                request.custId(), typeOfPlan, planName)) {
            throw new CustomerApiException(HttpStatus.CONFLICT, "subscription plan already exists");
        }
        Customer customer = customerRepository.findById(request.custId())
                .orElseThrow(() -> new CustomerApiException(HttpStatus.NOT_FOUND, "customer not found"));
        String durationUnit = normalizeDurationUnit(request.durationUnit());
        Savings savings = calculateSavings(request.custId(), typeOfPlan, durationUnit,
                request.durationValue(), request.basePriceMinor());

        CustomerSubscription subscription = new CustomerSubscription();
        subscription.setCustomer(customer);
        subscription.setActive(request.active() == null || request.active());
        subscription.setBasePriceMinor(request.basePriceMinor());
        subscription.setSavingsMinor(savings.amount());
        subscription.setSavingsPercentage(savings.percentage());
        subscription.setCurrency(normalizeCurrency(request.currency()));
        subscription.setDurationUnit(durationUnit);
        subscription.setDurationValue(request.durationValue());
        subscription.setPlanName(planName);
        subscription.setTypeOfPlan(typeOfPlan);
        return CustomerSubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<CustomerSubscriptionResponse> getByCustomerId(Long custId, String authenticatedCustomerId) {
        assertCustomerAccess(custId, authenticatedCustomerId);
        if (!customerRepository.existsById(custId)) {
            throw new CustomerApiException(HttpStatus.NOT_FOUND, "customer not found");
        }
        return subscriptionRepository.findByCustomerCustIdOrderBySubscriptionIdAsc(custId)
                .stream().map(CustomerSubscriptionResponse::from).toList();
    }

    private String normalizeCurrency(String value) {
        String currency = value.trim().toUpperCase(Locale.ROOT);
        if (!currency.matches("^[A-Z]{3}$")) {
            throw new CustomerApiException(HttpStatus.BAD_REQUEST, "currency must be a 3-letter code such as INR");
        }
        return currency;
    }

    private String normalizeDurationUnit(String value) {
        String unit = value.trim().replace("-", "").replace("_", "").replace(" ", "")
                .toUpperCase(Locale.ROOT);
        return switch (unit) {
            case "MONTH", "MONTHLY" -> "MONTHLY";
            case "QUARTER", "QUARTERLY", "QUOTERLY", "QUATERLY" -> "QUARTERLY";
            case "HALFYEAR", "HALFYEARLY", "HAFYEARLY" -> "HALF_YEARLY";
            case "YEAR", "YEARLY" -> "YEARLY";
            default -> value.trim();
        };
    }

    private Savings calculateSavings(Long custId, String typeOfPlan, String durationUnit,
            Integer durationValue, Long planPrice) {
        int months = effectiveMonths(durationUnit, durationValue);
        if (months == 1) {
            return new Savings(0L, BigDecimal.ZERO.setScale(2));
        }

        CustomerSubscription monthlyPlan = subscriptionRepository
                .findFirstByCustomerCustIdAndTypeOfPlanAndDurationUnitAndDurationValueOrderBySubscriptionIdAsc(
                        custId, typeOfPlan, "MONTHLY", 1)
                .orElseThrow(() -> new CustomerApiException(HttpStatus.BAD_REQUEST,
                        "create the one-month plan first to calculate savings"));

        long regularPrice = Math.multiplyExact(monthlyPlan.getBasePriceMinor(), months);
        long savingsAmount = Math.max(regularPrice - planPrice, 0L);
        BigDecimal percentage = regularPrice == 0 ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(savingsAmount).multiply(ONE_HUNDRED)
                        .divide(BigDecimal.valueOf(regularPrice), 2, RoundingMode.HALF_UP);
        return new Savings(savingsAmount, percentage);
    }

    private int effectiveMonths(String durationUnit, Integer durationValue) {
        return switch (durationUnit) {
            case "QUARTERLY" -> 3;
            case "HALF_YEARLY" -> 6;
            case "YEARLY" -> 12;
            default -> durationValue;
        };
    }

    private String normalizeTypeOfPlan(String value) {
        String type = value.trim().replace("-", "").replace("_", "").replace(" ", "")
                .toUpperCase(Locale.ROOT);
        return switch (type) {
            case "MEMBERSHIP", "MEMBER" -> "MEMBERSHIP";
            case "TRAINING", "TRYING", "PERSONALTRAINING", "PERSONALTRYING" -> "PERSONAL_TRAINING";
            default -> value.trim().toUpperCase(Locale.ROOT);
        };
    }

    private void assertCustomerAccess(Long custId, String authenticatedCustomerId) {
        if (custId == null || authenticatedCustomerId == null
                || !custId.toString().equals(authenticatedCustomerId)) {
            throw new CustomerApiException(HttpStatus.FORBIDDEN,
                    "you cannot access another customer's subscriptions");
        }
    }

    private record Savings(Long amount, BigDecimal percentage) {
    }
}
