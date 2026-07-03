package com.substring.foodies.repository;

import com.substring.foodies.entity.FoodRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FoodRatingRepository extends JpaRepository<FoodRating, Long> {

    Optional<FoodRating> findByUserIdAndFoodId(String userId, String foodId);

    @Query("SELECT COALESCE(AVG(fr.rating), 0) FROM FoodRating fr WHERE fr.food.id = :foodId")
    double averageRatingByFood(String foodId);
}
