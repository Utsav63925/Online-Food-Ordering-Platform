package com.substring.foodies.entity;
import com.substring.foodies.dto.enums.FoodType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FoodItems extends BaseAuditableEntity{

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Min(0)
    private int price;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Enumerated(EnumType.STRING)
    private FoodType foodType = FoodType.VEG;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "rating_star", nullable = false)
    private Double rating = 0.0;

    private String imageUrl;

    @Column(
            name = "normalized_name",
            nullable = false
    )
    private String normalizedName;

    @Min(0)
    private int discountAmount = 0;

    @ManyToMany(mappedBy = "foodItemsList")
    private Set<Restaurant> restaurants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_category_id", nullable = false)
    private FoodCategory foodCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_sub_category_id", nullable = false)
    private FoodSubCategory foodSubCategory;

    public int actualPrice()
    {
        return Math.max(0, price - discountAmount);
    }

    public int getDiscountPercentage() {
        if (price == 0) return 0;
        return (int) ((discountAmount * 100.0) / price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodItems)) return false;
        FoodItems other = (FoodItems) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    @PreUpdate
    private void normalizeName() {
        this.normalizedName = name
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        if (discountAmount > price) {
            throw new IllegalStateException("Discount cannot exceed price.");
        }
    }

}
