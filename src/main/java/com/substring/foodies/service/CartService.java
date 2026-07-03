package com.substring.foodies.service;

import com.substring.foodies.dto.AddItemToCartRequest;
import com.substring.foodies.dto.CartDto;
import com.substring.foodies.dto.CartItemsDto;

import java.util.List;

public interface CartService {

    CartDto addItemToCart(AddItemToCartRequest addItemToCartRequest);

    CartDto getCart(String userId);

    CartDto removeItemFromCart(String cartItemId, String userId);

    CartDto reduceItemFromCart(String cartItemId, String userId);

    List<CartItemsDto> getCartItems(String userId);

    void clearCart(String userId);
}
