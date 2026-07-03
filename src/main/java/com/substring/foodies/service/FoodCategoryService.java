package com.substring.foodies.service;

import com.substring.foodies.dto.FoodCategoryDto;
import com.substring.foodies.dto.FoodSubCategoryDto;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface FoodCategoryService {

    FoodCategoryDto create(FoodCategoryDto dto) throws BadRequestException;

    FoodCategoryDto getById(String id);

    List<FoodCategoryDto> getAll();

    List<FoodSubCategoryDto> getAllSubCategoriesByCategory(String id);

    FoodCategoryDto update(String id, FoodCategoryDto dto);

    FoodCategoryDto patch(String id, FoodCategoryDto dto);

    void delete(String id);
}
