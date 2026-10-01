package com.enty.payment.customer.repository;

import com.enty.payment.customer.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customer c where c.custId = :custId")
    Optional<Customer> findByIdForUpdate(Long custId);
    Optional<Customer> findByEmailIgnoreCase(String email);
    Optional<Customer> findByMobileNumber(String mobileNumber);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMobileNumber(String mobileNumber);
    boolean existsByEmailIgnoreCaseAndCustIdNot(String email, Long custId);
    boolean existsByMobileNumberAndCustIdNot(String mobileNumber, Long custId);
}
