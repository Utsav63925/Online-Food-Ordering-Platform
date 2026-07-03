package com.substring.foodies.controller;

import com.substring.foodies.dto.FoodSubCategoryDto;
import com.substring.foodies.service.FoodSubCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-subcategories")
public class FoodSubCategoryController {

    @Autowired
    private FoodSubCategoryService foodSubCategoryService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodSubCategoryDto> create(
            @Valid @RequestBody FoodSubCategoryDto dto) {

        FoodSubCategoryDto created =
                foodSubCategoryService.create(dto);

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<FoodSubCategoryDto> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                foodSubCategoryService.getById(id)
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<FoodSubCategoryDto>> getAll() {

        return ResponseEntity.ok(
                foodSubCategoryService.getAll()
        );
    }

    @GetMapping("/{categoryId}/sub-categories")
    public ResponseEntity<List<FoodSubCategoryDto>> getSubCategoriesByCategory(
            @PathVariable String categoryId) {

        List<FoodSubCategoryDto> subCategories =
                foodSubCategoryService.getSubCategoriesByCategory(categoryId);

        return ResponseEntity.ok(subCategories);
    }

    // UPDATE (PUT)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodSubCategoryDto> update(
            @PathVariable String id,
            @Valid @RequestBody FoodSubCategoryDto dto) {

        return ResponseEntity.ok(
                foodSubCategoryService.update(id, dto)
        );
    }

    // PATCH
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<FoodSubCategoryDto> patch(
            @PathVariable String id,
            @RequestBody FoodSubCategoryDto dto) {

        return ResponseEntity.ok(
                foodSubCategoryService.patch(id, dto)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        foodSubCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
