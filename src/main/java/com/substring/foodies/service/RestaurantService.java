package com.substring.foodies.service;

import com.substring.foodies.dto.RestaurantDto;
import com.substring.foodies.entity.Restaurant;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;


public interface RestaurantService {

    RestaurantDto addRestaurant(RestaurantDto restaurantDto);

    Page<RestaurantDto> getAllRestaurants(Pageable pageable);

    RestaurantDto updateSavedRestaurant(RestaurantDto restaurantDto, String id);

    RestaurantDto patchRestaurant(String restaurantId, RestaurantDto patchDto);

    RestaurantDto addFoodItems(String restoId, List<String> foodIds);

    void removeFoodItems(String restoId, List<String> foodIds);

    RestaurantDto addAddressesToRestaurant(String restoId, List<String> addressIds);

    RestaurantDto removeAddressesFromRestaurant(String restoId, List<String> addressIds);

    List<RestaurantDto> getRestaurantsByAddress(String addressId);

    RestaurantDto getRestaurantById (String id);

    void deleteRestaurant(String id);

    List<RestaurantDto> getAllOpenRestaurants();

    List<RestaurantDto> findByNameContainingIgnoreCase(String pattern);

    Resource getRestaurantBanner(String restaurantId);

    RestaurantDto uploadBanner(MultipartFile file, String id) throws IOException;

    RestaurantDto updateBanner(MultipartFile file, String restaurantId) throws IOException;

    void deleteBanner(String id);

    List<RestaurantDto> getByOwner(String ownerId);

    List<RestaurantDto> findByFoodItemsList_Id(String foodId);

    void activateRestaurant(String restaurantId);

    void deactivateRestaurant(String restaurantId);
}
