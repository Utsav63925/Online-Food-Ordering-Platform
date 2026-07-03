package com.substring.foodies.controller;

import com.substring.foodies.dto.PaymentVerifyObject;
import com.substring.foodies.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    // Create Razorpay Order
    @PostMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> createPayment(
            @PathVariable String orderId) {

        return ResponseEntity.ok(
                paymentService.createPayment(orderId)
        );
    }

    // Verify Payment
    @PostMapping("/verify/{orderId}")
    public ResponseEntity<Map<String, String>> verifyPayment(
            @PathVariable String orderId,
            @RequestBody PaymentVerifyObject dto) {

        paymentService.verifyPayment(orderId, dto);
        return ResponseEntity.ok(
                Map.of("message", "Payment verified successfully")
        );
    }
}
