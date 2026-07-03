package com.substring.foodies.dto;

import com.substring.foodies.dto.enums.FoodType;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FoodItemsMenuDto {

    private String id;
    private String name;
    private String description;
    private int price;
    private Boolean isAvailable;
    private FoodType foodType;
    private String imageUrl;
    private int discountAmount;
    private double rating;

}
