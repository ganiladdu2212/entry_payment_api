package com.enty.payment.customer.subscription.entity;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cust_subscriptions", uniqueConstraints = @UniqueConstraint(
        name = "uk_cust_subscription_plan", columnNames = {"cust_id", "plan_type", "plan_name"}))
public class CustomerSubscription extends BaseEntity {
    public CustomerSubscription() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cust_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_cust_subscriptions_customer"))
    private Customer customer;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "base_price_minor", nullable = false)
    private Long basePriceMinor;

    @Column(name = "savings_minor", nullable = false)
    private Long savingsMinor;

    @Column(name = "savings_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal savingsPercentage;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "duration_unit", nullable = false, length = 20)
    private String durationUnit;

    @Column(name = "duration_value", nullable = false)
    private Integer durationValue;

    @Column(name = "plan_name", nullable = false, length = 150)
    private String planName;

    @Column(name = "plan_type", nullable = false, length = 30)
    private String typeOfPlan;
}
