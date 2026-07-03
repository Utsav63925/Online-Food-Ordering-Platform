package com.substring.foodies.repository;

import com.substring.foodies.entity.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodCategoryRepository extends JpaRepository<FoodCategory, String> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNormalizedNameIgnoreCase(String name);
}
