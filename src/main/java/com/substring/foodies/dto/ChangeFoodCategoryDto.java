package com.substring.foodies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeFoodCategoryDto {

    @NotBlank(message = "Food category id is required")
    private String foodCategoryId;

    @NotBlank(message = "Food sub-category id is required")
    private String foodSubCategoryId;
}
