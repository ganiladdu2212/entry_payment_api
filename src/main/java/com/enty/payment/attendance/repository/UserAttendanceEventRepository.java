package com.enty.payment.attendance.repository;

import com.enty.payment.attendance.entity.UserAttendanceEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAttendanceEventRepository extends JpaRepository<UserAttendanceEvent, Long> {
    Optional<UserAttendanceEvent> findFirstByUserUserIdAndCreatedDateGreaterThanEqualAndCreatedDateLessThanOrderByCreatedDateDescAttendanceEventIdDesc(
        Long userId, LocalDateTime from, LocalDateTime until);
    java.util.List<UserAttendanceEvent> findByCustomerCustIdOrderByCreatedDateDescAttendanceEventIdDesc(Long custId);
    java.util.List<UserAttendanceEvent> findByCustomerCustIdAndUserUserIdOrderByCreatedDateDescAttendanceEventIdDesc(Long custId, Long userId);
}
