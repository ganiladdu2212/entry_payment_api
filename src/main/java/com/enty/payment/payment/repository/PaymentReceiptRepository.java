package com.enty.payment.payment.repository;

import com.enty.payment.payment.entity.PaymentReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {
    boolean existsByTransactionReferenceIgnoreCase(String transactionReference);
}
