package com.substring.foodies.dto;

import com.substring.foodies.dto.enums.FoodType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodItemRequestDto {

    @NotBlank(message = "Please provide the id.")
    private String id;

    @NotBlank(message = "Please provide the name.")
    private String name;

    private String description;

    @Min(0)
    private int price;

    @NotNull(message = "Please provide the food item's availability.")
    private Boolean isAvailable;

    @NotNull(message = "Please provide the food type.")
    private FoodType foodType;

    private String imageUrl;

    @Min(0)
    private int discountAmount;

    // 🔗 RELATION IDs ONLY
    @NotBlank(message = "Please provide the food category Id.")
    private String foodCategoryId;

    @NotBlank(message = "Please provide the food sub category Id.")
    private String foodSubCategoryId;

    private Set<String> restaurantIds;
}
