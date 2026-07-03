package com.substring.foodies.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_category")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodCategory extends BaseAuditableEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private String description;

    // 👇 controls category order in menu
    @Column(length = 255)
    private int displayOrder;

    @Column(
            name = "normalized_name",
            nullable = false
    )
    private String normalizedName;

    @OneToMany(mappedBy = "foodCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodSubCategory> foodSubCategoryList = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void normalizeName() {
        this.normalizedName = name
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

}
