package com.substring.foodies.dto;

import com.substring.foodies.dto.enums.FoodType;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodItemDetailsDto {

    private String id;
    private String name;
    private String description;
    private int price;
    private Boolean isAvailable;
    private FoodType foodType;
    private String imageUrl;
    private int discountAmount;
    private double rating;
    // Context information
    private String foodCategoryId;
    private String foodCategoryName;
    private String foodSubCategoryId;
    private String foodSubCategoryName;
}
