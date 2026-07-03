package com.substring.foodies.service;

import com.substring.foodies.dto.*;
import com.substring.foodies.dto.enums.FoodType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FoodService {

    // ===================== CREATE / UPDATE =====================

    FoodItemDetailsDto addFood(FoodItemRequestDto foodItemRequestDto);

    FoodItemDetailsDto updateFood(FoodItemRequestDto foodItemRequestDto, String foodId);

    FoodItemDetailsDto patchFood(String foodId, FoodItemsMenuDto  patchDto);

    Resource getFoodImage(String foodId);

    FoodItemDetailsDto uploadFoodImage(MultipartFile file, String id) throws IOException;

    void deleteFoodImage(String path);

    FoodItemDetailsDto updateFoodImage(MultipartFile file, String foodId) throws IOException;

    FoodItemDetailsDto changeFoodCategory(String foodId, ChangeFoodCategoryDto dto);

    // ===================== DELETE =====================

    void deleteFood(String foodId);

    // ===================== ADMIN / DETAILS =====================

    Page<FoodItemDetailsDto> getAllFoodItems(Pageable pageable);

    FoodItemDetailsDto getFoodById(String foodId);

    // ===================== MENU =====================

    List<FoodCategoryDto> getFoodByRestaurant(String restaurantId);

    // ===================== RESTAURANT-SCOPED FILTERS =====================

    List<FoodItemDetailsDto> getFoodByRestaurantAndCategory(
            String restaurantId,
            String foodCategoryId
    );

    List<FoodItemDetailsDto> getFoodByRestaurantAndSubCategory(
            String restaurantId,
            String foodSubCategoryId
    );

    List<FoodItemDetailsDto> getFoodByRestaurantAndFoodType(
            String restaurantId,
            FoodType foodType
    );

    List<FoodItemDetailsDto> searchFoodByRestaurantAndName(
            String restaurantId,
            String foodName
    );

    // ===================== GLOBAL FILTERS =====================

    List<FoodItemDetailsDto> getFoodByCategory(String foodCategoryId);

    List<FoodItemDetailsDto> getFoodBySubCategory(String foodSubCategoryId);

    List<FoodItemDetailsDto> getFoodByFoodType(FoodType foodType);

    List<FoodItemDetailsDto> searchFoodByName(String foodName);

    List<FoodItemDetailsDto> searchFoods(String restaurantId,
                                         String categoryId,
                                         String subCategoryId,
                                         FoodType foodType,
                                         Boolean isAvailable);

    // ===================== RELATION MANAGEMENT =====================

    void addRestoForFood(String foodId, List<String> restaurantIds);

    void deleteRestoForFood(String foodId, List<String> restaurantIds);

    // ===================== RATING =====================

    void updateFoodRating(String foodId, ChangeRatingDto rating);
}
