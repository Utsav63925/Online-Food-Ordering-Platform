package com.substring.foodies.controller;

import com.substring.foodies.dto.RestaurantDto;
import com.substring.foodies.service.RestaurantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/api/restaurant")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    private Logger logger= LoggerFactory.getLogger(RestaurantController.class);

    @Value("${restaurant.file.path}")
    private String path;

    @PostMapping("/")
    public ResponseEntity<RestaurantDto> addRestaurant(@Valid @RequestBody RestaurantDto restaurantDto)
    {
        RestaurantDto restaurant=restaurantService.addRestaurant(restaurantDto);
        return new ResponseEntity<>(restaurant, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable String id)
    {
        RestaurantDto restaurantDto=restaurantService.getRestaurantById(id);
        return new ResponseEntity<>(restaurantDto, HttpStatus.OK);
    }

    @GetMapping("/owner/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_ADMIN')")
    public ResponseEntity<List<RestaurantDto>> getRestaurantByOwner(@PathVariable String id)
    {
        List<RestaurantDto> restaurantDtoList=restaurantService.getByOwner(id);
        return new ResponseEntity<>(restaurantDtoList, HttpStatus.OK);
    }


    @GetMapping("/")
    public ResponseEntity<Page<RestaurantDto>> getAllRestaurants(@RequestParam(value="page", required = false, defaultValue = "0") int page,
                                                                 @RequestParam(value="size", required = false, defaultValue = "6") int size,
                                                                 @RequestParam(value="sortBy", required = false, defaultValue = "rating") String sortBy,
                                                                 @RequestParam(value="sortDir", required = false, defaultValue = "desc") String sortDir)
    {
        Sort sort=sortDir.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageable= PageRequest.of(page, size, sort);

        Page<RestaurantDto> restaurantDtoList=restaurantService.getAllRestaurants(pageable);
        return new ResponseEntity<>(restaurantDtoList, HttpStatus.OK);
    }

    @GetMapping("/by-food/{id}")
    public ResponseEntity<List<RestaurantDto>> getRestaurantsByFood(@PathVariable String id)
    {
        List<RestaurantDto> restaurantDtoList=restaurantService.findByFoodItemsList_Id(id);
        return new ResponseEntity<>(restaurantDtoList, HttpStatus.OK);
    }

    @GetMapping("/open")
    public ResponseEntity<List<RestaurantDto>> getAllOpenRestaurants()

    {
        List<RestaurantDto> restaurantDtoList=restaurantService.getAllOpenRestaurants();
        return new ResponseEntity<>(restaurantDtoList, HttpStatus.OK);
    }

    @GetMapping("/searchByName")
    public ResponseEntity<List<RestaurantDto>> searchRestaurantsByName(@RequestParam(value="name") String name)
    {
        List<RestaurantDto> restaurants = restaurantService.findByNameContainingIgnoreCase(name);

        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}/restaurants")
    public ResponseEntity<List<RestaurantDto>> getRestaurantsByAddress(
            @PathVariable String addressId
    ) {
        return ResponseEntity.ok(
                restaurantService.getRestaurantsByAddress(addressId)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(@Valid @RequestBody RestaurantDto restaurantDto, @PathVariable String id)
    {
        // Check if the restaurant with the given ID exists
        RestaurantDto restaurantDto1 = restaurantService.updateSavedRestaurant(restaurantDto, id);

        return new ResponseEntity<>(restaurantDto1, HttpStatus.OK);
    }

    // ✅ ADD addresses to restaurant
    @PostMapping("/{restaurantId}/addresses")
    public ResponseEntity<RestaurantDto> addAddressesToRestaurant(
            @PathVariable String restaurantId,
            @RequestBody List<String> addressIds
    ) {
        RestaurantDto updated = restaurantService
                .addAddressesToRestaurant(restaurantId, addressIds);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{restoId}/foods")
    public ResponseEntity<RestaurantDto> addFoodItemsToRestaurant(
            @PathVariable String restoId,
            @RequestBody List<String> foodIds
    ) {

        RestaurantDto updatedRestaurant =
                restaurantService.addFoodItems(restoId, foodIds);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedRestaurant);
    }

    @DeleteMapping("/{restaurantId}/foods")
    public ResponseEntity<Void> removeFoodItemsFromRestaurant(
            @PathVariable String restaurantId,
            @RequestBody List<String> foodIds
    ) {
        restaurantService.removeFoodItems(restaurantId, foodIds);
        return ResponseEntity.noContent().build();
    }

    // ✅ REMOVE addresses from restaurant
    @DeleteMapping("/{restaurantId}/addresses")
    public ResponseEntity<RestaurantDto> removeAddressesFromRestaurant(
            @PathVariable String restaurantId,
            @RequestBody List<String> addressIds
    ) {
        RestaurantDto updated = restaurantService
                .removeAddressesFromRestaurant(restaurantId, addressIds);

        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDto> patchRestaurant(
            @PathVariable String restaurantId,
            @RequestBody RestaurantDto dto) {

        return ResponseEntity.ok(
                restaurantService.patchRestaurant(restaurantId, dto)
        );
    }

    // ✅ ACTIVATE RESTAURANT (ADMIN ONLY)
    @PatchMapping("/{restaurantId}/activate")
    public ResponseEntity<String> activateRestaurant(
            @PathVariable String restaurantId
    ) {
        restaurantService.activateRestaurant(restaurantId);
        return ResponseEntity.ok("Restaurant activated successfully");
    }

    // ❌ DEACTIVATE RESTAURANT (ADMIN ONLY)
    @PatchMapping("/{restaurantId}/deactivate")
    public ResponseEntity<String> deactivateRestaurant(
            @PathVariable String restaurantId
    ) {
        restaurantService.deactivateRestaurant(restaurantId);
        return ResponseEntity.ok("Restaurant deactivated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable String id)
    {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    // API to handle restaurant banner;
    @PostMapping("/uploadBanner/{restaurantId}")
    public ResponseEntity<RestaurantDto> addBanner(
            @PathVariable String restaurantId,
            @RequestParam("banner") MultipartFile banner
    ) throws IOException {

        RestaurantDto dto =
                restaurantService.uploadBanner(banner, restaurantId);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // UPDATE banner
    @PutMapping("/updateBanner/{restaurantId}")
    public ResponseEntity<RestaurantDto> updateBanner(
            @PathVariable String restaurantId,
            @RequestParam("banner") MultipartFile banner
    ) throws IOException {

        RestaurantDto dto =
                restaurantService.updateBanner(banner, restaurantId);

        return ResponseEntity.ok(dto);
    }


    @DeleteMapping("/deleteBanner/{restaurantId}")
    public ResponseEntity<Void> deleteBanner(
            @PathVariable String restaurantId
    ) {
        restaurantService.deleteBanner(restaurantId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/getBanner/{restaurantId}")
    public ResponseEntity<Resource> getBanner(@PathVariable String restaurantId) {

        Resource banner = restaurantService.getRestaurantBanner(restaurantId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(banner);
    }

}



