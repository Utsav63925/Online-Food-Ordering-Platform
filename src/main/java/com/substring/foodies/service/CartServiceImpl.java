package com.substring.foodies.service;

import com.substring.foodies.dto.AddItemToCartRequest;
import com.substring.foodies.dto.CartDto;
import com.substring.foodies.dto.CartItemsDto;
import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.*;
import com.substring.foodies.exception.BadItemRequestException;
import com.substring.foodies.exception.FoodItemUnavailableException;
import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.CartRepository;
import com.substring.foodies.repository.FoodItemRepository;
import com.substring.foodies.repository.RestaurantRepository;
import com.substring.foodies.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ---------------- ADD ITEM ----------------

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Invalid session"));
    }

    private Cart findAndValidate(String userId)
    {
        User user = getLoggedInUser();
        if(user.getRole() != Role.ROLE_ADMIN && !user.getId().equals(userId))
        {
            throw new AccessDeniedException("You can only access your own cart");
        }

        Cart cart = cartRepository.findByCreatorId(userId)
                .orElseThrow(() -> new ResourceNotFound("Cart not found for userId = " + userId));

        return cart;
    }

    @Override
    @Transactional
    public CartDto addItemToCart(AddItemToCartRequest request) {

        if (request.getQuantity() <= 0) {
            throw new BadItemRequestException("Quantity must be greater than zero");
        }

        String userId = request.getUserId();
        String foodItemId = request.getFoodItemId();
        String restoId = request.getRestoId();

        User getLoggedInUser = getLoggedInUser();

        if(getLoggedInUser.getRole() != Role.ROLE_ADMIN && !getLoggedInUser.getId().equals(userId))
        {
            throw new AccessDeniedException("You can only create cart for yourself.");
        }

        User user = getLoggedInUser.getRole() == Role.ROLE_ADMIN ?
                            userRepository.findById(userId).orElseThrow(()->new ResourceNotFound("User not found with id = "+userId))
                            : getLoggedInUser;


        Restaurant restaurant = restaurantRepository.findById(restoId)
                .orElseThrow(() -> new ResourceNotFound("Restaurant not found with id = " + restoId));

        // ✅ OPTIONAL but RECOMMENDED
        if (!restaurant.isOpen()) {
            throw new BadItemRequestException("Restaurant is currently closed");
        }

        FoodItems foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFound("Food item not found with id = " + foodItemId));

        Cart cart = cartRepository.findByCreatorId(userId)
                .orElseGet(() ->
                        cartRepository.save(
                                Cart.builder()
                                        .creator(user)
                                        .restaurant(restaurant)
                                        .build()
                        )
                );

        if (!cart.getRestaurant().getId().equals(restoId)) {
            throw new BadItemRequestException("Cart already contains items from another restaurant");
        }

        FoodItems restaurantFood = restaurant.getFoodItemsList()
                .stream()
                .filter(food -> food.getId().equals(foodItemId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFound("Restaurant does not sell this food item")
                );

        if (!restaurantFood.getIsAvailable()) {
            throw new FoodItemUnavailableException("Food item is currently unavailable");
        }

        boolean existing = false;
        for (CartItems item : cart.getCartItems()) {
            if (item.getFoodItems().getId().equals(foodItemId)) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                existing = true;
                break;
            }
        }

        if (!existing) {
            CartItems newItem = CartItems.builder()
                    .cart(cart)
                    .foodItems(foodItem)
                    .quantity(request.getQuantity())
                    .build();
            cart.getCartItems().add(newItem);
        }

        cartRepository.save(cart);
        return modelMapper.map(cart, CartDto.class);
    }

    // ---------------- GET CART ----------------
    @Override
    public CartDto getCart(String userId) {
        Cart cart = findAndValidate(userId);
        return modelMapper.map(cart, CartDto.class);
    }

    // ---------------- REMOVE ITEM ----------------
    @Override
    @Transactional
    public CartDto removeItemFromCart(String cartItemId, String userId) {

        Cart cart = findAndValidate(userId);
        Iterator<CartItems> iterator = cart.getCartItems().iterator();
        while (iterator.hasNext()) {
            CartItems item = iterator.next();
            if (item.getId().equals(cartItemId)) {
                iterator.remove();
                cartRepository.save(cart);
                return modelMapper.map(cart, CartDto.class);
            }
        }

        throw new ResourceNotFound("Item not found in cart with id = " + cartItemId);
    }

    // ---------------- REDUCE ITEM ----------------
    @Override
    @Transactional
    public CartDto reduceItemFromCart(String cartItemId, String userId) {

        Cart cart = findAndValidate(userId);
        Iterator<CartItems> iterator = cart.getCartItems().iterator();
        while (iterator.hasNext()) {
            CartItems item = iterator.next();
            if (item.getId().equals(cartItemId)) {
                if (item.getQuantity() <= 1) {
                    iterator.remove();
                } else {
                    item.setQuantity(item.getQuantity() - 1);
                }

                cartRepository.save(cart);
                return modelMapper.map(cart, CartDto.class);
            }
        }

        throw new ResourceNotFound("Item not found in cart with id = " + cartItemId);
    }

    // ---------------- GET CART ITEMS ----------------
    @Override
    public List<CartItemsDto> getCartItems(String userId) {
        Cart cart = findAndValidate(userId);
        return cart.getCartItems()
                .stream()
                .map(item -> modelMapper.map(item, CartItemsDto.class))
                .toList();
    }

    // ---------------- CLEAR CART ----------------
    @Override
    @Transactional
    public void clearCart(String userId) {
        Cart cart = findAndValidate(userId);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}
