package com.substring.foodies.dto;

import com.substring.foodies.entity.Order;
import com.substring.foodies.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeliveryEarningDto {

    private String id;
    private User deliveryBoy;
    private Order order;
    private int amount;
    private LocalDateTime deliveryTime;
}
