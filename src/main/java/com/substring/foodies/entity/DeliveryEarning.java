package com.substring.foodies.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEarning {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "delivery_boy_id")
    private User deliveryBoy;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private int amount;

    private LocalDateTime deliveryTime;

}
