package com.substring.foodies.entity;
import com.substring.foodies.dto.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseAuditableEntity{

    @Id
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String addressLine;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String country;

    @OneToOne(mappedBy = "address")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    @ManyToMany(mappedBy = "addresses")
    private Set<Restaurant> restaurants = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address other = (Address) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
