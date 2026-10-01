package com.enty.payment.attendance.entity;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.entity.BaseEntity;
import com.enty.payment.user.entity.MemberUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name="user_attendance_credentials", uniqueConstraints={
    @UniqueConstraint(name="uk_attendance_credential_user", columnNames={"user_id"}),
    @UniqueConstraint(name="uk_attendance_credential_mobile", columnNames={"cust_id","mobile_number"}),
    @UniqueConstraint(name="uk_attendance_credential_device", columnNames={"cust_id","device_unique_id"})
})
public class UserAttendanceCredential extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="credential_id") private Long credentialId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="cust_id", nullable=false)
    private Customer customer;
    @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false)
    private MemberUser user;
    @Column(name="mobile_number", nullable=false, length=20) private String mobileNumber;
    @Column(name="pin_hash", nullable=false, length=100) private String pinHash;
    @Column(name="device_unique_id", nullable=false, length=100) private String deviceUniqueId;
}
