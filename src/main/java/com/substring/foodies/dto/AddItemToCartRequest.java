package com.substring.foodies.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddItemToCartRequest {

    @NotBlank(message = "Please provide the userId.")
    private String userId;

    @NotBlank(message = "Please provide the quantity.")
    private String foodItemId;

    @NotNull(message = "Please provide the quantity.")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;


    @NotBlank(message = "Please provide the restaurant Id.")
    private String restoId;
}
