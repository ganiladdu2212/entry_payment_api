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
        validateDuration(durationUnit, request.durationValue());
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
        CustomerSubscription saved = subscriptionRepository.save(subscription);
        if (isMonthlyBaseline(durationUnit, request.durationValue()))
            refreshSavings(request.custId(), typeOfPlan, saved);
        return CustomerSubscriptionResponse.from(saved);
    }

    @Transactional
    public CustomerSubscriptionResponse update(Long subscriptionId, CustomerSubscriptionRequest request,
            String authenticatedCustomerId) {
        assertCustomerAccess(request.custId(), authenticatedCustomerId);
        CustomerSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomerApiException(HttpStatus.NOT_FOUND, "subscription plan not found"));
        if (!subscription.getCustomer().getCustId().equals(request.custId()))
            throw new CustomerApiException(HttpStatus.FORBIDDEN, "subscription plan belongs to another customer");
        String planName = request.planName().trim();
        String typeOfPlan = normalizeTypeOfPlan(request.typeOfPlan());
        if (subscriptionRepository.existsByCustomerCustIdAndTypeOfPlanAndPlanNameIgnoreCaseAndSubscriptionIdNot(
                request.custId(), typeOfPlan, planName, subscriptionId))
            throw new CustomerApiException(HttpStatus.CONFLICT, "subscription plan already exists");
        String durationUnit = normalizeDurationUnit(request.durationUnit());
        validateDuration(durationUnit, request.durationValue());
        Savings savings = calculateSavings(request.custId(), typeOfPlan, durationUnit,
                request.durationValue(), request.basePriceMinor(), subscriptionId);
        subscription.setActive(request.active() == null || request.active());
        subscription.setBasePriceMinor(request.basePriceMinor());
        subscription.setSavingsMinor(savings.amount());
        subscription.setSavingsPercentage(savings.percentage());
        subscription.setCurrency(normalizeCurrency(request.currency()));
        subscription.setDurationUnit(durationUnit);
        subscription.setDurationValue(request.durationValue());
        subscription.setPlanName(planName);
        subscription.setTypeOfPlan(typeOfPlan);
        CustomerSubscription saved = subscriptionRepository.save(subscription);
        if (isMonthlyBaseline(durationUnit, request.durationValue()))
            refreshSavings(request.custId(), typeOfPlan, saved);
        return CustomerSubscriptionResponse.from(saved);
    }

    @Transactional
    public void delete(Long subscriptionId, String authenticatedCustomerId) {
        CustomerSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomerApiException(HttpStatus.NOT_FOUND, "subscription plan not found"));
        assertCustomerAccess(subscription.getCustomer().getCustId(), authenticatedCustomerId);
        subscriptionRepository.delete(subscription);
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
            case "DAY", "DAILY", "DAYS" -> "DAY";
            case "WEEK", "WEEKLY", "WEEKS" -> "WEEK";
            case "MONTH", "MONTHLY" -> "MONTHLY";
            case "QUARTER", "QUARTERLY", "QUOTERLY", "QUATERLY" -> "QUARTERLY";
            case "HALFYEAR", "HALFYEARLY", "HAFYEARLY" -> "HALF_YEARLY";
            case "YEAR" -> "YEAR";
            case "YEARLY" -> "YEARLY";
            default -> throw new CustomerApiException(HttpStatus.BAD_REQUEST,
                    "durationUnit must be DAY, WEEK, MONTH, YEAR, MONTHLY, QUARTERLY, HALF_YEARLY, or YEARLY");
        };
    }

    private void validateDuration(String unit, Integer value) {
        int maximum = switch (unit) {
            case "DAY" -> 3650;
            case "WEEK" -> 520;
            case "YEAR" -> 10;
            default -> 120;
        };
        if (value == null || value < 1 || value > maximum) {
            throw new CustomerApiException(HttpStatus.BAD_REQUEST,
                    "durationValue for " + unit + " must be between 1 and " + maximum);
        }
    }

    private Savings calculateSavings(Long custId, String typeOfPlan, String durationUnit,
            Integer durationValue, Long planPrice) {
        return calculateSavings(custId, typeOfPlan, durationUnit, durationValue, planPrice, null);
    }

    private Savings calculateSavings(Long custId, String typeOfPlan, String durationUnit,
            Integer durationValue, Long planPrice, Long updatingId) {
        if (isMonthlyBaseline(durationUnit, durationValue)) {
            return new Savings(0L, BigDecimal.ZERO.setScale(2));
        }

        CustomerSubscription monthlyPlan = subscriptionRepository
                .findFirstByCustomerCustIdAndTypeOfPlanAndDurationUnitAndDurationValueOrderBySubscriptionIdAsc(
                        custId, typeOfPlan, "MONTHLY", 1)
                .orElse(null);
        if (monthlyPlan == null)
            return new Savings(0L, BigDecimal.ZERO.setScale(2));
        if (updatingId != null && monthlyPlan.getSubscriptionId().equals(updatingId))
            return new Savings(0L, BigDecimal.ZERO.setScale(2));

        long regularPrice = regularPrice(monthlyPlan.getBasePriceMinor(), durationUnit, durationValue);
        long savingsAmount = Math.max(regularPrice - planPrice, 0L);
        BigDecimal percentage = regularPrice == 0 ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(savingsAmount).multiply(ONE_HUNDRED)
                        .divide(BigDecimal.valueOf(regularPrice), 2, RoundingMode.HALF_UP);
        return new Savings(savingsAmount, percentage);
    }

    private void refreshSavings(Long custId, String typeOfPlan, CustomerSubscription monthlyPlan) {
        for (CustomerSubscription plan : subscriptionRepository
                .findByCustomerCustIdAndTypeOfPlanOrderBySubscriptionIdAsc(custId, typeOfPlan)) {
            if (isMonthlyBaseline(plan.getDurationUnit(), plan.getDurationValue())) {
                plan.setSavingsMinor(0L);
                plan.setSavingsPercentage(BigDecimal.ZERO.setScale(2));
            } else {
                long regularPrice = regularPrice(monthlyPlan.getBasePriceMinor(),
                        plan.getDurationUnit(), plan.getDurationValue());
                long amount = Math.max(regularPrice - plan.getBasePriceMinor(), 0L);
                BigDecimal percentage = regularPrice == 0 ? BigDecimal.ZERO.setScale(2)
                        : BigDecimal.valueOf(amount).multiply(ONE_HUNDRED)
                                .divide(BigDecimal.valueOf(regularPrice), 2, RoundingMode.HALF_UP);
                plan.setSavingsMinor(amount);
                plan.setSavingsPercentage(percentage);
            }
        }
    }

    private boolean isMonthlyBaseline(String durationUnit, Integer durationValue) {
        return ("MONTH".equals(durationUnit) || "MONTHLY".equals(durationUnit)) && durationValue == 1;
    }

    private long regularPrice(long monthlyPrice, String durationUnit, int durationValue) {
        BigDecimal equivalentMonths = switch (durationUnit) {
            case "DAY" -> BigDecimal.valueOf(durationValue).divide(BigDecimal.valueOf(30), 8,
                    RoundingMode.HALF_UP);
            case "WEEK" -> BigDecimal.valueOf(durationValue).multiply(BigDecimal.valueOf(7))
                    .divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP);
            case "YEAR" -> BigDecimal.valueOf(durationValue).multiply(BigDecimal.valueOf(12));
            case "QUARTERLY" -> BigDecimal.valueOf(3);
            case "HALF_YEARLY" -> BigDecimal.valueOf(6);
            case "YEARLY" -> BigDecimal.valueOf(12);
            default -> BigDecimal.valueOf(durationValue);
        };
        return BigDecimal.valueOf(monthlyPrice).multiply(equivalentMonths)
                .setScale(0, RoundingMode.HALF_UP).longValueExact();
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
