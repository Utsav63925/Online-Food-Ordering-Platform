package com.substring.foodies.repository;

import com.substring.foodies.entity.Cart;
import com.substring.foodies.entity.CartItems;
import com.substring.foodies.entity.FoodItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, String> {

    List<CartItems> findByCart(Cart cart);
    CartItems findByFoodItems(FoodItems foodItems);
}
