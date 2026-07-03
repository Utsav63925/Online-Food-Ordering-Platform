package com.substring.foodies.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CartItems extends BaseAuditableEntity{

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(length = 36)
    private String id;

    @OneToOne
    @JoinColumn(name = "food_id")
    private FoodItems foodItems;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    public int getTotalCartItemsPrice()
    {
        return quantity * foodItems.actualPrice();
    }
}
