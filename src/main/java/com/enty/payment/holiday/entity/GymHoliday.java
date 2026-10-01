package com.enty.payment.holiday.entity;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name="gym_holidays", uniqueConstraints={
    @UniqueConstraint(name="uk_gym_holiday_customer_date", columnNames={"cust_id","holiday_date"})
})
public class GymHoliday extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="holiday_id") private Long holidayId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="cust_id", nullable=false) private Customer customer;
    @Column(name="holiday_date", nullable=false) private LocalDate holidayDate;
    @Column(nullable=false, length=250) private String purpose;
}
