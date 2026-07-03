package com.substring.foodies.repository;

import com.substring.foodies.entity.DeliveryEarning;
import com.substring.foodies.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DeliveryEarningRepository extends JpaRepository<DeliveryEarning, String> {

    List<DeliveryEarning> findByDeliveryBoy(User deliveryBoy);
}
