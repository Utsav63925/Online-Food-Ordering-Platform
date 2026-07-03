package com.substring.foodies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodCategoryDto {

    @NotBlank(message = "Please provide the id.")
    private String id;

    @NotBlank(message = "Please provide the category name.")
    private String name;

    @Size(
            max = 255,
            message = "Description cannot exceed 255 characters."
    )
    private String description;

    private List<FoodSubCategoryResponseDto> subCategories = new ArrayList<>();
}
