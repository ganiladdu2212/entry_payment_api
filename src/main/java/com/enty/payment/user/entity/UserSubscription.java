package com.enty.payment.user.entity;
import com.enty.payment.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @Entity @Table(name="usersubcription")
public class UserSubscription extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="subscription_id")
    private Long subscriptionId;
    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.PERSIST)
    @JoinColumn(name="membership_plan_id", unique=true) private UserPlanSnapshot membershipPlan;
    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.PERSIST)
    @JoinColumn(name="training_plan_id", unique=true) private UserPlanSnapshot trainingPlan;
    @Column(name="discount_type", nullable=false, length=20) private String discountType;
    @Column(name="discount_value", nullable=false, precision=12, scale=2) private BigDecimal discountValue;
    @Column(name="payment_mode", nullable=false, length=10) private String paymentMode;
    @Column(name="payment_status", length=20) private String paymentStatus;
}
