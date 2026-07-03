package com.substring.foodies.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderItem extends BaseAuditableEntity{

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private FoodItems foodItems;

    private int quantity;

    public int getActualPriceOfOrderItem()
    {
        return quantity * foodItems.actualPrice();
    }
}
