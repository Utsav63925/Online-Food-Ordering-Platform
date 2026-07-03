package com.substring.foodies.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderItemDto {

    private String id;
    private String orderId;
    private String foodItemsId;
    private String foodItemsName;
    private int quantity;
}
