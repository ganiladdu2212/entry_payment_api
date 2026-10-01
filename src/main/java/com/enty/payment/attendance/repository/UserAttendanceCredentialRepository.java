package com.enty.payment.attendance.repository;

import com.enty.payment.attendance.entity.UserAttendanceCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAttendanceCredentialRepository extends JpaRepository<UserAttendanceCredential, Long> {
    Optional<UserAttendanceCredential> findByCustomerCustIdAndDeviceUniqueId(Long custId, String deviceUniqueId);
    Optional<UserAttendanceCredential> findByUserUserId(Long userId);
}
