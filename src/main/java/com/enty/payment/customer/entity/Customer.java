package com.enty.payment.customer.entity;

import com.enty.payment.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customers_mobile_number", columnNames = "mobile_number"),
        @UniqueConstraint(name = "uk_customers_email", columnNames = "email")
})
public class Customer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "org_name", length = 200)
    private String orgName;

    @Column(name = "logo", length = 500)
    private String logo;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "pwd", nullable = false, length = 100)
    private String password;
}
