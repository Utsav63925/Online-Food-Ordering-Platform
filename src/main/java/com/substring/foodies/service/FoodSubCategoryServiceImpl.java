package com.substring.foodies.service;

import com.substring.foodies.dto.FoodSubCategoryDto;
import com.substring.foodies.entity.FoodCategory;
import com.substring.foodies.entity.FoodSubCategory;
import com.substring.foodies.exception.BadRequestException;
import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.FoodCategoryRepository;
import com.substring.foodies.repository.FoodSubCategoryRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.substring.foodies.Utility.Helper.normalize;

@Service
@Transactional
public class FoodSubCategoryServiceImpl implements FoodSubCategoryService {

    @Autowired
    private FoodSubCategoryRepository foodSubCategoryRepository;

    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    private FoodSubCategory findAndValidate(String id)
    {
        FoodSubCategory foodSubCategory =  foodSubCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Food Sub-category not found with id = " + id));

        return foodSubCategory;
    }

    @Override
    public FoodSubCategoryDto create(FoodSubCategoryDto dto) {

        if (foodSubCategoryRepository.existsById(dto.getId())) {
            throw new IllegalStateException(
                    "Food Sub Category already exists with id = " + dto.getId()
            );
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Sub-category name is required");
        }

        if (dto.getFoodCategoryId() == null) {
            throw new BadRequestException("Food category is required");
        }

        FoodCategory category = foodCategoryRepository
                .findById(dto.getFoodCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFound("Food category not found with id = " +
                                dto.getFoodCategoryId())
                );

        String normalized = normalize(dto.getName());

        if (foodSubCategoryRepository.existsByNormalizedNameIgnoreCaseAndFoodCategoryId(
                normalized, category.getId())) {

            throw new BadRequestException(
                    "Sub-category already exists with name = " + dto.getName()
            );
        }

        FoodSubCategory subCategory = modelMapper.map(dto, FoodSubCategory.class);
        subCategory.setFoodCategory(category);

        return modelMapper.map(
                foodSubCategoryRepository.save(subCategory),
                FoodSubCategoryDto.class
        );
    }


    @Override
    public FoodSubCategoryDto getById(String id) {
        FoodSubCategory subCategory = findAndValidate(id);

        return modelMapper.map(subCategory, FoodSubCategoryDto.class);
    }

    @Override
    public List<FoodSubCategoryDto> getAll() {
        return foodSubCategoryRepository.findAll()
                .stream()
                .map(sc -> modelMapper.map(sc, FoodSubCategoryDto.class))
                .toList();
    }

    @Override
    public List<FoodSubCategoryDto> getSubCategoriesByCategory(String id) {
        return foodSubCategoryRepository.findAllFoodSubCategoriesByFoodCategoryId(id)
                .stream()
                .map(sc -> modelMapper.map(sc, FoodSubCategoryDto.class))
                .toList();
    }

    @Override
    public FoodSubCategoryDto update(String id, FoodSubCategoryDto dto) {

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Sub-category name is required");
        }

        if (dto.getFoodCategoryId() == null) {
            throw new BadRequestException("Food category is required");
        }

        FoodSubCategory subCategory = findAndValidate(id);

        String newName = dto.getName();
        String normalized = normalize(dto.getName());

        String categoryId = dto.getFoodCategoryId();

        FoodCategory category = foodCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFound("Category not found with id = " + categoryId));


        // uniqueness check (exclude same entity)
        if (!subCategory.getName().equalsIgnoreCase(newName) &&
                foodSubCategoryRepository.existsByNormalizedNameIgnoreCaseAndFoodCategoryId(
                        normalized, categoryId)) {

            throw new BadRequestException(
                    "Sub-category already exists with name = " + newName + " in category id " + categoryId
            );
        }

        subCategory.setName(newName);
        subCategory.setFoodCategory(category);

        return modelMapper.map(
                foodSubCategoryRepository.save(subCategory),
                FoodSubCategoryDto.class
        );
    }



    @Override
    public FoodSubCategoryDto patch(String id, FoodSubCategoryDto dto) {

        FoodSubCategory subCategory = findAndValidate(id);

        // patch name
        if (dto.getName() != null &&
                !dto.getName().equalsIgnoreCase(subCategory.getName())) {

            String normalized = normalize(dto.getName());
            if (foodSubCategoryRepository.existsByNormalizedNameIgnoreCaseAndFoodCategoryId(
                    normalized,
                    subCategory.getFoodCategory().getId())) {

                throw new BadRequestException(
                        "Sub-category already exists with name = " + dto.getName() + " in category "+ subCategory.getFoodCategory().getId()
                );
            }
            subCategory.setName(dto.getName());
        }

        // patch category
        if (dto.getFoodCategoryId() != null) {

            FoodCategory category = foodCategoryRepository
                    .findById(dto.getFoodCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFound(
                                    "Category not found with id = " +
                                            dto.getFoodCategoryId())
                    );

            // 🔑 Determine the name that will actually be saved
            String effectiveName =
                    dto.getName() != null ? dto.getName() : subCategory.getName();

            String normalized = normalize(effectiveName);

            // 🔐 Uniqueness check in TARGET category
            if (foodSubCategoryRepository
                    .existsByNormalizedNameIgnoreCaseAndFoodCategoryId(
                            normalized,
                            dto.getFoodCategoryId())) {

                throw new BadRequestException(
                        "Sub-category already exists with name = " +
                                effectiveName + " in category " + dto.getFoodCategoryId()
                );
            }

            // ✅ Safe to move
            subCategory.setFoodCategory(category);
        }

        return modelMapper.map(
                foodSubCategoryRepository.save(subCategory),
                FoodSubCategoryDto.class
        );
    }


    @Override
    @Transactional
    public void delete(String id) {

        FoodSubCategory subCategory = findAndValidate(id);
        boolean hasFoodItems = !subCategory.getFoodItemList().isEmpty();

        if (hasFoodItems) {
            throw new BadRequestException(
                    "Cannot delete sub-category. Move or delete food items first."
            );
        }

        subCategory.getFoodCategory().getFoodSubCategoryList().remove(subCategory);
        foodSubCategoryRepository.delete(subCategory);
    }
}
