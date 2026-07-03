package com.substring.foodies.repository;

import com.substring.foodies.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, String> {

    List<Restaurant> findByOwnerId(String ownerId);

    boolean existsByOwnerId(String ownerId);

    List<Restaurant> findByIsOpenTrueOrderByRatingDesc();

    List<Restaurant> findByNameContainingIgnoreCaseOrderByRatingDesc(String pattern);

    List<Restaurant> findByFoodItemsList_IdOrderByRatingDesc(String foodId);

}
