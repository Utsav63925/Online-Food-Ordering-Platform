package com.substring.foodies.controller;

import com.substring.foodies.dto.FoodCategoryDto;
import com.substring.foodies.dto.FoodSubCategoryDto;
import com.substring.foodies.service.FoodCategoryService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-categories")
public class FoodCategoryController {

    @Autowired
    private FoodCategoryService foodCategoryService;

    @PostMapping
    public ResponseEntity<FoodCategoryDto> create(@Valid @RequestBody FoodCategoryDto dto) throws BadRequestException {
        return new ResponseEntity<>(foodCategoryService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodCategoryDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(foodCategoryService.getById(id));
    }

    @GetMapping("/{id}/sub-categories")
    public ResponseEntity<List<FoodSubCategoryDto>> getByAllSubCategories(@PathVariable String id) {
        return ResponseEntity.ok(foodCategoryService.getAllSubCategoriesByCategory(id));
    }


    @GetMapping
    public ResponseEntity<List<FoodCategoryDto>> getAll() {
        return ResponseEntity.ok(foodCategoryService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodCategoryDto> update(
            @PathVariable String id,
            @Valid @RequestBody FoodCategoryDto dto) {

        return ResponseEntity.ok(foodCategoryService.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FoodCategoryDto> patch(
            @PathVariable String id,
            @RequestBody FoodCategoryDto dto) {

        return ResponseEntity.ok(foodCategoryService.patch(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        foodCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
