package com.substring.foodies.controller;

import com.substring.foodies.dto.AddItemToCartRequest;
import com.substring.foodies.dto.CartDto;
import com.substring.foodies.dto.CartItemsDto;
import com.substring.foodies.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartDto> addItemToCart(@RequestBody @Valid AddItemToCartRequest request) {
        CartDto cartDto = cartService.addItemToCart(request);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    /**
     * Get cart for a user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartDto> getCart(@PathVariable String userId) {
        CartDto cartDto = cartService.getCart(userId);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    /**
     * Get all items in a user's cart
     */
    @GetMapping("/{userId}/items")
    public ResponseEntity<List<CartItemsDto>> getCartItems(@PathVariable String userId) {
        List<CartItemsDto> items = cartService.getCartItems(userId);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    /**
     * Remove an item from the cart
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartDto> removeItemFromCart(@PathVariable String userId,
                                                      @PathVariable String itemId) {
        CartDto cartDto = cartService.removeItemFromCart(itemId, userId);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/reduce/{itemId}")
    public ResponseEntity<CartDto> reduceItemFromCart(@PathVariable String userId,
                                                      @PathVariable String itemId) {
        CartDto cartDto = cartService.reduceItemFromCart(itemId, userId);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    /**
     * Clear the cart for a user
     */
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<String> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return new ResponseEntity<>("Cart cleared successfully.", HttpStatus.OK);
    }
}
