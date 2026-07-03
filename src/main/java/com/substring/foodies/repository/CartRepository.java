package com.substring.foodies.repository;

import com.substring.foodies.entity.Cart;
import com.substring.foodies.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {

    Optional<Cart> findByCreatorId(String creatorId);
    Optional<Cart> findByCreator(User creator);
    void deleteByCreatorId(String creatorId);
}
