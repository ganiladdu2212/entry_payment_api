package com.enty.payment.user.entity;
import com.enty.payment.entity.BaseEntity;
import com.enty.payment.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name="users")
public class MemberUser extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="user_id")
    private Long userId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="cust_id", nullable=false)
    private Customer customer;
    @Column(nullable=false, length=150) private String name;
    @Column(name="mobile_number", nullable=false, length=20) private String mobileNumber;
    @Column(name="country_code", nullable=false, length=5) private String countryCode;
    @OneToOne(fetch=FetchType.LAZY, optional=false, cascade=CascadeType.PERSIST)
    @JoinColumn(name="subscription_id", nullable=false, unique=true)
    private UserSubscription subscription;
}
