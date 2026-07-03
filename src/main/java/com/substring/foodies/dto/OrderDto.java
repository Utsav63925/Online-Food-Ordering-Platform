package com.substring.foodies.dto;

import com.substring.foodies.dto.enums.OrderStatus;
import com.substring.foodies.dto.enums.PaymentMode;
import com.substring.foodies.dto.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderDto {

    private String id;
    private String userId;
    private String restaurantId;
    private AddressDto address;
    private int totalAmount;
    private OrderStatus status = OrderStatus.PLACED;
    private LocalDateTime orderedAt;
    private LocalDateTime deliveryTime;
    private String deliveryBoyId;
    private String deliveryBoyName;
    private List<OrderItemDto> orderItemList = new ArrayList<>();
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
    private String paymentId;
    private String razorpayId;

}
