package com.substring.foodies.controller;

import com.substring.foodies.dto.*;
import com.substring.foodies.dto.enums.FoodType;
import com.substring.foodies.service.FoodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    @Autowired
    private FoodService foodService;


    @GetMapping("/foods")
    public ResponseEntity<List<FoodItemDetailsDto>> searchFoods(
            @RequestParam(required = false) String restaurantId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String subCategoryId,
            @RequestParam(required = false) FoodType foodType,
            @RequestParam(required = false) Boolean isAvailable
    ) {
        return ResponseEntity.ok(
                foodService.searchFoods(
                        restaurantId,
                        categoryId,
                        subCategoryId,
                        foodType,
                        isAvailable
                )
        );
    }


    @GetMapping("/{foodId}/image")
    public ResponseEntity<Resource> getFoodImage(@PathVariable String foodId) {

        Resource image = foodService.getFoodImage(foodId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    // ===================== ADD IMAGE (POST) =====================
    // Only adds image if none exists
    @PostMapping("/{foodId}/image")
    public ResponseEntity<FoodItemDetailsDto> addFoodImage(
            @PathVariable String foodId,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        FoodItemDetailsDto response =
                foodService.uploadFoodImage(image, foodId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ===================== UPDATE IMAGE (PUT) =====================
    // Replaces existing image
    @PutMapping("/{foodId}/image")
    public ResponseEntity<FoodItemDetailsDto> updateFoodImage(
            @PathVariable String foodId,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        FoodItemDetailsDto response =
                foodService.updateFoodImage(image, foodId);

        return ResponseEntity.ok(response);
    }

    // ===================== DELETE IMAGE (DELETE) =====================
    @DeleteMapping("/{foodId}/image")
    public ResponseEntity<Void> deleteFoodImage(
            @PathVariable String foodId
    ) {
        foodService.deleteFoodImage(foodId);
        return ResponseEntity.noContent().build();
    }

    // ---------------- CREATE ----------------
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodItemDetailsDto> addFood(
            @Valid @RequestBody FoodItemRequestDto dto) {

        return new ResponseEntity<>(
                foodService.addFood(dto),
                HttpStatus.CREATED
        );
    }

    // ---------------- UPDATE ----------------
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodItemDetailsDto> updateFood(
            @Valid @RequestBody FoodItemRequestDto dto,
            @PathVariable String id) {

        return ResponseEntity.ok(
                foodService.updateFood(dto, id)
        );
    }


    // ---------------- PATCH (SCALARS ONLY) ----------------
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodItemDetailsDto> patchFood(
            @PathVariable String id,
            @RequestBody FoodItemsMenuDto patchDto) {

        return ResponseEntity.ok(
                foodService.patchFood(id, patchDto)
        );
    }

    @PatchMapping("/{foodId}/category")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodItemDetailsDto> changeFoodCategory(
            @PathVariable String foodId,
            @RequestBody @Valid ChangeFoodCategoryDto dto
    ) {
        FoodItemDetailsDto updatedFood =
                foodService.changeFoodCategory(foodId, dto);

        return ResponseEntity.ok(updatedFood);
    }


    // ---------------- DELETE ----------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<Void> deleteFood(@PathVariable String id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- ADMIN / DETAILS ----------------
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<Page<FoodItemDetailsDto>> getAllFoodItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                foodService.getAllFoodItems(pageable)
        );
    }

    @GetMapping("/{foodId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodItemDetailsDto> getFoodById(
            @PathVariable String foodId) {

        return ResponseEntity.ok(
                foodService.getFoodById(foodId)
        );
    }

    // ---------------- MENU (USER) ----------------
    @GetMapping("/restaurant/{restaurantId}/menu")
    public ResponseEntity<List<FoodCategoryDto>> getMenuByRestaurant(
            @PathVariable String restaurantId) {

        return ResponseEntity.ok(
                foodService.getFoodByRestaurant(restaurantId)
        );
    }

    // ---------------- FILTERS ----------------
    @GetMapping("/restaurant/{restaurantId}/category/{categoryId}")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodByRestaurantAndCategory(
            @PathVariable String restaurantId,
            @PathVariable String categoryId) {

        return ResponseEntity.ok(
                foodService.getFoodByRestaurantAndCategory(restaurantId, categoryId)
        );
    }

    @GetMapping("/restaurant/{restaurantId}/subcategory/{subCategoryId}")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodByRestaurantAndSubCategory(
            @PathVariable String restaurantId,
            @PathVariable String subCategoryId) {

        return ResponseEntity.ok(
                foodService.getFoodByRestaurantAndSubCategory(restaurantId, subCategoryId)
        );
    }

    @GetMapping("/restaurant/{restaurantId}/type/{type}")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodByRestaurantAndFoodType(
            @PathVariable String restaurantId,
            @PathVariable FoodType type) {

        return ResponseEntity.ok(
                foodService.getFoodByRestaurantAndFoodType(restaurantId, type)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodByCategory(
            @PathVariable String categoryId) {

        return ResponseEntity.ok(
                foodService.getFoodByCategory(categoryId)
        );
    }

    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodBySubCategory(
            @PathVariable String subCategoryId) {

        return ResponseEntity.ok(
                foodService.getFoodBySubCategory(subCategoryId)
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodByFoodType(
            @PathVariable FoodType type) {

        return ResponseEntity.ok(
                foodService.getFoodByFoodType(type)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodByName(
            @RequestParam String name) {

        return ResponseEntity.ok(
                foodService.searchFoodByName(name)
        );
    }

    @GetMapping("/restaurant/{restaurantId}/search")
    public ResponseEntity<List<FoodItemDetailsDto>> getFoodByRestaurantAndName(
            @PathVariable String restaurantId,
            @RequestParam String name) {

        return ResponseEntity.ok(
                foodService.searchFoodByRestaurantAndName(restaurantId, name)
        );
    }

    // ---------------- RELATION MANAGEMENT ----------------
    @PostMapping("/{foodId}/restaurants")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<Void> addRestoForFood(
            @PathVariable String foodId,
            @RequestBody List<String> restoIds) {

        foodService.addRestoForFood(foodId, restoIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{foodId}/restaurants")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<Void> deleteRestoForFood(
            @PathVariable String foodId,
            @RequestBody List<String> restoIds) {

        foodService.deleteRestoForFood(foodId, restoIds);
        return ResponseEntity.noContent().build();
    }

    // ---------------- RATING ----------------
    @PatchMapping("/{foodId}/rating")
    public ResponseEntity<Void> updateFoodRating(
            @PathVariable String foodId,
            @RequestBody ChangeRatingDto rating) {

        foodService.updateFoodRating(foodId, rating);
        return ResponseEntity.noContent().build();
    }
}
