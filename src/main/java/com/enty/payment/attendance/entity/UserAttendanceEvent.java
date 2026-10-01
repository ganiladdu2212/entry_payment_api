package com.enty.payment.attendance.entity;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.entity.BaseEntity;
import com.enty.payment.user.entity.MemberUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name="user_attendance_events", indexes={
    @Index(name="idx_attendance_user_date", columnList="user_id,created_date"),
    @Index(name="idx_attendance_customer_date", columnList="cust_id,created_date")
})
public class UserAttendanceEvent extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="attendance_event_id") private Long attendanceEventId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="cust_id", nullable=false)
    private Customer customer;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false)
    private MemberUser user;
    @Column(name="user_name", nullable=false, length=150) private String userName;
    @Column(name="mobile_number", nullable=false, length=20) private String mobileNumber;
    @Column(name="device_unique_id", nullable=false, length=100) private String deviceUniqueId;
    @Column(name="action_type", nullable=false, length=10) private String actionType;
}
