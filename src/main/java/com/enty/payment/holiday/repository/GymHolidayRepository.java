package com.enty.payment.holiday.repository;

import com.enty.payment.holiday.entity.GymHoliday;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymHolidayRepository extends JpaRepository<GymHoliday,Long> {
    List<GymHoliday> findByCustomerCustIdOrderByHolidayDateAsc(Long custId);
    boolean existsByCustomerCustIdAndHolidayDate(Long custId, LocalDate holidayDate);
}
