package com.substring.foodies.dto;

import com.substring.foodies.entity.FoodCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodSubCategoryDto {

    @NotBlank(message = "Please provide the id.")
    private String id;

    @NotBlank(message = "Please provide the sub-category name.")
    private String name;

    @NotBlank(message = "Please provide the category Id.")
    private String foodCategoryId;

}
