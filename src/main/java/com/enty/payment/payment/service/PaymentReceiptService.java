package com.enty.payment.payment.service;

import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.payment.entity.PaymentReceipt;
import com.enty.payment.payment.repository.PaymentReceiptRepository;
import com.enty.payment.payment.response.PaymentReceiptResponse;
import com.enty.payment.user.entity.*;
import com.enty.payment.user.repository.MemberUserRepository;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.LoadLibs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PaymentReceiptService {
    private static final Pattern AMOUNT = Pattern.compile("(?:₹|Rs\\.?|INR)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UPI = Pattern.compile("[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}");
    private static final Pattern TRANSACTION = Pattern.compile("(?:UTR|UPI\\s*(?:transaction)?\\s*ID|transaction\\s*ID|reference(?:\\s*ID)?)[^A-Za-z0-9]{0,8}([A-Za-z0-9-]{8,})", Pattern.CASE_INSENSITIVE);
    private final MemberUserRepository users;
    private final PaymentReceiptRepository receipts;
    private final Path directory;

    public PaymentReceiptService(MemberUserRepository users, PaymentReceiptRepository receipts,
            @Value("${app.storage.payment-receipt-directory}") String directory) {
        this.users = users; this.receipts = receipts; this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Transactional
    public PaymentReceiptResponse verify(Long userId, MultipartFile image, String principal) {
        MemberUser user = users.findById(userId).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "user not found"));
        if (!user.getCustomer().getCustId().toString().equals(principal))
            throw error(HttpStatus.FORBIDDEN, "cannot access another customer's payment");
        if (!"PENDING".equals(user.getSubscription().getPaymentStatus()))
            throw error(HttpStatus.CONFLICT, "only pending payments can be verified");
        validateImage(image);
        Path stored = store(image, userId);
        try {
            String text = read(stored.toFile());
            long expected = expectedAmount(user);
            long captured = extractAmount(text, expected);
            String reference = extractRequired(TRANSACTION, text, "transaction ID/UTR was not readable");
            if (receipts.existsByTransactionReferenceIgnoreCase(reference))
                throw error(HttpStatus.CONFLICT, "this transaction ID/UTR is already used");
            if (captured != expected)
                throw error(HttpStatus.BAD_REQUEST, "receipt amount " + captured + " does not match expected amount " + expected);
            String lower = text.toLowerCase(Locale.ROOT);
            if (!(lower.contains("success") || lower.contains("paid") || lower.contains("completed")))
                throw error(HttpStatus.BAD_REQUEST, "successful payment status was not readable");

            PaymentReceipt receipt = new PaymentReceipt();
            receipt.setUser(user); receipt.setExpectedAmount(expected); receipt.setCapturedAmount(captured);
            receipt.setTransactionReference(reference); receipt.setReceiverUpiId(extractOptional(UPI, text));
            receipt.setPaymentApp(detectApp(lower));
            receipt.setVerificationMode("GYM_CONFIRMED_OCR");
            receipt.setReceiptImageUrl("/files/payment-receipts/" + stored.getFileName());
            receipt.setOcrText(text.length() > 10000 ? text.substring(0, 10000) : text);
            user.getSubscription().setPaymentStatus("RECEIVED");
            return PaymentReceiptResponse.from(receipts.save(receipt));
        } catch (CustomerApiException ex) {
            deleteQuietly(stored); throw ex;
        }
    }

    private String read(File file) {
        try {
            Tesseract engine = new Tesseract();
            engine.setDatapath(LoadLibs.extractTessResources("tessdata").getAbsolutePath());
            engine.setLanguage("eng");
            String text = engine.doOCR(file);
            if (text == null || text.isBlank()) throw error(HttpStatus.BAD_REQUEST, "receipt is unreadable; retake a clear photo");
            return text;
        } catch (TesseractException ex) { throw error(HttpStatus.UNPROCESSABLE_ENTITY, "receipt OCR failed; retake a clear photo"); }
    }

    private long expectedAmount(MemberUser user) {
        UserSubscription subscription = user.getSubscription();
        long total = price(subscription.getMembershipPlan()) + price(subscription.getTrainingPlan());
        BigDecimal discount = subscription.getDiscountValue();
        long reduction = "PERCENTAGE".equals(subscription.getDiscountType())
                ? BigDecimal.valueOf(total).multiply(discount).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).longValue()
                : discount.setScale(0, RoundingMode.HALF_UP).longValue();
        return Math.max(0, total - Math.min(total, reduction));
    }
    private long price(UserPlanSnapshot plan) { return plan == null ? 0 : plan.getBasePriceMinor(); }
    private long extractAmount(String text, long expected) {
        Matcher matcher = AMOUNT.matcher(text); Long first = null;
        while (matcher.find()) {
            long value = new BigDecimal(matcher.group(1).replace(",", "")).setScale(0, RoundingMode.HALF_UP).longValue();
            if (value == expected) return value;
            if (first == null) first = value;
        }
        // OCR commonly drops the rupee symbol. Accept the exact expected amount as a standalone
        // number while still rejecting digits embedded inside dates, UTRs, or transaction IDs.
        String expectedPlain = Long.toString(expected);
        String expectedIndian = java.text.NumberFormat.getIntegerInstance(new Locale("en", "IN")).format(expected);
        Pattern expectedAmount = Pattern.compile("(?<![0-9])(?:" + Pattern.quote(expectedPlain) + "|"
                + Pattern.quote(expectedIndian) + ")(?:\\.00)?(?![0-9])");
        if (expectedAmount.matcher(text).find()) return expected;
        if (first == null) throw error(HttpStatus.BAD_REQUEST, "payment amount was not readable");
        return first;
    }
    private String extractRequired(Pattern pattern, String text, String message) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) throw error(HttpStatus.BAD_REQUEST, message);
        return matcher.group(1).toUpperCase(Locale.ROOT);
    }
    private String extractOptional(Pattern pattern, String text) { Matcher matcher = pattern.matcher(text); return matcher.find() ? matcher.group() : null; }
    private String detectApp(String text) { return text.contains("phonepe") ? "PHONEPE" : text.contains("google pay") || text.contains("gpay") ? "GOOGLE_PAY" : text.contains("paytm") ? "PAYTM" : "OTHER_UPI"; }
    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) throw error(HttpStatus.BAD_REQUEST, "receipt image is mandatory");
        String type = image.getContentType();
        if (type == null || !(type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/webp")))
            throw error(HttpStatus.BAD_REQUEST, "receipt must be JPEG, PNG, or WEBP");
    }
    private Path store(MultipartFile image, Long userId) {
        try {
            Files.createDirectories(directory);
            String contentType = Objects.requireNonNull(image.getContentType());
            String extension = contentType.contains("png") ? ".png" : contentType.contains("webp") ? ".webp" : ".jpg";
            Path target = directory.resolve("payment-" + userId + "-" + UUID.randomUUID() + extension).normalize();
            if (!target.startsWith(directory)) throw error(HttpStatus.BAD_REQUEST, "invalid receipt path");
            image.transferTo(target); return target;
        } catch (IOException ex) { throw error(HttpStatus.INTERNAL_SERVER_ERROR, "unable to store payment receipt"); }
    }
    private void deleteQuietly(Path path) { try { Files.deleteIfExists(path); } catch (IOException ignored) { } }
    private CustomerApiException error(HttpStatus status, String message) { return new CustomerApiException(status, message); }
}
