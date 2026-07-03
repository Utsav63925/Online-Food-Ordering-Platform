package com.substring.foodies.entity;
import com.substring.foodies.dto.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name="foodie_users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditableEntity{

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    private boolean isAvailable=true;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Restaurant> restaurantList = new ArrayList<>();

    private boolean isEnabled=true;

    private String gender;

    @Column(name = "reset_otp", nullable = true)
    private String resetOtp;

    @Column(name = "otp_expiry", nullable = true)
    private LocalDateTime otpExpiry;

    @OneToOne(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;
}
