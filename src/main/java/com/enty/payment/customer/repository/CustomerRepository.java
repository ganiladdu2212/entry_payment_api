package com.enty.payment.customer.repository;

import com.enty.payment.customer.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailIgnoreCase(String email);
    Optional<Customer> findByMobileNumber(String mobileNumber);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMobileNumber(String mobileNumber);
    boolean existsByEmailIgnoreCaseAndCustIdNot(String email, Long custId);
    boolean existsByMobileNumberAndCustIdNot(String mobileNumber, Long custId);
}
