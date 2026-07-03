package com.substring.foodies.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartItemsDto {

    private String id;
    private String foodItemsId;
    private String foodItemsName;
    private int quantity;
    private String cartId;
}
