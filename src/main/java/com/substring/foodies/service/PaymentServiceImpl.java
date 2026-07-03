package com.substring.foodies.service;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.substring.foodies.dto.PaymentVerifyObject;
import com.substring.foodies.dto.enums.OrderStatus;
import com.substring.foodies.dto.enums.PaymentStatus;
import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.Order;
import com.substring.foodies.entity.User;
import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.OrderRepository;
import com.substring.foodies.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    @Autowired
    private UserRepository userRepository;

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Invalid session"));
    }

    private void validateOrder(Order order) {

        User loggedInUser = getLoggedInUser();

        boolean isOwner = loggedInUser.getId().equals(order.getUser().getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not authorized to access this order"
            );
        }
    }

    // ---------------- CREATE PAYMENT ----------------
    @Override
    public Map<String, Object> createPayment(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFound("Order not found with id = " + orderId));

        validateOrder(order);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Payment already completed");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for cancelled order");
        }

        try {
            RazorpayClient client =
                    new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject request = new JSONObject();
            request.put("amount", order.getTotalAmount() * 100);
            request.put("currency", "INR");
            request.put("receipt", order.getId());

            JSONObject notes = new JSONObject();
            notes.put("user", order.getUser().getName());
            request.put("notes", notes);

            com.razorpay.Order razorpayOrder = client.orders.create(request);

            order.setRazorpayId(razorpayOrder.get("id"));
            orderRepository.save(order);

            return Map.of(
                    "razorpayOrderId", razorpayOrder.get("id"),
                    "amount", razorpayOrder.get("amount"),
                    "currency", razorpayOrder.get("currency"),
                    "orderId", order.getId()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment", e);
        }
    }

    // ---------------- VERIFY PAYMENT ----------------
    @Override
    @Transactional
    public void verifyPayment(String orderId, PaymentVerifyObject dto) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFound("Order not found with id = " + orderId));

        validateOrder(order);

        // 2️⃣ 🔥 IMPORTANT GUARD
        if (order.getRazorpayId() == null) {
            throw new IllegalStateException("Razorpay order not created yet");
        }


        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", order.getRazorpayId());
            options.put("razorpay_payment_id", dto.getRazorpayPaymentId());
            options.put("razorpay_signature", dto.getRazorpaySignature());

            boolean verified =
                    Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!verified) {
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                throw new IllegalStateException("Payment verification failed");
            }

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentId(dto.getRazorpayPaymentId());
            orderRepository.save(order);

        } catch (Exception e) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentId(dto.getRazorpayPaymentId());
            orderRepository.save(order);
            throw new IllegalStateException("Payment verification failed");
        }
    }
}
