package com.substring.foodies.service;

import com.substring.foodies.dto.AddressDto;
import com.substring.foodies.dto.FileData;
import com.substring.foodies.dto.RestaurantDto;
import com.substring.foodies.dto.enums.AddressType;
import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.Address;
import com.substring.foodies.entity.FoodItems;
import com.substring.foodies.entity.Restaurant;
import com.substring.foodies.entity.User;
import com.substring.foodies.exception.BadRequestException;
import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.AddressRepository;
import com.substring.foodies.repository.FoodItemRepository;
import com.substring.foodies.repository.RestaurantRepository;
import com.substring.foodies.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    @Autowired
    private FileService fileService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${restaurant.file.path}")
    private String bannerFolderpath;

    private Logger log= LoggerFactory.getLogger(RestaurantServiceImpl.class);

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Invalid session"));
    }

    private Restaurant findAndValidate(String restaurantId)
    {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFound("Restaurant not found with id = "+restaurantId));

        return restaurant;
    }

    private User validateRestaurantOwner(String ownerId) {

        User loggedInUser = getLoggedInUser();

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new ResourceNotFound("User not found with id = " + ownerId));

        // ✅ Owner must be ADMIN or RESTAURANT_ADMIN
        if (owner.getRole() != Role.ROLE_ADMIN &&
                owner.getRole() != Role.ROLE_RESTAURANT_ADMIN) {

            throw new IllegalStateException(
                    "User " + ownerId + " is not authorized to own a restaurant."
            );
        }
        // ✅ ADMIN can assign anyone (ADMIN or RESTAURANT_ADMIN)
        return owner;
    }


    private void validateRestaurantAccess(Restaurant restaurant) {

        User user = getLoggedInUser();

        Role role = user.getRole();

        // ❌ Only ADMIN or RESTAURANT_ADMIN allowed
        if (role != Role.ROLE_ADMIN && role != Role.ROLE_RESTAURANT_ADMIN) {
            throw new AccessDeniedException(
                    "Access denied. Only ADMIN or RESTAURANT_ADMIN can manage restaurants."
            );
        }

        if (user.getRole() == Role.ROLE_RESTAURANT_ADMIN &&
                !user.getId().equals(restaurant.getOwner().getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to perform this action. " +
                            "Only the restaurant owner or an admin can perform this action."
            );
        }
    }

    @Override
    public RestaurantDto addRestaurant(RestaurantDto restaurantDto) {

        if (restaurantRepository.existsById(restaurantDto.getId())) {
            throw new IllegalStateException(
                    "Restaurant already exists with id = " + restaurantDto.getId()
            );
        }

        Restaurant restaurant = modelMapper.map(restaurantDto, Restaurant.class);
        restaurant.setRating(0.0);

        User owner = validateRestaurantOwner(restaurantDto.getOwnerId());
        User loggedInUser = getLoggedInUser();
        // 🔒 RESTAURANT_ADMIN can assign ONLY themselves
        if (loggedInUser.getRole() == Role.ROLE_RESTAURANT_ADMIN &&
                !loggedInUser.getId().equals(owner.getId())) {

            throw new AccessDeniedException(
                    "Restaurant admins can assign ownership only to themselves."
            );
        }

        restaurant.setOwner(owner);

        List<String> addressIds = restaurantDto.getAddresses()
                .stream()
                .map(AddressDto::getId)
                .toList();

        List<Address> addressList = addressRepository.findAllById(addressIds);
        Set<String> foundIds = addressList.stream()
                .map(Address::getId)
                .collect(Collectors.toSet());

        List<String> missingIds = addressIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFound("Addresses not found with ids = " + missingIds);
        }

        for (Address address : addressList) {

            if(address.getAddressType() != AddressType.RESTAURANT)
            {
                throw new BadRequestException("Only restaurant addresses are allowed");
            }
            restaurant.getAddresses().add(address);
            address.getRestaurants().add(restaurant);
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        return modelMapper.map(saved, RestaurantDto.class);
    }

    @Override
    public Page<RestaurantDto> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(resto->modelMapper.map(resto, RestaurantDto.class));
    }

    @Override
    @Transactional
    public RestaurantDto updateSavedRestaurant(RestaurantDto restaurantDto, String restaurantId) {

        Restaurant restaurant = findAndValidate(restaurantId);

        validateRestaurantAccess(restaurant);

        restaurant.setName(restaurantDto.getName());
        restaurant.setDescription(restaurantDto.getDescription());
        restaurant.setOpenTime(restaurantDto.getOpenTime());
        restaurant.setCloseTime(restaurantDto.getCloseTime());

        if (restaurantDto.getOwnerId() != null &&
                !restaurantDto.getOwnerId().equals(restaurant.getOwner().getId())) {

            User newOwner = validateRestaurantOwner(restaurantDto.getOwnerId());
            restaurant.setOwner(newOwner);
        }

        Restaurant updated = restaurantRepository.save(restaurant);
        return modelMapper.map(updated, RestaurantDto.class);
    }


    @Override
    @Transactional
    public RestaurantDto patchRestaurant(String restaurantId, RestaurantDto patchDto) {

        Restaurant restaurant = findAndValidate(restaurantId);

        validateRestaurantAccess(restaurant);

        if (patchDto.getName() != null)
            restaurant.setName(patchDto.getName());

        if (patchDto.getDescription() != null)
            restaurant.setDescription(patchDto.getDescription());

        if (patchDto.getOpenTime() != null)
            restaurant.setOpenTime(patchDto.getOpenTime());

        if (patchDto.getCloseTime() != null)
            restaurant.setCloseTime(patchDto.getCloseTime());

        if (patchDto.getOwnerId() != null &&
                !patchDto.getOwnerId().equals(restaurant.getOwner().getId())) {

            User newOwner = validateRestaurantOwner(patchDto.getOwnerId());
            restaurant.setOwner(newOwner);
        }

        Restaurant updated = restaurantRepository.save(restaurant);
        return modelMapper.map(updated, RestaurantDto.class);
    }

    @Override
    @Transactional
    public RestaurantDto addFoodItems(String restoId, List<String> foodIds) {

        Restaurant restaurant = findAndValidate(restoId);
        validateRestaurantAccess(restaurant); // if you already use this elsewhere

        List<FoodItems> foodItems = foodItemRepository.findAllById(foodIds);

        // 🔴 Validate missing food IDs
        Set<String> foundIds = foodItems.stream()
                .map(FoodItems::getId)
                .collect(Collectors.toSet());

        List<String> missingIds = foodIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFound("Food items not found with ids = " + missingIds);
        }

        // 🔗 Link food items with restaurant
        for (FoodItems food : foodItems) {
            restaurant.getFoodItemsList().add(food);
            food.getRestaurants().add(restaurant); // only if bidirectional
        }

        restaurantRepository.save(restaurant);
        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Override
    @Transactional
    public void removeFoodItems(String restoId, List<String> foodIds) {

        Restaurant restaurant = findAndValidate(restoId);
        validateRestaurantAccess(restaurant); // if you already use this elsewhere

        List<FoodItems> foodItems = foodItemRepository.findAllById(foodIds);

        // 🔴 Validate missing food IDs
        Set<String> foundIds = foodItems.stream()
                .map(FoodItems::getId)
                .collect(Collectors.toSet());

        List<String> missingIds = foodIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFound("Food items not found with ids = " + missingIds);
        }

        // 🔗 Link food items with restaurant
        for (FoodItems food : foodItems) {
            restaurant.getFoodItemsList().remove(food);
            food.getRestaurants().remove(restaurant); // only if bidirectional
        }
        restaurantRepository.save(restaurant);
    }


    @Override
    @Transactional
    public RestaurantDto addAddressesToRestaurant(
            String restaurantId,
            List<String> addressIds
    ) {

        Restaurant restaurant = findAndValidate(restaurantId);
        validateRestaurantAccess(restaurant);

        List<Address> addresses = addressRepository.findAllById(addressIds);

        Set<String> foundIds = addresses.stream()
                .map(Address::getId)
                .collect(Collectors.toSet());

        List<String> missingIds = addressIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFound("Addresses not found with ids = " + missingIds);
        }


        for (Address addr : addresses) {
            if(addr.getAddressType() != AddressType.RESTAURANT)
            {
                throw new BadRequestException("Only restaurant addresses are allowed, Invalid address for restaurant = "+addr.getId());
            }
            restaurant.getAddresses().add(addr);
            addr.getRestaurants().add(restaurant);
        }

        restaurantRepository.save(restaurant);
        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Transactional
    public RestaurantDto removeAddressesFromRestaurant(
            String restaurantId,
            List<String> addressIds
    ) {
        Restaurant restaurant = findAndValidate(restaurantId);
        validateRestaurantAccess(restaurant);

        Set<Address> toRemove = restaurant.getAddresses().stream()
                .filter(a -> addressIds.contains(a.getId()))
                .collect(Collectors.toSet());

        for (Address addr : toRemove) {
            restaurant.getAddresses().remove(addr);
            addr.getRestaurants().remove(restaurant);
        }

        restaurantRepository.save(restaurant);
        return modelMapper.map(restaurant, RestaurantDto.class);
    }


    @Override
    public List<RestaurantDto> findByFoodItemsList_Id(String foodId) {
        return restaurantRepository
                .findByFoodItemsList_IdOrderByRatingDesc(foodId)
                .stream()
                .map(resto->modelMapper.map(resto, RestaurantDto.class))
                .toList();
    }

    @Override
    public RestaurantDto getRestaurantById(String id) {
        Restaurant restaurant = findAndValidate(id);

        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    public List<RestaurantDto> getRestaurantsByAddress(String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFound("Address not found with id = "+addressId));

        return address.getRestaurants()
                .stream()
                .sorted(Comparator.comparing(Restaurant::getRating).reversed())
                .map(r -> modelMapper.map(r, RestaurantDto.class))
                .toList();
    }


    @Override
    @Transactional
    public void deleteRestaurant(String id) {

        Restaurant restaurant = findAndValidate(id);

        validateRestaurantAccess(restaurant);

        // Remove Restaurant ↔ FoodItems
        for (FoodItems food : restaurant.getFoodItemsList()) {
            food.getRestaurants().remove(restaurant);
        }
        restaurant.getFoodItemsList().clear();

        // Remove Restaurant ↔ Address
        for (Address addr : restaurant.getAddresses()) {
            addr.getRestaurants().remove(restaurant);
        }
        restaurant.getAddresses().clear();

        restaurantRepository.delete(restaurant);
    }


    @Override
    public List<RestaurantDto> getAllOpenRestaurants() {
        return restaurantRepository
                .findByIsOpenTrueOrderByRatingDesc()
                .stream()
                .map(resto->modelMapper.map(resto, RestaurantDto.class))
                .toList();
    }

    @Override
    public List<RestaurantDto> findByNameContainingIgnoreCase(String pattern) {
        return restaurantRepository.findByNameContainingIgnoreCaseOrderByRatingDesc(pattern)
                .stream()
                .map(resto->modelMapper.map(resto, RestaurantDto.class))
                .toList();
    }

    @Override
    public Resource getRestaurantBanner(String restaurantId) {

        Restaurant restaurant = findAndValidate(restaurantId);

        if (restaurant.getBanner() == null) {
            throw new ResourceNotFound("Banner not found for restaurant");
        }

        Path bannerPath = Paths.get(bannerFolderpath)
                .resolve(restaurant.getBanner())
                .normalize();

        try {
            Resource resource = new UrlResource(bannerPath.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFound("Banner file not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid banner path", e);
        }
    }


    @Override
    @Transactional
    public RestaurantDto uploadBanner(MultipartFile file, String restaurantId) throws IOException {

        Restaurant restaurant = findAndValidate(restaurantId);

        validateRestaurantAccess(restaurant);

        if (restaurant.getBanner() != null) {
            throw new IllegalStateException(
                    "Banner already exists. Use UPDATE banner API to update the banner.");
        }

        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String newFileName = System.currentTimeMillis() + extension;

        FileData fileData = fileService.uploadFile(file, bannerFolderpath + newFileName);
        restaurant.setBanner(fileData.getFileName());

        restaurantRepository.save(restaurant);
        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Override
    @Transactional
    public RestaurantDto updateBanner(MultipartFile file, String restaurantId) throws IOException {

        Restaurant restaurant = findAndValidate(restaurantId);

        validateRestaurantAccess(restaurant);

        if (restaurant.getBanner() == null) {
            throw new IllegalStateException(
                    "No banner exists. Use ADD banner API first.");
        }

        Path oldPath = Paths.get(bannerFolderpath)
                .resolve(restaurant.getBanner())
                .normalize();
        Files.deleteIfExists(oldPath);

        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String newFileName = System.currentTimeMillis() + extension;

        FileData fileData = fileService.uploadFile(file, bannerFolderpath + newFileName);
        restaurant.setBanner(fileData.getFileName());

        restaurantRepository.save(restaurant);
        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Override
    @Transactional
    public void deleteBanner(String restaurantId) {

        Restaurant restaurant = findAndValidate(restaurantId);

        validateRestaurantAccess(restaurant);

        if (restaurant.getBanner() == null) {
            return;
        }

        try {
            Path bannerPath = Paths.get(bannerFolderpath)
                    .resolve(restaurant.getBanner())
                    .normalize();

            Files.deleteIfExists(bannerPath);

            restaurant.setBanner(null);
            restaurantRepository.save(restaurant);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete restaurant banner", e);
        }
    }


    @Override
    public List<RestaurantDto> getByOwner(String ownerId) {
        return restaurantRepository.findByOwnerId(ownerId)
                .stream()
                .map(resto->modelMapper.map(resto, RestaurantDto.class))
                .toList();
    }

    @Override
    @Transactional
    public void activateRestaurant(String restaurantId) {

        Restaurant restaurant = findAndValidate(restaurantId);
        validateRestaurantAccess(restaurant);

        restaurant.setActive(true);
        restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public void deactivateRestaurant(String restaurantId) {

        Restaurant restaurant = findAndValidate(restaurantId);

        validateRestaurantAccess(restaurant);

        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }

}
