package com.enty.payment.user.entity;
import com.enty.payment.entity.BaseEntity;
import com.enty.payment.customer.entity.Customer;
import com.enty.payment.customer.subscription.entity.CustomerSubscription;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @Entity @Table(name="usersubscriptionlist")
public class UserPlanSnapshot extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="subscription_id")
    private Long subscriptionId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="cust_id", nullable=false)
    private Customer customer;
    @Column(nullable=false) private Boolean active;
    @Column(name="base_price_minor", nullable=false) private Long basePriceMinor;
    @Column(name="savings_minor", nullable=false) private Long savingsMinor;
    @Column(name="savings_percentage", nullable=false, precision=5, scale=2) private BigDecimal savingsPercentage;
    @Column(nullable=false, length=3) private String currency;
    @Column(name="duration_unit", nullable=false, length=20) private String durationUnit;
    @Column(name="duration_value", nullable=false) private Integer durationValue;
    @Column(name="plan_name", nullable=false, length=150) private String planName;
    @Column(name="plan_type", nullable=false, length=30) private String typeOfPlan;

    public static UserPlanSnapshot copyOf(CustomerSubscription plan) {
        UserPlanSnapshot copy = new UserPlanSnapshot();
        copy.customer=plan.getCustomer(); copy.active=plan.getActive();
        copy.basePriceMinor=plan.getBasePriceMinor(); copy.savingsMinor=plan.getSavingsMinor();
        copy.savingsPercentage=plan.getSavingsPercentage(); copy.currency=plan.getCurrency();
        copy.durationUnit=plan.getDurationUnit(); copy.durationValue=plan.getDurationValue();
        copy.planName=plan.getPlanName(); copy.typeOfPlan=plan.getTypeOfPlan();
        return copy;
    }
}
