package com.substring.foodies.service;

import com.substring.foodies.dto.PaymentVerifyObject;

import java.util.Map;

public interface PaymentService {

    Map<String, Object> createPayment(String orderId);

    void verifyPayment(String orderId, PaymentVerifyObject paymentVerifyObject);
}
