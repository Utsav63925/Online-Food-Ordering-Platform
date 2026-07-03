package com.substring.foodies.service;

import com.substring.foodies.dto.FoodSubCategoryDto;

import java.util.List;

public interface FoodSubCategoryService {

    FoodSubCategoryDto create(FoodSubCategoryDto dto);

    FoodSubCategoryDto getById(String id);

    List<FoodSubCategoryDto> getAll();

    List<FoodSubCategoryDto> getSubCategoriesByCategory(String id);

    FoodSubCategoryDto update(String id, FoodSubCategoryDto dto);

    FoodSubCategoryDto patch(String id, FoodSubCategoryDto dto);

    void delete(String id);
}
