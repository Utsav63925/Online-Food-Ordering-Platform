package com.substring.foodies.service;

import com.substring.foodies.dto.FoodCategoryDto;
import com.substring.foodies.dto.FoodSubCategoryDto;
import com.substring.foodies.entity.FoodCategory;
import com.substring.foodies.entity.FoodSubCategory;
import com.substring.foodies.exception.BadRequestException;
import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.FoodCategoryRepository;
import com.substring.foodies.repository.FoodItemRepository;
import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.substring.foodies.Utility.Helper.normalize;

@Service
@Transactional
public class FoodCategoryServiceImpl implements FoodCategoryService {

    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    private FoodCategory findAndValidate(String id)
    {
        FoodCategory category = foodCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Food category not found with id = " + id));

        return category;
    }

    @Override
    public FoodCategoryDto create(FoodCategoryDto dto) throws BadRequestException {

        if (foodCategoryRepository.existsById(dto.getId())) {
            throw new IllegalStateException(
                    "Food Category already exists with id = " + dto.getId()
            );
        }

        String normalized = normalize(dto.getName());

        if (foodCategoryRepository.existsByNormalizedNameIgnoreCase(normalized)) {
            throw new BadRequestException(
                    "Food category already exists with name = " + dto.getName()
            );
        }

        FoodCategory category = modelMapper.map(dto, FoodCategory.class);
        return modelMapper.map(foodCategoryRepository.save(category), FoodCategoryDto.class);
    }


    @Override
    public FoodCategoryDto getById(String id) {
        FoodCategory category = findAndValidate(id);
        return modelMapper.map(category, FoodCategoryDto.class);
    }

    @Override
    public List<FoodCategoryDto> getAll() {
        return foodCategoryRepository.findAll()
                .stream()
                .map(cat -> modelMapper.map(cat, FoodCategoryDto.class))
                .toList();
    }

    @Override
    public List<FoodSubCategoryDto> getAllSubCategoriesByCategory(String id)
    {
        FoodCategory category = findAndValidate(id);

        List<FoodSubCategory> foodSubCategoryList = category.getFoodSubCategoryList();
        return foodSubCategoryList.stream()
                .map(cat -> modelMapper.map(cat, FoodSubCategoryDto.class)).toList();
    }

    @Override
    public FoodCategoryDto update(String id, FoodCategoryDto dto) {

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        FoodCategory category = findAndValidate(id);

        String newName = dto.getName();
        String normalized = normalize(newName);

        boolean nameChanged =
                !category.getName().equalsIgnoreCase(newName);

        if (nameChanged &&
                foodCategoryRepository.existsByNormalizedNameIgnoreCase(normalized)) {

            throw new BadRequestException(
                    "Food category already exists with name = " + newName
            );
        }

        category.setName(newName);              // triggers @PreUpdate
        category.setDescription(dto.getDescription());

        return modelMapper.map(
                foodCategoryRepository.save(category),
                FoodCategoryDto.class
        );
    }


    @Override
    public FoodCategoryDto patch(String id, FoodCategoryDto dto) {

        FoodCategory category = findAndValidate(id);

        if (dto.getName() != null) {

            String newName = dto.getName();
            String normalized = normalize(newName);

            boolean nameChanged =
                    !category.getName().equalsIgnoreCase(newName);

            if (nameChanged &&
                    foodCategoryRepository.existsByNormalizedNameIgnoreCase(normalized)) {

                throw new BadRequestException(
                        "Food category already exists with name = " + newName
                );
            }

            category.setName(newName);   // triggers @PreUpdate
        }

        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }

        return modelMapper.map(
                foodCategoryRepository.save(category),
                FoodCategoryDto.class
        );
    }



    @Override
    public void delete(String id) {

        FoodCategory category = findAndValidate(id);

        boolean hasFoods = foodItemRepository.existsByFoodCategoryId(id);
        boolean hasSubCategories = !category.getFoodSubCategoryList().isEmpty();

        if (hasFoods || hasSubCategories) {
            throw new BadRequestException(
                    "Deletion not allowed. This category contains sub-categories and/or food items. Please move or remove them before deleting the category."
            );
        }

        foodCategoryRepository.delete(category);
    }

}
