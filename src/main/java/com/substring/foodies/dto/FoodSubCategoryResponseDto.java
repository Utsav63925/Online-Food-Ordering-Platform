package com.substring.foodies.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodSubCategoryResponseDto {

    private String id;
    private String name;
    private List<FoodItemsMenuDto> foodItems = new ArrayList<>();
}
