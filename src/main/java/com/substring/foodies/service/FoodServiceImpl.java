package com.substring.foodies.service;


import com.substring.foodies.dto.*;
import com.substring.foodies.dto.enums.FoodType;
import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.*;
import com.substring.foodies.exception.FoodCategoryException;
import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.substring.foodies.Utility.Helper.normalize;

@Slf4j
@Service
@Transactional
public class FoodServiceImpl implements FoodService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    @Autowired
    private FoodSubCategoryRepository foodSubCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodRatingRepository foodRatingRepository;

    @Autowired
    private FileService fileService;

    @Value("${food.file.path}")
    private String bannerFolderpath;

    // ===================== HELPERS =====================
    private FoodItems findAndValidate(String id)
    {
        FoodItems foodItem = foodItemRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFound("Food item not found with id = " + id));
        return foodItem;
    }

    private void validateRestaurant(String restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFound("Restaurant not found with id = " + restaurantId);
        }
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Invalid session"));
    }
    // ===================== CREATE =====================

    @Override
    @Transactional
    public FoodItemDetailsDto addFood(FoodItemRequestDto dto) {

        if (foodItemRepository.existsById(dto.getId())) {
            throw new IllegalStateException(
                    "Food item already exists with id = " + dto.getId()
            );
        }

        String normalized = normalize(dto.getName());
        if (foodItemRepository.existsByNormalizedName(normalized)) {
            throw new IllegalStateException(
                    "Food item already exists with name = " + dto.getName()
            );
        }

        FoodCategory category = foodCategoryRepository
                .findById(dto.getFoodCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFound("Food category not found with id = "+dto.getFoodCategoryId()));

        FoodSubCategory subCategory = foodSubCategoryRepository
                .findById(dto.getFoodSubCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFound("Food subcategory not found with id = "+dto.getFoodSubCategoryId()));

        if (!subCategory.getFoodCategory().getId().equals(category.getId())) {
            throw new FoodCategoryException("SubCategory does not belong to category");
        }

        // 1️⃣ Create FoodItems
        FoodItems foodItem = modelMapper.map(dto, FoodItems.class);
        foodItem.setFoodCategory(category);
        foodItem.setFoodSubCategory(subCategory);

        // 2️⃣ SAVE FOOD FIRST (IMPORTANT)
        FoodItems savedFood = foodItemRepository.save(foodItem);

        // 3️⃣ Fetch restaurants
        List<Restaurant> restaurants =
                restaurantRepository.findAllById(dto.getRestaurantIds());

        if (restaurants.size() != dto.getRestaurantIds().size()) {
            throw new ResourceNotFound("One or more restaurants not found");
        }

        // 4️⃣ Attach relations
        restaurants.forEach(resto -> {
            resto.getFoodItemsList().add(savedFood);
            savedFood.getRestaurants().add(resto);
        });

        // 5️⃣ Save again to sync relations
        foodItemRepository.save(savedFood);

        return modelMapper.map(savedFood, FoodItemDetailsDto.class);
    }


    // ===================== UPDATE =====================

    @Override
    public FoodItemDetailsDto updateFood(FoodItemRequestDto dto, String foodId) {

        FoodItems food = findAndValidate(foodId);

        String normalized = normalize(dto.getName());

        if (foodItemRepository
                .existsByNormalizedNameAndIdNot(normalized, foodId)) {

            throw new IllegalStateException(
                    "Food item already exists with name = " +
                            dto.getName()
            );
        }

        food.setName(dto.getName());
        food.setDescription(dto.getDescription());
        food.setPrice(dto.getPrice());
        food.setIsAvailable(dto.getIsAvailable());
        food.setFoodType(dto.getFoodType());
        food.setDiscountAmount(dto.getDiscountAmount());

        FoodCategory category = foodCategoryRepository
                .findById(dto.getFoodCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFound("Food category not found with id = "+dto.getFoodCategoryId()));

        FoodSubCategory subCategory = foodSubCategoryRepository
                .findById(dto.getFoodSubCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFound("Food subcategory not found with id = "+dto.getFoodSubCategoryId()));

        if (!subCategory.getFoodCategory().getId().equals(category.getId())) {
            throw new FoodCategoryException("SubCategory does not belong to category");
        }

        food.setFoodCategory(category);
        food.setFoodSubCategory(subCategory);

        FoodItems updated = foodItemRepository.save(food);
        return modelMapper.map(updated, FoodItemDetailsDto.class);
    }

    // ===================== PATCH =====================

    @Override
    public FoodItemDetailsDto patchFood(String foodId, FoodItemsMenuDto patchDto) {

        FoodItems food = findAndValidate(foodId);

        if (patchDto.getName() != null)
        {
            String normalized = normalize(patchDto.getName());

            if (foodItemRepository.existsByNormalizedNameAndIdNot(normalized, foodId)) {
                throw new IllegalStateException(
                        "Food item already exists with name = " + patchDto.getName()
                );
            }

            food.setName(patchDto.getName());
        }
        if (patchDto.getDescription() != null) food.setDescription(patchDto.getDescription());
        if (patchDto.getPrice() > 0) food.setPrice(patchDto.getPrice());
        if (patchDto.getIsAvailable() != null) food.setIsAvailable(patchDto.getIsAvailable());
        if (patchDto.getFoodType() != null) food.setFoodType(patchDto.getFoodType());
        if (patchDto.getDiscountAmount() > 0) food.setDiscountAmount(patchDto.getDiscountAmount());

        FoodItems updated = foodItemRepository.save(food);
        return modelMapper.map(updated, FoodItemDetailsDto.class);
    }

    @Override
    @Transactional
    public FoodItemDetailsDto changeFoodCategory(String foodId, ChangeFoodCategoryDto dto) {
        FoodItems food = findAndValidate(foodId);

        FoodCategory category = foodCategoryRepository
                .findById(dto.getFoodCategoryId())
                .orElseThrow(() -> new ResourceNotFound("Category not found with id = "+dto.getFoodCategoryId()));

        FoodSubCategory subCategory = foodSubCategoryRepository
                .findById(dto.getFoodSubCategoryId())
                .orElseThrow(() -> new ResourceNotFound("Subcategory not found with id = "+dto.getFoodSubCategoryId()));

        if (!subCategory.getFoodCategory().getId().equals(category.getId())) {
            throw new FoodCategoryException("SubCategory does not belong to category");
        }

        String normalized = normalize(food.getName());

        if (foodItemRepository
                .existsByNormalizedNameAndFoodCategoryIdAndFoodSubCategoryIdAndIdNot(
                        normalized,
                        dto.getFoodCategoryId(),
                        dto.getFoodSubCategoryId(),
                        foodId)) {

            throw new IllegalStateException(
                    "Food item already exists with name = " +
                            food.getName() + " in the selected category and sub-category"
            );
        }

        food.setFoodCategory(category);
        food.setFoodSubCategory(subCategory);
        return modelMapper.map(foodItemRepository.save(food), FoodItemDetailsDto.class);
    }


    @Override
    public Resource getFoodImage(String foodId) {

        FoodItems food = findAndValidate(foodId);

        if (food.getImageUrl() == null) {
            throw new ResourceNotFound("Food image not found");
        }

        Path imagePath = Paths.get(bannerFolderpath)
                .resolve(food.getImageUrl())
                .normalize();

        try {
            Resource resource = new UrlResource(imagePath.toUri());

            if (!resource.exists()) {
                throw new ResourceNotFound("Food image file not found");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid food image path", e);
        }
    }

    @Override
    public FoodItemDetailsDto uploadFoodImage(MultipartFile file, String id) throws IOException {
        FoodItems foodItem = findAndValidate(id);

        if (foodItem.getImageUrl() != null) {
            throw new IllegalStateException(
                    "Image already exists. Use UPDATE image API to update the image.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String newFileName = System.currentTimeMillis() + extension;

        FileData fileData = fileService.uploadFile(file, bannerFolderpath + newFileName);
        foodItem.setImageUrl(fileData.getFileName());

        foodItemRepository.save(foodItem);
        return modelMapper.map(foodItem, FoodItemDetailsDto.class);
    }

    @Override
    @Transactional
    public FoodItemDetailsDto updateFoodImage(MultipartFile file, String foodId) throws IOException {

        FoodItems food = findAndValidate(foodId);

        if (food.getImageUrl() == null) {
            throw new IllegalStateException(
                    "No image exists. Use ADD image API first.");
        }

        // delete old image
        Path oldPath = Paths.get(bannerFolderpath)
                .resolve(food.getImageUrl())
                .normalize();
        Files.deleteIfExists(oldPath);

        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String newFileName = System.currentTimeMillis() + extension;

        FileData fileData = fileService.uploadFile(file, bannerFolderpath + newFileName);
        food.setImageUrl(fileData.getFileName());

        foodItemRepository.save(food);
        return modelMapper.map(food, FoodItemDetailsDto.class);
    }


    @Override
    @Transactional
    public void deleteFoodImage(String foodId) {

        FoodItems food = findAndValidate(foodId);

        if (food.getImageUrl() == null) {
            return; // nothing to delete
        }

        try {
            Path imagePath = Paths.get(bannerFolderpath)
                    .resolve(food.getImageUrl())
                    .normalize();

            Files.deleteIfExists(imagePath);

            food.setImageUrl(null);
            foodItemRepository.save(food);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete food image", e);
        }
    }


    // ===================== DELETE =====================

    @Override
    @Transactional
    public void deleteFood(String foodId) {

        FoodItems food = findAndValidate(foodId);

        // ❌ Non-owning side → do NOT manage relationship
        if (!food.getRestaurants().isEmpty()) {
            throw new IllegalStateException(
                    "Food item is linked to restaurants and cannot be deleted. First remove the foodItem from all the restaurants.");
        }

        // Delete image
        if (food.getImageUrl() != null) {
            try {
                Path imagePath = Paths.get(bannerFolderpath)
                        .resolve(food.getImageUrl())
                        .normalize();
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete food image", e);
            }
        }

        // ✅ Safe delete
        foodItemRepository.delete(food);
    }



    // ===================== ADMIN =====================

    @Override
    public Page<FoodItemDetailsDto> getAllFoodItems(Pageable pageable) {
        return foodItemRepository
                .findAll(pageable)
                .map(food -> modelMapper.map(food, FoodItemDetailsDto.class));
    }

    @Override
    public FoodItemDetailsDto getFoodById(String foodId) {
        FoodItems food = findAndValidate(foodId);
        return modelMapper.map(food, FoodItemDetailsDto.class);
    }

    // ===================== MENU =====================

    @Override
    public List<FoodCategoryDto> getFoodByRestaurant(String restaurantId) {

        validateRestaurant(restaurantId);

        List<FoodItems> foodItems = foodItemRepository.findMenuByRestaurant(restaurantId);

        Map<String, FoodCategoryDto> categoryMap = new LinkedHashMap<>();

        for (FoodItems food : foodItems) {

            FoodCategory category = food.getFoodCategory();
            FoodSubCategory subCategory = food.getFoodSubCategory();

            if (category == null || subCategory == null) continue;

            FoodCategoryDto categoryDto = categoryMap.computeIfAbsent(
                    category.getId(),
                    id -> new FoodCategoryDto(
                            category.getId(),
                            category.getName(),
                            category.getDescription(),
                            new ArrayList<>()
                    )
            );

            FoodSubCategoryResponseDto subCategoryDto =
                    categoryDto.getSubCategories()
                            .stream()
                            .filter(sc -> sc.getId().equals(subCategory.getId()))
                            .findFirst()
                            .orElseGet(() -> {
                                FoodSubCategoryResponseDto scDto =
                                        new FoodSubCategoryResponseDto(
                                                subCategory.getId(),
                                                subCategory.getName(),
                                                new ArrayList<>()
                                        );
                                categoryDto.getSubCategories().add(scDto);
                                return scDto;
                            });

            subCategoryDto.getFoodItems()
                    .add(modelMapper.map(food, FoodItemsMenuDto.class));
        }

        categoryMap.values().forEach(cat ->
                cat.getSubCategories().forEach(sub ->
                        sub.getFoodItems()
                                .sort(Comparator.comparing(FoodItemsMenuDto::getRating).reversed())
                )
        );

        return new ArrayList<>(categoryMap.values());
    }

    // ===================== FILTERS =====================

    @Override
    public List<FoodItemDetailsDto> getFoodByRestaurantAndCategory(
            String restaurantId, String foodCategoryId) {

        validateRestaurant(restaurantId);

        return foodItemRepository
                .findByRestaurantsIdAndFoodCategoryIdOrderByRatingDesc(
                        restaurantId, foodCategoryId)
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    @Override
    public List<FoodItemDetailsDto> getFoodByRestaurantAndSubCategory(
            String restaurantId, String foodSubCategoryId) {

        validateRestaurant(restaurantId);

        return foodItemRepository
                .findByRestaurantsIdAndFoodSubCategoryIdOrderByRatingDesc(
                        restaurantId, foodSubCategoryId)
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    @Override
    public List<FoodItemDetailsDto> getFoodByRestaurantAndFoodType(
            String restaurantId, FoodType foodType) {

        validateRestaurant(restaurantId);

        return foodItemRepository
                .findByRestaurantsIdAndFoodTypeOrderByRatingDesc(
                        restaurantId, foodType)
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    @Override
    public List<FoodItemDetailsDto> getFoodByCategory(String foodCategoryId) {
        return foodItemRepository
                .findByFoodCategoryIdOrderByRatingDesc(foodCategoryId)
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    @Override
    public List<FoodItemDetailsDto> getFoodBySubCategory(String foodSubCategoryId) {
        return foodItemRepository
                .findByFoodSubCategoryIdOrderByRatingDesc(foodSubCategoryId)
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    @Override
    public List<FoodItemDetailsDto> getFoodByFoodType(FoodType foodType) {
        return foodItemRepository
                .findByFoodTypeOrderByRatingDesc(foodType)
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    @Override
    public List<FoodItemDetailsDto> searchFoodByName(String foodName) {
        return foodItemRepository
                .findByNormalizedNameIgnoreCaseContainingOrderByRatingDesc(normalize(foodName))
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    public List<FoodItemDetailsDto> searchFoods(
            String restaurantId,
            String categoryId,
            String subCategoryId,
            FoodType foodType,
            Boolean isAvailable
    ) {
        return foodItemRepository.search(
                        restaurantId,
                        categoryId,
                        subCategoryId,
                        foodType,
                        isAvailable
                ).stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }


    @Override
    public List<FoodItemDetailsDto> searchFoodByRestaurantAndName(
            String restaurantId, String foodName) {

        validateRestaurant(restaurantId);

        return foodItemRepository
                .findByRestaurantsIdAndNormalizedNameIgnoreCaseContainingOrderByRatingDesc(
                        restaurantId, normalize(foodName))
                .stream()
                .map(f -> modelMapper.map(f, FoodItemDetailsDto.class))
                .toList();
    }

    // ===================== RELATION MANAGEMENT =====================

    @Override
    @Transactional
    public void addRestoForFood(String foodId, List<String> restaurantIds) {

        FoodItems food = findAndValidate(foodId);

        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);

        if (restaurants.size() != restaurantIds.size()) {
            throw new ResourceNotFound("One or more restaurants not found");
        }

        User user = getLoggedInUser();

        if(user.getRole() == Role.ROLE_RESTAURANT_ADMIN)
        {
            for(Restaurant resto: restaurants)
            {
                if(!resto.getOwner().getId().equals(user.getId()))
                {
                    throw new AccessDeniedException(
                            "You are not authorized to perform this action. " +
                                    "Only the restaurant owner or an admin can perform this action."
                    );
                }
            }
        }

        restaurants.forEach(resto -> {
                    resto.getFoodItemsList().add(food);
                    food.getRestaurants().add(resto);
        });

        restaurantRepository.saveAll(restaurants);
    }

    @Override
    @Transactional
    public void deleteRestoForFood(String foodId, List<String> restaurantIds) {

        FoodItems food = findAndValidate(foodId);

        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);

        if (restaurants.size() != restaurantIds.size()) {
            throw new ResourceNotFound("One or more restaurants not found");
        }

        User user = getLoggedInUser();

        if(user.getRole() == Role.ROLE_RESTAURANT_ADMIN)
        {
            for(Restaurant resto: restaurants)
            {
                if(!resto.getOwner().getId().equals(user.getId()))
                {
                    throw new AccessDeniedException(
                            "You are not authorized to perform this action. " +
                                    "Only the restaurant owner or an admin can perform this action."
                    );
                }
            }
        }
        restaurants.forEach(resto -> {
            resto.getFoodItemsList().remove(food);
            food.getRestaurants().remove(resto);
        });

        restaurantRepository.saveAll(restaurants);
    }

    // ===================== RATING =====================

    @Override
    @Transactional
    public void updateFoodRating(String foodId, ChangeRatingDto changeRating) {

        double rating = changeRating.getRating();
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        FoodItems food = findAndValidate(foodId);
        User user = getLoggedInUser();

        FoodRating rat = foodRatingRepository
                .findByUserIdAndFoodId(user.getId(), foodId)
                .orElse(
                        FoodRating.builder()
                                .user(user)
                                .food(food)
                                .build()
                );

        rat.setRating(rating);
        foodRatingRepository.save(rat);

        // 🔁 Recalculate average
        double avgRating = foodRatingRepository.averageRatingByFood(foodId);
        food.setRating(avgRating);
        foodItemRepository.save(food);

        // 🔁 Update restaurant ratings
        updateRestaurantRating(food.getRestaurants());
    }

    private void updateRestaurantRating(Set<Restaurant> restaurants) {
        for (Restaurant r : restaurants) {
            double avg = foodItemRepository.avgRatingByRestaurant(r.getId());
            r.setRating(avg);
            restaurantRepository.save(r);
        }
    }
}
