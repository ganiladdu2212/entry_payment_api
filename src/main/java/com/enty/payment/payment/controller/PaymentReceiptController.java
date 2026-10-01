package com.enty.payment.payment.controller;

import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.payment.response.PaymentReceiptResponse;
import com.enty.payment.payment.service.PaymentReceiptService;
import java.security.Principal;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/v1/users")
public class PaymentReceiptController {
    private final PaymentReceiptService service;
    public PaymentReceiptController(PaymentReceiptService service) { this.service = service; }

    @PostMapping(value = "/{userId}/payments/verify-receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerApiResponse<PaymentReceiptResponse>> verify(@PathVariable Long userId,
            @RequestPart("image") MultipartFile image, Principal principal) {
        return ResponseEntity.ok(CustomerApiResponse.success("payment receipt verified and marked as received",
                service.verify(userId, image, principal.getName())));
    }
}
