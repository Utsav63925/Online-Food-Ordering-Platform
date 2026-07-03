package com.substring.foodies.repository;

import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    List<User> findByName(String userName);
    Optional<User> findByEmail(String userEmail);
    int countByRole(Role role);
    boolean existsByEmail(String email);

    // 🚴 Find free delivery boys
    Optional<User> findFirstByRoleAndIsAvailableTrue(Role role);

    // 🔁 Free delivery boys after 10 minutes
    List<User> findByRoleAndIsAvailableFalse(Role role);
}
