package com.substring.foodies.repository;

import com.substring.foodies.entity.Address;
import com.substring.foodies.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findByUser(User user);
    Optional<Address> findByUserId(String userId);
}
