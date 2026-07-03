package com.substring.foodies.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Cart extends BaseAuditableEntity{

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(length = 36)
    private String id;

    @ManyToOne
    private Restaurant restaurant;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItems> cartItems = new ArrayList<>();

}
