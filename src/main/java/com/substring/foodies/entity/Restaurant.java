package com.substring.foodies.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "foodie_restaurant")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant extends BaseAuditableEntity{

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @ManyToMany()
    @JoinTable(
            name = "restaurant_address", // join table name
            joinColumns = @JoinColumn(name = "restaurant_id"), // this entity's FK
            inverseJoinColumns = @JoinColumn(name = "address_id") // other entity's FK
    )
    private Set<Address> addresses = new HashSet<>();

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    private boolean isOpen=true;

    private boolean isActive=true;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "rating_star")
    private Double rating = 0.0;

    @ManyToMany
    @JoinTable(
            name = "restaurant_food",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private Set<FoodItems> foodItemsList = new HashSet<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> cartList = new ArrayList<>();

    private String banner;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant other = (Restaurant) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
