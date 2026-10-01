package com.enty.payment.payment.entity;

import com.enty.payment.entity.BaseEntity;
import com.enty.payment.user.entity.MemberUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name = "payment_receipts", uniqueConstraints = @UniqueConstraint(name = "uk_payment_receipt_transaction", columnNames = "transaction_reference"))
public class PaymentReceipt extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "payment_receipt_id") private Long paymentReceiptId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private MemberUser user;
    @Column(name = "expected_amount", nullable = false) private Long expectedAmount;
    @Column(name = "captured_amount", nullable = false) private Long capturedAmount;
    @Column(name = "transaction_reference", nullable = false, length = 100) private String transactionReference;
    @Column(name = "receiver_upi_id", length = 150) private String receiverUpiId;
    @Column(name = "payment_app", length = 40) private String paymentApp;
    @Column(name = "payment_date") private LocalDateTime paymentDate;
    @Column(name = "verification_mode", nullable = false, length = 30) private String verificationMode;
    @Column(name = "receipt_image_url", nullable = false, length = 500) private String receiptImageUrl;
    @Lob @Column(name = "ocr_text", nullable = false, columnDefinition = "TEXT") private String ocrText;
}
