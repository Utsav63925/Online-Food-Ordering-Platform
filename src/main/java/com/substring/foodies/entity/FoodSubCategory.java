package com.substring.foodies.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "food_sub_category")
public class FoodSubCategory extends BaseAuditableEntity{

    @Id
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private FoodCategory foodCategory;

    // 👇 controls category order in menu
    private int displayOrder;

    @Column(
            name = "normalized_name",
            nullable = false
    )
    private String normalizedName;

    @OneToMany(
            mappedBy = "foodSubCategory",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FoodItems> foodItemList = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void normalizeName() {
        this.normalizedName = name
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

}
