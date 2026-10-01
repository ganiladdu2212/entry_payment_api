package com.enty.payment.payment.response;

import com.enty.payment.payment.entity.PaymentReceipt;
import java.time.LocalDateTime;

public record PaymentReceiptResponse(Long paymentReceiptId, Long userId, Long subscriptionId,
        Long expectedAmount, Long capturedAmount, String transactionReference, String receiverUpiId,
        String paymentApp, LocalDateTime paymentDate, String verificationMode,
        String receiptImageUrl, String paymentStatus, LocalDateTime createdDate) {
    public static PaymentReceiptResponse from(PaymentReceipt receipt) {
        return new PaymentReceiptResponse(receipt.getPaymentReceiptId(), receipt.getUser().getUserId(),
                receipt.getUser().getSubscription().getSubscriptionId(), receipt.getExpectedAmount(),
                receipt.getCapturedAmount(), receipt.getTransactionReference(), receipt.getReceiverUpiId(),
                receipt.getPaymentApp(), receipt.getPaymentDate(), receipt.getVerificationMode(),
                receipt.getReceiptImageUrl(), receipt.getUser().getSubscription().getPaymentStatus(), receipt.getCreatedDate());
    }
}
